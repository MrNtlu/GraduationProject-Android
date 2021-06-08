package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrntlu.localsocialmedia.databinding.FragmentProfileFollowBinding
import com.mrntlu.localsocialmedia.utils.setToolbarBackButton
import com.mrntlu.localsocialmedia.view.adapter.viewpager.ProfileFollowPagerAdapter
import kotlin.properties.Delegates

class ProfileFollowFragment : BaseFragment<FragmentProfileFollowBinding>() {

    private var userID by Delegates.notNull<Int>()
    private var isFollowing = false

    companion object{
        const val USERID_ARG = "userID"
        const val IS_FOLLOWING_ARG = "isFollowing"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userID = it.getInt(USERID_ARG)
            isFollowing = it.getBoolean(IS_FOLLOWING_ARG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileFollowBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(true)

        setupViewPager(view)
    }

    private fun setupViewPager(view: View) {
        val pagerAdapter = ProfileFollowPagerAdapter(view.context, childFragmentManager, userID)
        binding.profileViewPager.adapter = pagerAdapter
        binding.profileTabLayout.setupWithViewPager(binding.profileViewPager)
        if (isFollowing) {
            val tab = binding.profileTabLayout.getTabAt(1)
            tab?.select()
        }
    }
}