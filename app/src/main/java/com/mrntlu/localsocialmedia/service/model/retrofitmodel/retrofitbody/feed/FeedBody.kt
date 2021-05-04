package com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.feed

import android.net.Uri

data class FeedBody(
    val message: String,
    val type: Int,
    val latitude: Float,
    val longitude: Float,
    val locationName: String,
    val images: List<Uri>?
)
