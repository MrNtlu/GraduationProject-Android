package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserService {

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user")
    suspend fun searchUser(@Query("search") search: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user/{user_id}/followers")
    suspend fun getUserFollowers(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<List<UserFollowModel>>

    @GET("user/{user_id}/followings")
    suspend fun getUserFollowings(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<List<UserFollowModel>>

    @PUT("user/{user_id}/edit")
    suspend fun updateUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<Unit>

    @Multipart
    @PUT("user/{user_id}/upload")
    suspend fun uploadUserImage(
        @Path("user_id") userID: String,
        @Query("key") token: String,
        @Part part: MultipartBody.Part
    ): BaseResponse<Unit>
}