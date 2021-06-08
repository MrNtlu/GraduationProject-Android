package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.UserSearchResponse
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient
import com.mrntlu.localsocialmedia.service.retrofit.UserService
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Query

class UserViewModel(application: Application): BaseViewModel(application) {

    private val apiClient = RetrofitClient.getClient().create(UserService::class.java)
    private lateinit var followingsLiveData: MutableLiveData<BaseResponse<ArrayList<UserFollowModel>>>
    private lateinit var followersLiveData: MutableLiveData<BaseResponse<ArrayList<UserFollowModel>>>
    private lateinit var userLiveData: MutableLiveData<UserSearchResponse>

    fun getUserInfo(userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.getUserInfo(userID, token)
    }

    fun searchUserObserver(): LiveData<UserSearchResponse> {
        userLiveData = MutableLiveData()
        return userLiveData
    }

    fun searchUser(search: String, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(userLiveData, coroutinesErrorHandler){
            apiClient.searchUser(search, page, token)
        }

    fun setUserFollowingsObserver(): LiveData<BaseResponse<ArrayList<UserFollowModel>>> {
        followingsLiveData = MutableLiveData()
        return followingsLiveData
    }

    fun getUserFollowings(userID: String, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(followingsLiveData, coroutinesErrorHandler){
            apiClient.getUserFollowings(userID, page, token)
        }

    fun setUserFollowersObserver(): LiveData<BaseResponse<ArrayList<UserFollowModel>>> {
        followersLiveData = MutableLiveData()
        return followersLiveData
    }

    fun getUserFollowers(userID: String, page: Int, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) =
        basePaginationRequest(followersLiveData, coroutinesErrorHandler){
            apiClient.getUserFollowers(userID, page, token)
        }

    fun followUser(userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.followUser(userID, token)
    }

    fun uploadUserImage(userID: String, token: String, part: MultipartBody.Part, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.uploadUserImage(userID, token, part)
    }

    fun updateUserInfo(userID: String, token: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.updateUserInfo(userID, token)
    }

    fun changePassword(token: String, oldPassword: String, password: String, rePassword: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.changePassword(token, oldPassword, password, rePassword)
    }
}