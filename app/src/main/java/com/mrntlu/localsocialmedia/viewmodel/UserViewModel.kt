package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient
import com.mrntlu.localsocialmedia.service.retrofit.UserService
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler

class UserViewModel(application: Application): BaseViewModel(application) {

    private val apiClient = RetrofitClient.getClient().create(UserService::class.java)

    fun getUserInfo(userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getUserInfo(userID, token)
    }

    fun searchUser(search: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.searchUser(search, token)
    }
}