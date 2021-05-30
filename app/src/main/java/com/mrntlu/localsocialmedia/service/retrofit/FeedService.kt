package com.mrntlu.localsocialmedia.service.retrofit

import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import retrofit2.http.*

interface FeedService {

    @GET("feed/{feed_id}")
    suspend fun getFeed(@Path("feed_id") feedID: String, @Query("key") token: String)

//GET FEED
    @GET("feed/user/{user_id}")
    suspend fun getUserFeed(@Path("user_id") userID: String, @Query("key") token: String): BaseResponse<List<FeedModel>>

    @GET("feed/location")
    suspend fun getFeedsByLocation(
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("distance") distance: Int,
        @Query("key") token: String
    )

    @GET("feed/follow")
    suspend fun getFeedByFollowings(@Query("key") token: String): BaseResponse<List<FeedModel>>

    @POST("create/feed")
    suspend fun postFeed(@Body body: FeedBody, @Path("feed_id") feedID: String, @Query("key") token: String)

//GET COMMENTS
    @GET("feed/{feed_id}/comments")
    suspend fun getFeedComments(@Path("feed_id") feedID: String, @Query("key") token: String)

    @POST("feed/{feed_id}/create")
    suspend fun postComment(@Body body: CommentBody, @Path("feed_id") feedID: String, @Query("key") token: String)

//HANDLE VOTE REPORT
    @POST("feed/{feed_id}/vote")
    suspend fun voteFeed(@Body body: VoteBody, @Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<Unit>

    @PUT("feed/{feed_id}/vote")
    suspend fun updateFeedVote(@Body body: VoteBody, @Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<Unit>

    @DELETE("feed/{feed_id}/vote")
    suspend fun deleteFeedVote(@Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<Unit>

    @POST("feed/{feed_id}/report")
    suspend fun reportFeed(@Path("feed_id") feedID: String, @Query("key") token: String):  BaseResponse<Unit>

    //TODO Search in all feeds?
    /*
    @GET("feedlist")
    suspend fun getAllFeeds()
     */
}