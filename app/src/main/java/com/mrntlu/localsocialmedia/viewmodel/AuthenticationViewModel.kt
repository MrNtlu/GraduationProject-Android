package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import com.mrntlu.localsocialmedia.service.model.retrofitbody.LoginBody
import com.mrntlu.localsocialmedia.service.model.retrofitbody.RegisterBody
import com.mrntlu.localsocialmedia.service.retrofit.AuthenticationService
import com.mrntlu.localsocialmedia.service.retrofit.RetrofitClient
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler

class AuthenticationViewModel(application: Application): BaseViewModel(application) {
    private val apiClient = RetrofitClient.getClient().create(AuthenticationService::class.java)

    fun loginUser(body: LoginBody, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.loginUser(body)
    }

    fun registerUser(body: RegisterBody, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(coroutinesErrorHandler){
        apiClient.registerUser(body)
    }

    /*fun loginUser(body: LoginBody, errorHandler: CoroutinesErrorHandler): LiveData<BaseResponse<LoginResponse>>{
        val liveData = MutableLiveData<BaseResponse<LoginResponse>>()

        mJob = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            errorHandler.onError(throwable.localizedMessage ?: "Error occured! Please try again.")
        }){
            var response: BaseResponse<LoginResponse>? = null
            val job = withTimeoutOrNull(Constants.TIME_OUT){
                response = apiClient.loginUser(body)
            }
            withContext(Dispatchers.Main){
                if (job == null){
                    errorHandler.onError("Timeout! Please try again.")
                }else{
                    response?.let {
                        liveData.value = it
                    }
                }
            }
        }

        return liveData
    }*/
}