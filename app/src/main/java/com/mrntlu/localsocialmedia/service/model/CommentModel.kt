package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentModel(
    val id: Int,
    val author: UserModel,
    var message: String,
    val postedDate: String,
    var updatedDate: String,
    val isSpam: Boolean,
    var likes: Int,
    @SerializedName("is_liked")
    var isLiked: Boolean
): Parcelable
