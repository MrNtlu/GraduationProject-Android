package com.mrntlu.localsocialmedia.view.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFeedBinding
import com.mrntlu.localsocialmedia.databinding.FragmentFeedDetailsBinding
import com.mrntlu.localsocialmedia.service.model.CommentModel
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.utils.*
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.adapter.CommentAdapter
import com.mrntlu.localsocialmedia.view.adapter.CommentInteraction
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

class FeedDetailsFragment : BaseFragment<FragmentFeedDetailsBinding>(), CoroutinesErrorHandler {
    private enum class SortType(val sort: String){
        LIKE_DESC("-like"),
        DATE_ASC("postedDate"),
        DATE_DESC("-postedDate")
    }

    private val viewModel: FeedViewModel by viewModels()
    private var isLoading = false
    private var pageNum = 1
    private var sortType = SortType.DATE_DESC
    private var commentAdapter: CommentAdapter? = null
    private var feedController: FeedController? = null

    private lateinit var feedModel: FeedModel
    private var _feedBinding: CellFeedBinding? = null
    private val feedBinding get() = _feedBinding!!

    companion object{
        const val FEED_ID_ARG = "feedID"
        const val FEED_MODEL_ARG = "feedModel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getParcelable<FeedModel>(FEED_MODEL_ARG)?.let { feed ->
                feedModel = feed
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedDetailsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(true)
        feedController = FeedController()
        _feedBinding = binding.commentFeedDetailLayout

        setUI(view.context)
        setRecyclerView()
        setObservers()
        setData()
        setListeners()
    }

    private fun setUI(context: Context) {
        feedModel.apply {
            feedBinding.setUI(this, true)
            feedBinding.setVoteUI(context, userVote)
        }
    }

    private fun setRecyclerView(){
        binding.commentRV.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            commentAdapter = CommentAdapter(currentUser, object: CommentInteraction{
                override fun onItemSelected(position: Int, item: CommentModel) {}

                override fun onFavPressed(position: Int, commentModel: CommentModel) {
                    val observer = if (commentModel.isLiked)
                        viewModel.deleteLikeComment(commentModel.id.toString(), token, feedController!!.dialogErrorHandler(context))
                    else
                        viewModel.likeComment(commentModel.id.toString(), token, feedController!!.dialogErrorHandler(context))

                    observer.observe(viewLifecycleOwner){ response ->
                        if (response.status == 200 && response.data != null){
                            commentAdapter?.updateItem(position, response.data)
                        }else
                            feedController?.dialogErrorHandler(context)?.onError(response.message)
                        observer.removeObservers(viewLifecycleOwner)
                    }
                }

                override fun onReportPressed(position: Int, commentModel: CommentModel) {
                    MaterialDialogUtil.setDialog(this@apply.context, getString(R.string.are_you_sure), "Do you want to REPORT?", object: DialogButtons{
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.reportComment(commentModel.id.toString(), token, feedController!!.dialogErrorHandler(context)).observe(viewLifecycleOwner){ response ->
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

                override fun onDeletePressed(position: Int, commentModel: CommentModel) {
                    MaterialDialogUtil.setDialog(this@apply.context, getString(R.string.are_you_sure), "Do you want to DELETE?", object: DialogButtons{
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.deleteComment(commentModel.id.toString(), token, feedController!!.dialogErrorHandler(context)).observe(viewLifecycleOwner){ response ->
                                if (response.status == 200) {
                                    (activity as MainActivity).setLoadingLayout(false)
                                    commentAdapter?.removeItem(position, commentModel)
                                }else
                                    MaterialDialogUtil.showErrorDialog(context, response.message)
                            }
                        }
                    })
                }

                override fun onErrorRefreshPressed() {
                    commentAdapter?.submitLoading()
                    setData()
                }
            })
            adapter = commentAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    commentAdapter?.let {
                        if (it.itemCount % Constants.COMMENT_PAGINATION_LIMIT == 0 &&
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
        viewModel.setCommentsObserver().observe(viewLifecycleOwner){
            if (it.status == 200){
                it.data?.let { data ->
                    if (pageNum <= 1)
                        commentAdapter?.submitList(data)
                    else {
                        isLoading = false
                        commentAdapter?.updateList(data)
                    }
                } ?: commentAdapter?.submitPaginationError()
            }else
                onError(it.message)
        }
    }

    private fun setData(){
        viewModel.getFeedComments(feedModel.id.toString(), pageNum, sortType.sort, token,this)
    }

    private fun setListeners(){
        feedBinding.feedAuthorImage.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(ProfileFragment.USER_ARG, feedModel.author)
            navController.navigate(R.id.action_feedDetailsFragment_to_profileFragment, bundle)
        }

        feedBinding.feedAuthorLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(ProfileFragment.USER_ARG, feedModel.author)
            navController.navigate(R.id.action_feedDetailsFragment_to_profileFragment, bundle)
        }

        feedBinding.feedUpVoteButton.setOnClickListener {
            userVoteClickHandler(it.context, VoteType.UpVote)
        }

        feedBinding.feedDownVoteButton.setOnClickListener {
            userVoteClickHandler(it.context, VoteType.DownVote)
        }

        feedBinding.feedMoreButton.setOnClickListener {
            val popup = PopupMenu(it.context, it)
            popup.inflate(
                if (currentUser.id == feedModel.author.id)
                    R.menu.delete_menu
                else
                    R.menu.report_menu
            )
            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.reportMenu) {
                    (activity as MainActivity).setLoadingLayout(true)
                    viewModel.reportFeed(feedModel.id.toString(), token, feedController!!.dialogErrorHandler(it.context)).observe(viewLifecycleOwner){ response ->
                        (activity as MainActivity).setLoadingLayout(false)
                        MaterialDialogUtil.showInfoDialog(
                            it.context,
                            if (response.status == 200) "Success" else "Error!",
                            if (response.status == 200) "Thanks for reporting, we will review it as soon as possible." else response.message
                        )
                    }
                }else if (menuItem.itemId == R.id.deleteMenu) {
                    MaterialDialogUtil.setDialog(it.context, getString(R.string.are_you_sure), "Do you want to DELETE?", object: DialogButtons{
                        override fun positiveButton() {
                            (activity as MainActivity).setLoadingLayout(true)
                            viewModel.deleteFeed(feedModel.id.toString(), token, feedController!!.dialogErrorHandler(context)).observe(viewLifecycleOwner){ response ->
                                if (response.status == 200) {
                                    (activity as MainActivity).setLoadingLayout(false)
                                    navController.popBackStack()
                                }else
                                    MaterialDialogUtil.showErrorDialog(it.context, response.message)
                            }
                        }
                    })
                }
                true
            }
            popup.show()
        }

        binding.commentPostButton.setOnClickListener {
            if (binding.commentEditText.text.toString().isNotEmptyOrBlank()){
                val comment = binding.commentEditText.text.toString()
                binding.commentEditText.text = null
                viewModel.postComment(
                    CommentBody(comment), feedModel.id.toString(),
                    token, feedController!!.dialogErrorHandler(it.context)
                ).observe(viewLifecycleOwner){ response ->
                    if (response.status == 200){
                        if (sortType == SortType.DATE_DESC){
                            if (response.data != null)
                                commentAdapter?.updateList(arrayListOf(response.data))
                            else{
                                commentAdapter?.submitLoading()
                                pageNum = 1
                                setData()
                            }
                        }else{
                            commentAdapter?.submitLoading()
                            pageNum = 1
                            setData()
                        }
                    }else
                        MaterialDialogUtil.showErrorDialog(it.context, response.message)
                }
            }else {
                MaterialDialogUtil.showErrorDialog(it.context, "Please write something.")
            }
        }

        binding.commentSwipeRefresh.setOnRefreshListener {
            pageNum = 1
            commentAdapter?.submitLoading()
            setData()
            binding.commentSwipeRefresh.isRefreshing = false
        }
    }

    private fun userVoteClickHandler(context: Context, voteType: VoteType){
        val observer = feedController?.voteClickHandler(
            voteType, viewModel, feedModel, token, feedController!!.dialogErrorHandler(context)
        )

        observer?.observe(viewLifecycleOwner){ response ->
            if (response.status == 200 && response.data != null){
                feedModel = response.data
                feedBinding.setVoteUI(context, feedModel.userVote)
                feedBinding.feedVoteText.text = (feedModel.upvoteCount - feedModel.downvoteCount).toString()
            }
            observer.removeObservers(viewLifecycleOwner)
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                commentAdapter?.submitError(message)
            }
        }
    }

    override fun onDestroyView() {
        feedController = null
        commentAdapter = null
        _feedBinding = null
        super.onDestroyView()
    }
}