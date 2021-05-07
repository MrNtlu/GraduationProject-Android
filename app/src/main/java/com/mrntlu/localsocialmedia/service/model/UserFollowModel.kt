package com.mrntlu.localsocialmedia.service.model

import java.util.*

data class UserFollowModel(
        val id: Int,
        val user: UserModel,
        val date: Date
)