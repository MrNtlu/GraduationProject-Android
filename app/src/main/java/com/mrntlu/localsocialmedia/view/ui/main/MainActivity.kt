package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrntlu.localsocialmedia.databinding.ActivityMainBinding
import com.mrntlu.localsocialmedia.utils.printLog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var token: String
    private lateinit var userID: String

    companion object{
        const val TOKEN_ARG = "token"
        const val ID_ARG = "id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val i = intent
        val extras = i.extras
        if (extras != null) {
            token = extras.getString(TOKEN_ARG,"")
            userID = extras.getString(ID_ARG,"")
        }

        binding.textView3.text = "Token: $token\nID: $userID"
    }
}