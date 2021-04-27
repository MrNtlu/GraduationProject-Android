package com.mrntlu.localsocialmedia.view.ui.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}