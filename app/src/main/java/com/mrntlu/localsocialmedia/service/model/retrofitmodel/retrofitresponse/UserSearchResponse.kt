package com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitresponse

import android.os.Parcelable
import com.mrntlu.localsocialmedia.service.model.UserModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserSearchResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<UserModel>
): Parcelable
