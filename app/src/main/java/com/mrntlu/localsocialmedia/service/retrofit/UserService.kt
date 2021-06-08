package com.mrntlu.localsocialmedia.service.retrofit

import com.google.gson.JsonObject
import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.UserSearchResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserService {

    @GET("user/{user_id}")
    suspend fun getUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<UserModel>

    @GET("user")
    suspend fun searchUser(@Query("search") search: String, @Query("page") page: Int, @Query("key") token: String): UserSearchResponse

    @GET("user/{user_id}/followers")
    suspend fun getUserFollowers(@Path("user_id") userID: String, @Query("page") page: Int, @Query("key") token: String): BaseResponse<ArrayList<UserFollowModel>>

    @GET("user/{user_id}/followings")
    suspend fun getUserFollowings(@Path("user_id") userID: String, @Query("page") page: Int, @Query("key") token: String): BaseResponse<ArrayList<UserFollowModel>>

    @PUT("user/{user_id}/edit")
    suspend fun updateUserInfo(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<Unit>

    @POST("user/{user_id}/follow")
    suspend fun followUser(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<UserModel>

    @Multipart
    @PUT("user/{user_id}/upload")
    suspend fun uploadUserImage(
        @Path("user_id") userID: String,
        @Query("key") token: String,
        @Part part: MultipartBody.Part
    ): BaseResponse<Unit>

    @PUT("change_password/{user_id}")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Query("old_password") oldPassword: String,
        @Query("password") password: String,
        @Query("password2") rePassword: String
    ): JsonObject
}