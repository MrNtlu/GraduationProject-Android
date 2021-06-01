package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.databinding.FragmentFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.utils.Constants
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.utils.setToolbarBackButton
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.adapter.FeedAdapter
import com.mrntlu.localsocialmedia.view.adapter.FeedInteraction
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

class FeedFragment : BaseFragment<FragmentFeedBinding>(), CoroutinesErrorHandler {

    private var feedAdapter: FeedAdapter? = null
    private var feedController: FeedController? = null
    private val viewModel: FeedViewModel by viewModels()

    private var isLoading = false
    private var pageNum = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(false)

        setRecyclerView()
        setObservers()
        setData()
    }

    private fun setRecyclerView() {
        binding.feedRV.apply{
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            feedAdapter = FeedAdapter(currentUser, object: FeedInteraction {
                override fun onItemSelected(position: Int, item: FeedModel) {
                    printLog("Feed item clicked $item")
                }

                override fun onReportPressed(position: Int, feedModel: FeedModel) {
                    TODO("Not yet implemented")
                }

                override fun onVotePressed(voteType: VoteType, position: Int, feedModel: FeedModel) {
                    val observer = feedController?.voteClickHandler(
                        voteType, viewModel, feedModel, token, feedController!!.dialogErrorHandler(context)
                    )

                    observer?.observe(viewLifecycleOwner){ response ->
                        if (response.status == 200 && response.data != null){
                            feedAdapter?.updateItem(position, response.data)
                        }
                        observer.removeObservers(viewLifecycleOwner)
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
        viewModel.setFeedByFollowings().observe(viewLifecycleOwner){
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
        viewModel.getFeedByFollowings(pageNum, token, this)
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                feedAdapter?.submitError(message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter = null
        feedController = null
    }
}