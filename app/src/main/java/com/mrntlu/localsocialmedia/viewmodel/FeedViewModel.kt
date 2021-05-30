package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.service.retrofit.FeedService
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import retrofit2.http.Path
import retrofit2.http.Query

class FeedViewModel(application: Application): BaseViewModel(application) {

    private val apiClient = RetrofitClient.getClient().create(FeedService::class.java)

    fun getUserFeed(userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getUserFeed(userID, token)
    }

    fun postFeed(body: FeedBody, userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.postFeed(body, userID, token)
    }

    fun getFeedComments(feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getFeedComments(feedID, token)
    }

    fun postComment(body: CommentBody, feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.postComment(body, feedID, token)
    }

    fun getFeedsByLocation(latitude: Float, longitude: Float, distance: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getFeedsByLocation(latitude, longitude, distance, token)
    }

    fun voteFeed(body: VoteBody, feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.voteFeed(body, feedID, token)
    }

    fun updateFeedVote(body: VoteBody, feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.updateFeedVote(body, feedID, token)
    }

    fun deleteFeedVote(feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.deleteFeedVote(feedID, token)
    }

    fun getFeedByFollowings(token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getFeedByFollowings(token)
    }

    fun reportFeed(feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.reportFeed(feedID, token)
    }
}