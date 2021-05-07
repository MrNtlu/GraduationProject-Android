package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user")
    suspend fun searchUser(@Query("search") search: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user/{user_id}/followers")
    suspend fun getUserFollowers(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<List<UserFollowModel>>

    @GET("user/{user_id}/followings")
    suspend fun getUserFollowings(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<List<UserFollowModel>>

    //@POST("user/{user_id}/follow")

    //Upload Image
    //Update Profile
}