package com.mrntlu.localsocialmedia.view.ui.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.ActivityAuthenticationBinding
import com.mrntlu.localsocialmedia.service.UserManager
import com.mrntlu.localsocialmedia.utils.shouldVisible

class AuthenticationActivity : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private lateinit var _userManager: UserManager
    val userManager get() = _userManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _userManager = UserManager(this)
        navController = (supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment).navController
    }

    fun setLoadingVisibility(shouldBeVisible: Boolean){
        binding.loadingLayout.root.shouldVisible(shouldBeVisible)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}