package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.service.model.UserModel

abstract class BaseFragment<T>: Fragment() {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    protected lateinit var navController: NavController
    protected lateinit var currentUser: UserModel
    protected lateinit var token: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = (activity as MainActivity).currentUser
        token = (activity as MainActivity).token
        navController = Navigation.findNavController(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}