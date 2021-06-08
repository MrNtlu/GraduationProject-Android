package com.mrntlu.localsocialmedia.service.retrofit

import android.net.Uri
import com.mrntlu.localsocialmedia.service.model.CommentModel
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface FeedService {

    @GET("feed/{feed_id}")
    suspend fun getFeed(@Path("feed_id") feedID: String, @Query("key") token: String)

//GET FEED
    @GET("feed/user/{user_id}")
    suspend fun getUserFeed(@Path("user_id") userID: String, @Query("page") page: Int, @Query("key") token: String): BaseResponse<ArrayList<FeedModel>>

    @DELETE("feed/{feed_id}/delete")
    suspend fun deleteFeed(@Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<Unit>

    @GET("feed/location")
    suspend fun getFeedsByLocation(
        @Query("lat") latitude: Float,
        @Query("long") longitude: Float,
        @Query("distance") distance: Int,
        @Query("page") page: Int,
        @Query("key") token: String
    ): BaseResponse<ArrayList<FeedModel>>

    @GET("feed/follow")
    suspend fun getFeedByFollowings(@Query("page") page: Int, @Query("key") token: String): BaseResponse<ArrayList<FeedModel>>

    @Multipart
    @POST("create/feed")
    suspend fun postFeed(
        @Part("message") message: RequestBody,
        @Part("type") type: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("locationName") locationName: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>?,
        @Query("key") token: String): BaseResponse<FeedModel>

//HANDLE VOTE REPORT
    @POST("feed/{feed_id}/vote")
    suspend fun voteFeed(@Body body: VoteBody, @Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<FeedModel>

    @PUT("feed/{feed_id}/vote")
    suspend fun updateFeedVote(@Body body: VoteBody, @Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<FeedModel>

    @DELETE("feed/{feed_id}/vote")
    suspend fun deleteFeedVote(@Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<FeedModel>

    @POST("feed/{feed_id}/report")
    suspend fun reportFeed(@Path("feed_id") feedID: String, @Query("key") token: String):  BaseResponse<Unit>

//GET COMMENTS
    @GET("feed/{feed_id}/comments")
    suspend fun getFeedComments(@Path("feed_id") feedID: String, @Query("page") page: Int, @Query("sort") sort: String, @Query("key") token: String): BaseResponse<ArrayList<CommentModel>>

    @DELETE("comment/{comment_id}/delete")
    suspend fun deleteComment(@Path("comment_id") commentID: String, @Query("key") token: String): BaseResponse<Unit>

    @POST("feed/{feed_id}/create")
    suspend fun postComment(@Body body: CommentBody, @Path("feed_id") feedID: String, @Query("key") token: String): BaseResponse<CommentModel>

    @POST("comment/{comment_id}/like")
    suspend fun likeComment(@Path("comment_id") commentID: String, @Query("key") token: String): BaseResponse<CommentModel>

    @DELETE("comment/{comment_id}/like")
    suspend fun deleteLikeComment(@Path("comment_id") commentID: String, @Query("key") token: String): BaseResponse<CommentModel>

    @POST("comment/{comment_id}/report")
    suspend fun reportComment(@Path("comment_id") commentID: String, @Query("key") token: String): BaseResponse<Unit>

    //TODO Search in all feeds?
    /*
    @GET("feedlist")
    suspend fun getAllFeeds()
     */
}