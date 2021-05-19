package com.mrntlu.localsocialmedia.view.ui.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentProfileBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.*
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.FeedAdapter
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

class ProfileFragment : BaseFragment<FragmentProfileBinding>(), CoroutinesErrorHandler{

    private lateinit var userModel: UserModel
    private val viewModel: FeedViewModel by viewModels()
    private var feedAdapter: FeedAdapter? = null
    private var isCurrentUser = false

    private val displayUser get() =
        if (isCurrentUser)
            currentUser
        else
            userModel

    companion object{
        const val USER_ARG = "user"
        const val USER_ID_ARG = "userID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.getParcelable<UserModel>(USER_ARG) != null) {
                userModel = it.getParcelable(USER_ARG)!!
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCurrentUser = !::userModel.isInitialized
        (activity as MainActivity).setToolbarBackButton(!isCurrentUser)

        setUI(view)
        setRecyclerView()
        setListeners()
        setObservers()
    }

    private fun setUI(view: View){
        setHasOptionsMenu(isCurrentUser)

        displayUser.apply {
            if (imageUri != null) {
                binding.profileImageProgress.setVisible()
                Glide.with(view.context).load(imageUri).addListener(object: RequestListener<Drawable>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        if (_binding != null)
                            binding.profileImageProgress.setGone()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        if (_binding != null)
                            binding.profileImageProgress.setGone()
                        return false
                    }

                }).placeholder(ResourcesCompat.getDrawable(resources,R.drawable.ic_account_126,null)).into(binding.profileImageView)
            }else
                binding.profileImageView.setImageResource(R.drawable.ic_account_126)
            binding.profileNameText.text = name
            val usernameText = "@$username"
            binding.profileUsernameText.text = usernameText
            binding.profilePostText.text = postCount.toString()
            binding.profileFollowingText.text = followingCount.toString()
            binding.profileFollowerText.text = followerCount.toString()
        }
    }

    private fun setRecyclerView() {
        binding.profileFeedRV.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            feedAdapter = FeedAdapter(currentUser, object: Interaction<FeedModel>{
                override fun onItemSelected(position: Int, item: FeedModel) {
                    printLog("Feed item clicked $item")
                }

                override fun onErrorRefreshPressed() {
                    //TODO("Not yet implemented")
                    printLog("Error pressed on Feed")
                }
            })
            adapter = feedAdapter
        }
    }

    private fun setListeners(){
        binding.profileFollowerLayout.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_profileFollowFragment, bundleOf(
                ProfileFollowFragment.USERID_ARG to (displayUser.id)
            ))
        }

        binding.profileFollowingLayout.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_profileFollowFragment, bundleOf(
                ProfileFollowFragment.USERID_ARG to (displayUser.id)
            ))
        }

        binding.profileSwipeRefresh.setOnRefreshListener {
            feedAdapter?.submitLoading()
            setObservers()
            binding.profileSwipeRefresh.isRefreshing = false
        }
    }

    private fun setObservers(){
        viewModel.getUserFeed(displayUser.id.toString(), token, this).observe(viewLifecycleOwner){
            if (it.status == 200 && it.data != null){
                feedAdapter?.submitList(it.data)
            }else
                onError(it.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.edit_profile -> {
                printLog("Edit pressed")
                true
                //navController.navigate(R.id., bundleOf("user" to currentUser))
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                feedAdapter?.submitError(message)
                /*context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }*/
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter = null
    }
}