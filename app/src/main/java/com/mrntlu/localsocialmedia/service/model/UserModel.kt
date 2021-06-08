package com.mrntlu.localsocialmedia.service.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mrntlu.localsocialmedia.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(
    val id: Int,
    @SerializedName("image")
    var _image: String?,
    val email: String,
    val username: String,
    var name: String,
    @SerializedName("follower_count")
    var followerCount: Int,
    @SerializedName("following_count")
    var followingCount: Int,
    @SerializedName("post_count")
    var postCount: Int,
    @SerializedName("is_following")
    var isFollowing: Boolean
) : Parcelable {

    val imageUri get() = _image?.let {
        "${Constants.URL}$it"
    }
}