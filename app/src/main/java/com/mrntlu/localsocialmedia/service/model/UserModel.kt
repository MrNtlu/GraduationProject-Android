package com.mrntlu.localsocialmedia.service.model

data class UserModel(
        val id: Int,
        val email: String,
        val username: String,
        var name: String,
        var followings: List<UserFollowModel>,
        var followers: List<UserFollowModel>
)