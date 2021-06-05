package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.utils.*
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
        setHasOptionsMenu(true)
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
                    val bundle = Bundle()
                    bundle.putParcelable(FeedDetailsFragment.FEED_MODEL_ARG, item)
                    navController.navigate(R.id.action_feedFragment_to_feedDetailsFragment, bundle)
                }

                override fun onReportPressed(position: Int, feedModel: FeedModel) {
                    MaterialDialogUtil.setDialog(this@apply.context, getString(R.string.are_you_sure), "Do you want to REPORT?", object:
                        DialogButtons {
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.reportFeed(feedModel.id.toString(), token, this@FeedFragment).observe(viewLifecycleOwner){ response ->
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
                        voteType, viewModel, feedModel, token, feedController!!.dialogErrorHandler(context)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_menu -> {
                navController.navigate(
                    R.id.action_feedFragment_to_postFeedFragment,
                    bundleOf(PostFeedFragment.DIRECTION_ARG to 1)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter = null
        feedController = null
    }
}