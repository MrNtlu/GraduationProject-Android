package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    companion object{
        fun getClient(): Retrofit{
            return Retrofit.Builder()
                    .baseUrl(Constants.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }
}