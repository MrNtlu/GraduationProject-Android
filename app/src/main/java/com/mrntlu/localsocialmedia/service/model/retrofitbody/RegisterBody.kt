package com.mrntlu.localsocialmedia.service.model.retrofitbody

data class RegisterBody(
        val username: String,
        val email: String,
        val name: String,
        val password: String
)