package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitbody.LoginBody
import com.mrntlu.localsocialmedia.service.model.retrofitbody.RegisterBody
import com.mrntlu.localsocialmedia.service.model.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.service.model.retrofitresponse.LoginResponse
import retrofit2.http.*

interface AuthenticationService {

    @POST("login")
    suspend fun loginUser(@Body body: LoginBody): BaseResponse<LoginResponse>

    @POST("register")
    suspend fun registerUser(@Body body: RegisterBody): BaseResponse<RegisterBody>

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String): BaseResponse<UserModel>
}