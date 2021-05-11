package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import com.mrntlu.localsocialmedia.service.retrofit.FeedService
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient

class FeedViewModel(application: Application): BaseViewModel(application) {

    private val apiClient = RetrofitClient.getClient().create(FeedService::class.java)


}