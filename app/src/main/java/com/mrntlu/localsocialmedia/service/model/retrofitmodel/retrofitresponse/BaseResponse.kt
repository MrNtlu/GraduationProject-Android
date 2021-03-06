package com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse

data class BaseResponse<T>(
        val status: Int,
        val message: String,
        val data: T?
)