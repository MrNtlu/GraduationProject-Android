package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FeedModel(
    val id: Int,
    val author: UserModel,
    var message: String,
    private val type: String,
    val postedDate: String,
    var updatedDate: String,
    val latitude: Float,
    val longitude: Float,
    val locationName: String?,
    val images: List<String>,
    val votes: List<FeedVoteModel>,
    @SerializedName("upvote_count")
    var upvoteCount: Int,
    @SerializedName("downvote_count")
    var downvoteCount: Int,
): Parcelable{

    val feedType get() = FeedType.valueOf(type)

}