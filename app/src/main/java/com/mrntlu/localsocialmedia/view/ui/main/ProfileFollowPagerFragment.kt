package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentProfileFollowPagerBinding
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.ui.authentication.AuthenticationActivity
import com.mrntlu.localsocialmedia.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.util.*

class ProfileFollowPagerFragment(private val pagerType: PagerType, private val userID: Int): Fragment(),
    CoroutinesErrorHandler {
    enum class PagerType{
        FOLLOWER,
        FOLLOWING
    }

    private var _binding: FragmentProfileFollowPagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var token: String
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileFollowPagerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        token = (activity as MainActivity).token

        setRecyclerView()
        setObservers()
    }

    private fun setRecyclerView() {
        binding.profileFollowRV.apply {

        }
    }

    private fun setObservers() {
        val liveData = when(pagerType){
            PagerType.FOLLOWING -> {
                userViewModel.getUserFollowings(userID.toString(),token, this)
            }
            PagerType.FOLLOWER -> {
                userViewModel.getUserFollowers(userID.toString(),token, this)
            }
        }
        liveData.observe(viewLifecycleOwner){
            if (it.status == 200){
                printLog(it.data!!.toTypedArray().contentToString())
            }else
                onError(it.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                //TODO Recyclerview should be notified

                context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }
            }
        }
    }
}