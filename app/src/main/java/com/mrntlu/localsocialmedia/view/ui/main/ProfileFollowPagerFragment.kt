
package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentProfileFollowPagerBinding
import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.FollowAdapter
import com.mrntlu.localsocialmedia.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.*

class ProfileFollowPagerFragment(private val pagerType: PagerType, private val userID: Int):
    BaseFragment<FragmentProfileFollowPagerBinding>(), CoroutinesErrorHandler {
    enum class PagerType{
        FOLLOWER,
        FOLLOWING
    }

    private var followAdapter: FollowAdapter? = null
    private val userViewModel: UserViewModel by viewModels()

    private var isLoading = false
    private var pageNum = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileFollowPagerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setObservers()
        setData()
    }

    private fun setRecyclerView() {
        binding.profileFollowRV.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            followAdapter = FollowAdapter(object: Interaction<UserFollowModel> {
                override fun onItemSelected(position: Int, item: UserFollowModel) {
                    navController.navigate(R.id.action_profileFollowFragment_to_profileFragment,
                        bundleOf(ProfileFragment.USER_ARG to item.user))
                }

                override fun onErrorRefreshPressed() {
                    followAdapter?.submitLoading()
                    setData()
                }
            })
            adapter = followAdapter

            var isScrolling=false
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    followAdapter?.let {
                        if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == it.itemCount - 1 && isScrolling && !isLoading) {
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

    private fun setObservers() {
        val liveData = when(pagerType){
            PagerType.FOLLOWING -> {
                userViewModel.setUserFollowingsObserver()
            }
            PagerType.FOLLOWER -> {
                userViewModel.setUserFollowersObserver()
            }
        }
        liveData.observe(viewLifecycleOwner){
            if (it.status == 200){
                it.data?.let { data ->
                    printLog("$data")
                    if (pageNum <= 1)
                        followAdapter?.submitList(data)
                    else {
                        isLoading = false
                        followAdapter?.updateList(data)
                    }
                } ?: followAdapter?.submitPaginationError()
            }else
                onError(it.message)
        }
    }

    private fun setData() {
        when(pagerType){
            PagerType.FOLLOWING -> {
                userViewModel.getUserFollowings(userID.toString(), pageNum, token, this)
            }
            PagerType.FOLLOWER -> {
                userViewModel.getUserFollowers(userID.toString(), pageNum, token, this)
            }
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                followAdapter?.submitError(message)

                /*context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }*/
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        followAdapter = null
    }
}