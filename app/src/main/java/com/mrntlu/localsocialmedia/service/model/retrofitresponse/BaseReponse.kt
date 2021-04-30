package com.mrntlu.localsocialmedia.service.model.retrofitresponse

data class BaseReponse<T>(
        val status: Int,
        val message: String,
        val data: T?
)