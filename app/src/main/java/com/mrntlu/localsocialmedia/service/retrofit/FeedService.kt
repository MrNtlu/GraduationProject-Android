package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import retrofit2.http.*

interface FeedService {

    @GET("feedlist")
    suspend fun getAllFeeds()

    @GET("feed/{feed_id}")
    suspend fun getFeed(@Path("feed_id") feedID: String, @Query("key") token: String)

    @GET("feed/user/{user_id}")
    suspend fun getUserFeed(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<FeedModel>

    @POST("create/feed")
    suspend fun postFeed(@Body body: FeedBody, @Path("feed_id") feedID: String, @Query("key") token: String)

    @GET("feed/{feed_id}/comments")
    suspend fun getFeedComments(@Path("feed_id") feedID: String, @Query("key") token: String)

    @POST("feed/{feed_id}/create")
    suspend fun postComment(@Body body: CommentBody, @Path("feed_id") feedID: String, @Query("key") token: String)

    @GET("feed/location")
    suspend fun getFeedsByLocation(
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("distance") distance: Int,
        @Query("key") token: String
    )

    //Feed by following
}