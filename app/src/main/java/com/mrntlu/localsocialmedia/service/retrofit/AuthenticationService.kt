package com.mrntlu.localsocialmedia.service.retrofit

import com.google.gson.JsonObject
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitbody.LoginBody
import com.mrntlu.localsocialmedia.service.model.retrofitbody.RegisterBody
import com.mrntlu.localsocialmedia.service.model.retrofitresponse.BaseReponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthenticationService {

    @POST("login")
    suspend fun loginUser(@Body body: LoginBody)

    @POST("register")
    suspend fun registerUser(@Body body: RegisterBody): BaseReponse<JsonObject>

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String): BaseReponse<UserModel>
}