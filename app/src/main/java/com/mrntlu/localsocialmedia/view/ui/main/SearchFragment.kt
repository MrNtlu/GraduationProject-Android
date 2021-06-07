package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentSearchBinding
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment<FragmentSearchBinding>(), CoroutinesErrorHandler {

    private val viewModel: UserViewModel by viewModels()
    private var isLoading = false
    private var pageNum = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setListeners()
    }

    private fun setRecyclerView() {
        binding.searchRV.apply {

        }
    }

    private fun setListeners() {
        binding.button.setOnClickListener {
            viewModel.searchUser(binding.searchView.query.toString(), pageNum, token, this)
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}