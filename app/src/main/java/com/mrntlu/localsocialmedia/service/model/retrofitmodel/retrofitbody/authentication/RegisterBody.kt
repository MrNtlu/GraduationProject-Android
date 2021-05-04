package com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.authentication

data class RegisterBody(
        val username: String,
        val email: String,
        val name: String,
        val password: String
)