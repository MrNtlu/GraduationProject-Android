package com.mrntlu.localsocialmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mrntlu.localsocialmedia.utils.Constants
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import kotlinx.coroutines.*

open class BaseViewModel(application: Application): AndroidViewModel(application) {

    private var mJob: Job? = null

    protected fun <T> baseRequest(errorHandler: CoroutinesErrorHandler, request:suspend () -> T): LiveData<T> {
        val liveData = MutableLiveData<T>()

        mJob = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, error ->
            errorHandler.onError(error.localizedMessage ?: "Error occured! Please try again.")
        }){
            var response: T? = null
            val job = withTimeoutOrNull(Constants.TIME_OUT){
                response = request()
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
    }

    protected fun <T> basePaginationRequest(liveData: MutableLiveData<T> ,errorHandler: CoroutinesErrorHandler, request:suspend () -> T){
        mJob = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, error ->
            errorHandler.onError(error.localizedMessage ?: "Error occured! Please try again.")
        }){
            var response: T? = null
            val job = withTimeoutOrNull(Constants.TIME_OUT){
                response = request()
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
    }

    override fun onCleared() {
        super.onCleared()
        mJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
    }
}