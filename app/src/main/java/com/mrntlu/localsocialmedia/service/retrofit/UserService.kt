package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user")
    suspend fun searchUser(@Query("search") search: String, @Query("key") token: String): BaseResponse<UserModel>

    //Upload Image
    //Update Profile
}