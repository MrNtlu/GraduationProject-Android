package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.ActivityMainBinding
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.setGone
import com.mrntlu.localsocialmedia.utils.setVisible
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.ui.authentication.AuthenticationActivity
import com.mrntlu.localsocialmedia.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutinesErrorHandler {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val userViewModel: UserViewModel by viewModels()

    companion object{
        const val TOKEN_ARG = "token"
        const val ID_ARG = "id"

        lateinit var token: String
        lateinit var userID: String
        lateinit var currentUser: UserModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = (supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment).navController

        val i = intent
        val extras = i.extras
        if (extras != null) {
            token = extras.getString(TOKEN_ARG, "")
            userID = extras.getString(ID_ARG, "")
        }

        binding.loadingLayout.root.setVisible()
        setActionBar()
        userViewModel.getUserInfo(userID, token, this).observe(this){
            binding.loadingLayout.root.setGone()
            if (it.status == 200 && it.data != null){
                currentUser = it.data
                NavigationUI.setupWithNavController(binding.mainBottomNav, navController)
            }else
                onError(it.message)
        }
    }

    private fun setActionBar(){
        setSupportActionBar(binding.mainToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true) //false for image
            supportActionBar!!.title = null
        }

        binding.mainToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onError(message: String) {
        lifecycleScope.launch {
            whenResumed {
                MaterialDialogUtil.showErrorDialog(baseContext, message)
                //TODO Signout
            }
        }
    }
}