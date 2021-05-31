package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.CommentBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.FeedBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.service.retrofit.FeedService
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import retrofit2.http.Path
import retrofit2.http.Query

class FeedViewModel(application: Application): BaseViewModel(application) {

    private val apiClient = RetrofitClient.getClient().create(FeedService::class.java)
    private lateinit var userFeedLiveData: MutableLiveData<BaseResponse<ArrayList<FeedModel>>>
    private lateinit var feedFollowingLiveData: MutableLiveData<BaseResponse<ArrayList<FeedModel>>>
    private lateinit var feedLocationLiveData: MutableLiveData<BaseResponse<ArrayList<FeedModel>>>

    fun setUserFeedObserver(): LiveData<BaseResponse<ArrayList<FeedModel>>> {
        userFeedLiveData = MutableLiveData()
        return userFeedLiveData
    }

    fun getUserFeed(userID: String, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(userFeedLiveData, coroutinesErrorHandler){
            apiClient.getUserFeed(userID, page, token)
        }

    fun setFeedByFollowings(): LiveData<BaseResponse<ArrayList<FeedModel>>> {
        feedFollowingLiveData = MutableLiveData()
        return feedFollowingLiveData
    }

    fun getFeedByFollowings(page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(feedFollowingLiveData, coroutinesErrorHandler){
            apiClient.getFeedByFollowings(page, token)
        }

    fun setFeedByLocation(): LiveData<BaseResponse<ArrayList<FeedModel>>> {
        feedLocationLiveData = MutableLiveData()
        return feedLocationLiveData
    }

    fun getFeedsByLocation(latitude: Float, longitude: Float, distance: Int, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(feedLocationLiveData, coroutinesErrorHandler){
            apiClient.getFeedsByLocation(latitude, longitude, distance, page, token)
        }

    fun postFeed(body: FeedBody, userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.postFeed(body, userID, token)
    }

    fun getFeedComments(feedID: String, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getFeedComments(feedID, page, token)
    }

    fun postComment(body: CommentBody, feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.postComment(body, feedID, token)
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

    fun reportFeed(feedID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.reportFeed(feedID, token)
    }
}