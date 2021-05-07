package com.mrntlu.localsocialmedia.view.ui.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentProfileBinding
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var currentUser: UserModel
    private lateinit var userModel: UserModel
    private var isCurrentUser = false

    companion object{
        const val USER_ARG = "user"
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
        currentUser = (activity as MainActivity).currentUser
        (activity as MainActivity).setToolbarBackButton(false)
        navController = Navigation.findNavController(view)
        isCurrentUser = !::userModel.isInitialized

        setUI(view)
        setListeners()
    }

    private fun setUI(view: View){
        binding.profileEditButton.shouldVisible(isCurrentUser)
        val userProfile = if (isCurrentUser) currentUser else userModel
        userProfile.apply {
            if (image != null) {
                binding.profileImageProgress.setVisible()
                Glide.with(view.context).load(image).addListener(object: RequestListener<Drawable>{
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
    private fun setListeners(){
        binding.profileEditButton.setOnClickListener {
            printLog("Edit")
        }

        binding.profileFollowerLayout.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_profileFollowFragment, bundleOf(
                ProfileFollowFragment.USERID_ARG to (if (isCurrentUser) currentUser.id else userModel.id)
            ))
        }

        binding.profileFollowingLayout.setOnClickListener {
            navController.navigate(R.id.action_profileFragment_to_profileFollowFragment, bundleOf(
                ProfileFollowFragment.USERID_ARG to (if (isCurrentUser) currentUser.id else userModel.id)
            ))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}