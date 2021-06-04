package com.mrntlu.localsocialmedia.view.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
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
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.utils.*
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

class PostFeedFragment : BaseFragment<FragmentPostFeedBinding>(), CoroutinesErrorHandler {
    enum class Direction(val num: Int){
        HomeFragment(0),
        FeedFragment(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.num == value }
        }
    }
    private val viewModel: FeedViewModel by viewModels()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocation: Location
    private var cancellationToken = CancellationTokenSource()
    private lateinit var direction: Direction

    companion object {
        const val DIRECTION_ARG = "direction"
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

    private fun requestPermission() {
        permissionActivityResult.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
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
                viewModel.postFeed(
                    FeedBody(
                        binding.postFeedEditText.text.toString(), FeedType.FEED.num,
                        mLocation.latitude.toFloat(), mLocation.longitude.toFloat(),
                        binding.postFeedLocationText.text.toString(), null
                    ), token, this
                ).observe(viewLifecycleOwner){ response ->
                    binding.postFeedLoading.root.setGone()
                    if (response.status == 200){
                        navController.navigate(
                            when(direction){
                                Direction.HomeFragment -> {
                                    R.id.action_postFeedFragment_to_homeFragment
                                }
                                Direction.FeedFragment -> {
                                    R.id.action_postFeedFragment_to_feedFragment
                                }
                            })
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
        super.onDestroyView()
    }
}