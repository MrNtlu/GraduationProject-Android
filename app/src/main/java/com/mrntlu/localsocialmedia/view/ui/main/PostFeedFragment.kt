package com.mrntlu.localsocialmedia.view.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentPostFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedType
import com.mrntlu.localsocialmedia.utils.*
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.adapter.FeedImageAdapter
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostFeedFragment : BaseFragment<FragmentPostFeedBinding>(), CoroutinesErrorHandler {
    enum class Direction(val num: Int){
        HomeFragment(0),
        FeedFragment(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.num == value }
        }
    }
    private val viewModel: FeedViewModel by viewModels()
    private val imageList: ArrayList<MultipartBody.Part> = arrayListOf()
    private val imageUriList: ArrayList<Uri> = arrayListOf()
    private var imageAdapter: FeedImageAdapter? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocation: Location
    private var cancellationToken = CancellationTokenSource()
    private lateinit var direction: Direction
    private var radius: Double = Double.NaN

    companion object {
        const val DIRECTION_ARG = "direction"
    }

    private val imagePickerActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        onActivityResult(result)
    }

    private val imagePermissionActivityResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){ permission ->
        if (permission){
            openImageChooser()
        }else
            context?.let {
                MaterialDialogUtil.showErrorDialog(it, "Permission denied!")
            }
    }

    private val permissionActivityResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        var isPermissionsGranted = false
        for (permission in permissions.values){
            isPermissionsGranted = permission
            if (!permission)
                break
        }
        context?.let {
            if (isPermissionsGranted)
                getLocation()
            else
                Toast.makeText(it, "Couldn't get the permission.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            direction = Direction.fromInt(it.getInt(DIRECTION_ARG, 0))
            radius = it.getDouble(HomeFragment.RADIUS_ARG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostFeedBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(true)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)

        setUI()
        if (!checkPermissions(view.context))
            requestPermission()
        else
            getLocation()
        setListeners()
        setRecyclerView()
    }

    private fun setUI() {
        currentUser.imageUri?.let {
            Glide.with(binding.postFeedAuthorImage)
                .load(it)
                .addListener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        binding.postFeedAuthorImageProgress.setGone()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        binding.postFeedAuthorImageProgress.setGone()
                        return false
                    }

                })
                .placeholder(
                    ResourcesCompat.getDrawable(binding.postFeedAuthorImage.context.resources,
                        R.drawable.ic_account_126,null))
                .into(binding.postFeedAuthorImage)
        } ?: binding.postFeedAuthorImage.setImageResource(R.drawable.ic_account_126)

        binding.postFeedAuthorName.text = currentUser.name
    }

    private fun checkPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setRecyclerView() {
        binding.postFeedImageRV.apply {
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager
            imageAdapter = FeedImageAdapter()
            adapter = imageAdapter
        }
    }

    private fun requestPermission() {
        permissionActivityResult.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun checkImagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            context?.let {
                val result = ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
                result == PackageManager.PERMISSION_GRANTED
            } ?: true
        }
    }

    private fun requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            imagePermissionActivityResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE,)
        else{
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    ),
                    1001
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        binding.postFeedProgress.setVisible()
        mFusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            location?.let {
                onUserLocationReceivedHandler(it)
            } ?: requestUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestUserLocation(){
        mFusedLocationClient?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken.token)?.addOnSuccessListener { location: Location? ->
            location?.let {
                onUserLocationReceivedHandler(it)
            } ?: Toast.makeText(context, "Couldn't get location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUserLocationReceivedHandler(location: Location) {
        binding.postFeedProgress.setGone()
        mLocation = location
        binding.postFeedLocationText.text = context?.let {
            location.getLocationName(it)
        } ?: "Unknown"
    }

    private fun setListeners() {
        binding.postFeedButton.setOnClickListener {
            if (::mLocation.isInitialized && binding.postFeedEditText.text.toString().isNotEmptyOrBlank()){
                binding.postFeedLoading.root.setVisible()
                val messageBody = RequestBody.create(MediaType.parse("text/plain"), binding.postFeedEditText.text.toString())
                val typeBody = RequestBody.create(MediaType.parse("text/plain"), FeedType.FEED.num.toString())
                val latitudeBody = RequestBody.create(MediaType.parse("text/plain"), mLocation.latitude.toString())
                val longitudeBody = RequestBody.create(MediaType.parse("text/plain"), mLocation.longitude.toString())
                val locationNameBody = RequestBody.create(MediaType.parse("text/plain"), binding.postFeedLocationText.text.toString())
                val imageListBody = if (imageList.isEmpty())
                    null
                else
                    imageList

                viewModel.postFeed(
                    messageBody,
                    typeBody,
                    latitudeBody,
                    longitudeBody,
                    locationNameBody,
                    imageListBody, token, this
                ).observe(viewLifecycleOwner){ response ->
                    binding.postFeedLoading.root.setGone()
                    printLog("$response ${response.data} ${response.message}")
                    if (response.status == 200){
                        navController.navigate(
                            when(direction){
                                Direction.HomeFragment -> {
                                    R.id.action_postFeedFragment_to_homeFragment
                                }
                                Direction.FeedFragment -> {
                                    R.id.action_postFeedFragment_to_feedFragment
                                }
                            }, bundleOf(HomeFragment.RADIUS_ARG to radius))
                    }else
                        onError(response.message)
                }
            }else{
                MaterialDialogUtil.showErrorDialog(it.context,
                    if (::mLocation.isInitialized)
                        "Couldn't get location. Please wait and try again!"
                    else
                        "Please don't leave message empty!"
                )
            }
        }

        binding.postFeedAddImageButton.setOnClickListener {
            if (checkImagePermission())
                openImageChooser()
            else
                requestImagePermission()
        }
    }

    private fun onActivityResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_CANCELED){
            context?.let {
                if (result.data != null && result.data!!.data != null){
                    val imageResult = result.data!!.data!!

                    val image = FileUtils.getFile(it, imageResult)
                    val requestFile = RequestBody.create(
                        MediaType.parse(it.contentResolver.getType(imageResult) ?: "image/jpeg"),
                        image
                    )
                    val body = MultipartBody.Part.createFormData("images", image.name, requestFile)
                    imageList.add(body)
                    imageUriList.add(imageResult)
                    if (binding.postFeedImageRV.isGone)
                        binding.postFeedImageRV.setVisible()

                    imageAdapter?.submitList(imageUriList)
                }
            }

        }
    }

    private fun openImageChooser(){
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        imagePickerActivityResult.launch(intent)
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                if (_binding != null && binding.postFeedLoading.root.isVisible)
                    binding.postFeedLoading.root.setGone()
                context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        cancellationToken.cancel()
    }

    override fun onDestroyView() {
        cancellationToken.cancel()
        imageAdapter = null
        super.onDestroyView()
    }
}