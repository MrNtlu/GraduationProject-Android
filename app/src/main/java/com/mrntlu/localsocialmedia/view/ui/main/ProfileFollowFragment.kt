package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.databinding.FragmentProfileFollowBinding
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.setToolbarBackButton
import com.mrntlu.localsocialmedia.view.adapter.viewpager.ProfileFollowPagerAdapter
import kotlin.properties.Delegates

class ProfileFollowFragment : Fragment() {

    private var _binding: FragmentProfileFollowBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var currentUser: UserModel
    private var userID by Delegates.notNull<Int>()

    companion object{
        const val USERID_ARG = "userID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userID = it.getInt(USERID_ARG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileFollowBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = (activity as MainActivity).currentUser
        (activity as MainActivity).setToolbarBackButton(true)
        navController = Navigation.findNavController(view)

        setupViewPager(view)
    }

    private fun setupViewPager(view: View) {
        val pagerAdapter = ProfileFollowPagerAdapter(view.context, childFragmentManager, userID)
        binding.profileViewPager.adapter = pagerAdapter
        binding.profileTabLayout.setupWithViewPager(binding.profileViewPager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}