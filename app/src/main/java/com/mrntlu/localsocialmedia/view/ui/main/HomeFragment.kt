package com.mrntlu.localsocialmedia.view.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentHomeBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.utils.*
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.adapter.FeedAdapter
import com.mrntlu.localsocialmedia.view.adapter.FeedInteraction
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch
import kotlin.math.ln

@SuppressLint("MissingPermission")
class HomeFragment : BaseFragment<FragmentHomeBinding>(), CoroutinesErrorHandler {

    private var feedAdapter: FeedAdapter? = null
    private val viewModel: FeedViewModel by viewModels()
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var feedController: FeedController? = null
    private var markerList: ArrayList<Marker>? = null
    private var circle: Circle? = null
    private var mMap: GoogleMap? = null
    private var cancellationToken = CancellationTokenSource()
    private var mLocation: LatLng? = null
    private var isLoading = false
    private var pageNum = 1
    private var radius: Double = Double.NaN

    companion object{
        const val RADIUS_ARG = "radius"
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
                getLastKnownUserLocation(it)
            else
                Toast.makeText(it, "Couldn't get the permission.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            radius = it.getDouble(RADIUS_ARG, Double.NaN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container,false)
        binding.homeMap.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(false)
        setHasOptionsMenu(true)
        markerList = arrayListOf()

        if (radius.isNaN())
            radius = 10.0

        feedController = FeedController()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)
        setMap()
        if (!checkPermissions(view.context))
            requestPermission()
        else
            getLastKnownUserLocation(view.context)

        setRecyclerView()
        setObservers()
        setListeners()
    }

    private fun setMap() {
        binding.homeMap.getMapAsync { googleMap ->
            mMap = googleMap
            with(mMap!!.uiSettings){
                isZoomControlsEnabled = false
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMapToolbarEnabled = false
            }

            mMap?.setOnMarkerClickListener { marker ->
                (marker.tag as? FeedModel)?.let {
                    navController.navigate(R.id.action_homeFragment_to_feedDetailsFragment, bundleOf(
                        FeedDetailsFragment.FEED_MODEL_ARG to it
                    ))
                }

                true
            }
        }
    }

    private fun setMarker(isPaginating: Boolean, newFeedList: ArrayList<FeedModel>) {
        mMap?.let {
            if (!isPaginating) {
                for (marker in markerList!!)
                    marker.remove()
                markerList!!.clear()
            }

            for (feed in newFeedList){
                val location = LatLng(feed.latitude.toDouble(), feed.longitude.toDouble())
                val marker = it.addMarker(MarkerOptions()
                    .position(location)
                    .title(feed.locationName ?: "Feed")
                    .snippet(feed.message))
                marker?.tag = feed
                marker?.let {  m ->
                    markerList!!.add(m)
                }
            }
        }
    }

    private fun setCircle(){
        if (mLocation != null){
            val radiusText = "${radius.toInt()}m"
            binding.homeRadiusText.text = radiusText
            mMap?.let {
                circle?.remove()
                circle = it.addCircle(CircleOptions()
                    .center(mLocation!!)
                    .radius(radius)
                    .strokeColor(Color.parseColor("#ACC3E9"))
                    .fillColor(Color.parseColor("#40ACC3E9")))
                it.animateCamera(CameraUpdateFactory.newLatLngZoom(circle!!.center, getZoomLevel(circle!!)))
            }
        }
    }

    private fun getZoomLevel(circle: Circle): Float{
        val zoomLevel: Float
        val radius = circle.radius + circle.radius / 2
        val scale = radius / 200
        zoomLevel = (16 - ln(scale) / ln(2.0)).toFloat()

        return zoomLevel
    }

    private fun setRecyclerView() {
        binding.homeRV.apply{
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            feedAdapter = FeedAdapter(currentUser, object: FeedInteraction {
                override fun onItemSelected(position: Int, item: FeedModel) {
                    val bundle = Bundle()
                    bundle.putParcelable(FeedDetailsFragment.FEED_MODEL_ARG, item)
                    navController.navigate(R.id.action_homeFragment_to_feedDetailsFragment, bundle)
                }

                override fun onReportPressed(position: Int, feedModel: FeedModel) {
                    MaterialDialogUtil.setDialog(this@apply.context, getString(R.string.are_you_sure), "Do you want to REPORT?", object: DialogButtons{
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.reportFeed(feedModel.id.toString(), token, feedController!!.dialogErrorHandler(context)).observe(viewLifecycleOwner){ response ->
                                (activity as MainActivity).setLoadingLayout(false)
                                MaterialDialogUtil.showInfoDialog(
                                    context,
                                    if (response.status == 200) "Success" else "Error!",
                                    if (response.status == 200) "Thanks for reporting, we will review it as soon as possible." else response.message
                                )
                            }
                        }
                    })
                }

                override fun onVotePressed(voteType: VoteType, position: Int, feedModel: FeedModel) {
                    val observer = feedController?.voteClickHandler(
                        voteType, viewModel, feedModel, token, this@HomeFragment
                    )

                    observer?.observe(viewLifecycleOwner){ response ->
                        if (response.status == 200 && response.data != null){
                            feedAdapter?.updateItem(position, response.data)
                        }
                        observer.removeObservers(viewLifecycleOwner)
                    }
                }

                override fun onDeletePressed(position: Int, feedModel: FeedModel) {
                    MaterialDialogUtil.setDialog(this@apply.context, getString(R.string.are_you_sure), "Do you want to DELETE?", object: DialogButtons{
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.deleteFeed(feedModel.id.toString(), token, feedController!!.dialogErrorHandler(context)).observe(viewLifecycleOwner){ response ->
                                if (response.status == 200) {
                                    (activity as MainActivity).setLoadingLayout(false)
                                    feedAdapter?.removeItem(position, feedModel)
                                }else
                                    MaterialDialogUtil.showErrorDialog(context, response.message)
                            }
                        }
                    })
                }

                override fun onErrorRefreshPressed() {
                    feedAdapter?.submitLoading()
                    setData()
                }
            })
            adapter = feedAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    feedAdapter?.let {
                        if (it.itemCount % Constants.FEED_PAGINATION_LIMIT == 0 &&
                            linearLayoutManager.findLastCompletelyVisibleItemPosition() == it.itemCount - 1 &&
                            isScrolling && !isLoading) {

                            isLoading = true
                            pageNum++
                            it.submitPaginationLoading()
                            this@apply.scrollToPosition(it.itemCount - 1)
                            setData()
                        }
                    }
                }
            })
        }
    }

    private fun setObservers(){
        viewModel.setFeedByLocation().observe(viewLifecycleOwner){
            if (it.status == 200){
                it.data?.let { data ->
                    if (pageNum <= 1) {
                        setMarker(false, data)
                        feedAdapter?.submitList(data)
                    }else {
                        isLoading = false
                        setMarker(true, data)
                        feedAdapter?.updateList(data)
                    }
                } ?: feedAdapter?.submitPaginationError()
            }else
                onError(it.message)
        }
    }

    private fun setData(){
        viewModel.getFeedsByLocation(
            mLocation!!.latitude.toFloat() , mLocation!!.longitude.toFloat(), radius.toInt(),
            pageNum, token, this)
    }

    private fun setListeners(){
        binding.profileSwipeRefresh.setOnRefreshListener {
            pageNum = 1
            feedAdapter?.submitLoading()
            setData()
            binding.profileSwipeRefresh.isRefreshing = false
        }

        binding.homeZoomInButton.setOnClickListener {
            val prevRadius = radius
            if (radius <= 200){ // MAX 200M
                when (radius) {
                    in 10.0..25.0 -> {
                        radius += 5
                    }
                    in 30.0..40.0 -> {
                        radius += 10
                    }
                    in 50.0..150.0 -> {
                        radius += 50
                    }
                }
            }
            if (prevRadius != radius) {
                setCircle()
                pageNum = 1
                feedAdapter?.submitLoading()
                setData()
            }
        }

        binding.homeZoomOutButton.setOnClickListener {
            val prevRadius = radius
            if (radius >= 10){ // MIN 10M
                when (radius) {
                    in 15.0..30.0 -> {
                        radius -= 5
                    }
                    in 30.0..50.0 -> {
                        radius -= 10
                    }
                    in 100.0..200.0 -> {
                        radius -= 50
                    }
                }
            }
            if (prevRadius != radius) {
                setCircle()
                pageNum = 1
                feedAdapter?.submitLoading()
                setData()
            }
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                feedAdapter?.submitError(message)
            }
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        permissionActivityResult.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun getLastKnownUserLocation(context: Context) {
        if (!checkPermissions(context))
            requestPermission()
        else {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                location?.let {
                    onUserLocationReceivedHandler(it)
                } ?: requestUserLocation()
            }
        }
    }

    private fun requestUserLocation(){
        val loadingText = "Please wait while we are getting your location..."
        binding.homeLoadingLayout.textView.text = loadingText
        binding.homeLoadingLayout.root.setVisible()
        mFusedLocationClient?.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken.token)?.addOnSuccessListener { location: Location? ->
            binding.homeLoadingLayout.root.setGone()
            location?.let {
                onUserLocationReceivedHandler(it)
            } ?: Toast.makeText(context, "Couldn't get location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUserLocationReceivedHandler(location: Location){
        mLocation = LatLng(location.latitude, location.longitude)
        mMap?.isMyLocationEnabled = true
        setData()
        setCircle()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_menu -> {
                navController.navigate(
                    R.id.action_homeFragment_to_postFeedFragment,
                    bundleOf(
                        PostFeedFragment.DIRECTION_ARG to 0,
                        RADIUS_ARG to radius
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.homeMap.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.homeMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.homeMap.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.homeMap.onStop()
        cancellationToken.cancel()
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_binding != null)
            binding.homeMap.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.homeMap.onLowMemory()
    }

    override fun onDestroyView() {
        cancellationToken.cancel()
        feedAdapter = null
        feedController = null
        circle?.remove()
        circle = null
        for (marker in markerList!!)
            marker.remove()
        markerList = null
        mMap?.clear()
        mMap = null
        binding.homeMap.onDestroy()
        super.onDestroyView()
    }
}