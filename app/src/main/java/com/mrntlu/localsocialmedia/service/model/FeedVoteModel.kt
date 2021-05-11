package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedVoteModel(
    val user: UserModel,
    private val vote: String
): Parcelable{

    val voteType get() = VoteType.valueOf(vote)
}
