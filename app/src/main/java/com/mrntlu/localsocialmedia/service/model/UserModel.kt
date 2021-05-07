package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(
        val id: Int,
        val image: String?,
        val email: String,
        val username: String,
        var name: String,
        @SerializedName("follower_count")
        var followerCount: Int,
        @SerializedName("following_count")
        var followingCount: Int,
        @SerializedName("post_count")
        var postCount: Int
): Parcelable