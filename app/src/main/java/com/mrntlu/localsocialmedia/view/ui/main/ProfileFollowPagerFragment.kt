
package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileFollowPagerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setObservers()
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
                    //TODO("Not yet implemented")
                    printLog("Error pressed.")
                }

            })
            adapter = followAdapter
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
            if (it.status == 200 && it.data != null){
                printLog("${it.data}")
                followAdapter?.submitList(it.data)
            }else
                onError(it.message)
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