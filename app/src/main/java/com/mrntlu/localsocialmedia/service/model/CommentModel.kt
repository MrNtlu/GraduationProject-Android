package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentModel(
    val id: Int,
    val author: UserModel,
    var message: String,
    val postedDate: String,
    var updatedDate: String
): Parcelable
