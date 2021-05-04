package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.authentication.LoginBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.authentication.RegisterBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.LoginResponse
import retrofit2.http.*

interface AuthenticationService {

    @POST("login")
    suspend fun loginUser(@Body body: LoginBody): BaseResponse<LoginResponse>

    @POST("register")
    suspend fun registerUser(@Body body: RegisterBody): BaseResponse<RegisterBody>
}