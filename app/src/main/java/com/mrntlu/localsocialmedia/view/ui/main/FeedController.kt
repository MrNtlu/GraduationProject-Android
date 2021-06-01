package com.mrntlu.localsocialmedia.view.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed.VoteBody
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse.BaseResponse
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.viewmodel.FeedViewModel

class FeedController {

    fun dialogErrorHandler(context: Context?) = object: CoroutinesErrorHandler{
        override fun onError(message: String) {
            context?.let {
                MaterialDialogUtil.showErrorDialog(it, message)
            }
        }
    }

    fun voteClickHandler(voteType: VoteType, viewModel: FeedViewModel,
                         feedModel: FeedModel, token: String,
                         errorHandler: CoroutinesErrorHandler): LiveData<BaseResponse<FeedModel>> {
        return if (feedModel.userVote.isVoted){
            if (feedModel.userVote.voteType == voteType){
                viewModel.deleteFeedVote(feedModel.id.toString(), token, errorHandler)
            }else {
                viewModel.updateFeedVote(VoteBody(voteType.num), feedModel.id.toString(), token, errorHandler)
            }
        }else{
            viewModel.voteFeed(VoteBody(voteType.num), feedModel.id.toString(), token, errorHandler)
        }
    }
}