package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.utils.setToolbarBackButton
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.FeedAdapter
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel

class FeedFragment : BaseFragment<FragmentFeedBinding>() {

    private var feedAdapter: FeedAdapter? = null
    private val viewModel: FeedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setToolbarBackButton(false)

        setRecyclerView()
        setObservers()
    }

    private fun setRecyclerView() {
        binding.feedRV.apply{
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            feedAdapter = FeedAdapter(currentUser, object: Interaction<FeedModel> {
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

    private fun setObservers(){
        //TODO viewmodel
        feedAdapter?.submitList(arrayListOf())
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}