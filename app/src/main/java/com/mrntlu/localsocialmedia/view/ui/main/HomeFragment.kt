package com.mrntlu.localsocialmedia.view.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.mrntlu.localsocialmedia.databinding.FragmentHomeBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserVoteModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.utils.Constants
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.FeedAdapter
import com.mrntlu.localsocialmedia.view.adapter.FeedInteraction
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch
import kotlin.math.ln

class HomeFragment : BaseFragment<FragmentHomeBinding>(), CoroutinesErrorHandler {

    private var feedAdapter: FeedAdapter? = null
    private val viewModel: FeedViewModel by viewModels()
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var marker: Marker? = null
    private var circle: Circle? = null
    private var mMap: GoogleMap? = null
    private var mLocation: LatLng? = null
    private var isLoading = false
    private var pageNum = 1
    private var radius = 10.0

    private val permissionActivityResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        var isPermissionsGranted = false
        for (permission in permissions.values){
            isPermissionsGranted = permission
            if (!permission)
                break
        }
        context?.let {
            if (isPermissionsGranted)
                getCurrentUserLocation(it)
            else
                Toast.makeText(it, "Couldn't get the permission.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container,false)
        binding.homeMap.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.context)
        setMap()
        if (!checkPermissions(view.context))
            requestPermission()
        else
            getCurrentUserLocation(view.context)

        setRecyclerView()
        setObservers()
        setData()
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
            }
        }
    }

    private fun setMarker() {
        if (mLocation != null){
            mMap?.let {
                marker?.remove()
                marker = it.addMarker(MarkerOptions()
                    .position(mLocation!!)
                    .title("Your Position"))
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
                    .strokeColor(Color.RED)
                    .fillColor(Color.WHITE))
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
                    printLog("Feed item clicked $item")
                }

                override fun onReportPressed(position: Int, feedModel: FeedModel) {
                    viewModel.reportFeed(feedModel.id.toString(), token, this@HomeFragment).observe(viewLifecycleOwner){ response ->
                        if (response.status == 200){

                        }else{

                        }
                    }
                }

                override fun onVotePressed(voteType: VoteType, position: Int, feedModel: FeedModel) {
                    val observer = if (feedModel.userVote.isVoted){
                        if (voteType == feedModel.userVote.voteType){
                            viewModel.deleteFeedVote(feedModel.id.toString(), token, this@HomeFragment)
                        }else{
                            viewModel.updateFeedVote(VoteBody(voteType.num), feedModel.id.toString(), token, this@HomeFragment)
                        }
                    }else{
                        viewModel.voteFeed(VoteBody(voteType.num), feedModel.id.toString(), token, this@HomeFragment)
                    }

                    observer.observe(viewLifecycleOwner){ response ->
                        printLog("$response")
                        if (response.status == 200 && response.data != null){
                            feedAdapter?.updateItem(position, response.data)
                        }else{

                        }
                    }
                }

                override fun onErrorRefreshPressed() {
                    feedAdapter?.submitLoading()
                    setData()
                }
            })
            adapter = feedAdapter

            var isScrolling=false
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
                    if (pageNum <= 1)
                        feedAdapter?.submitList(data)
                    else {
                        isLoading = false
                        feedAdapter?.updateList(data)
                    }
                } ?: feedAdapter?.submitPaginationError()
            }else
                onError(it.message)
        }
    }

    private fun setData(){
        viewModel.getFeedsByLocation(
            40.97446f, 29.24127f, 3,
            pageNum, token, this)
    }

    private fun setListeners(){
        binding.homeZoomInButton.setOnClickListener {
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
            setCircle()
        }

        binding.homeZoomOutButton.setOnClickListener {
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
            setCircle()
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

    @SuppressLint("MissingPermission")
    private fun getCurrentUserLocation(context: Context) {
        if (!checkPermissions(context))
            requestPermission()
        else {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                location?.let {
                    mLocation = LatLng(it.latitude, it.longitude)
                    setMarker()
                    setCircle()
                } ?: Toast.makeText(context, "Couldn't get location.", Toast.LENGTH_SHORT).show()
            }
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.homeMap.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.homeMap.onLowMemory()
    }

    override fun onDestroyView() {
        feedAdapter = null
        marker?.remove()
        marker = null
        mMap?.clear()
        mMap = null
        binding.homeMap.onDestroy()
        super.onDestroyView()
    }
}