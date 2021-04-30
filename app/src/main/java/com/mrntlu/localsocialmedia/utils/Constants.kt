package com.mrntlu.localsocialmedia.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class Constants {
    companion object{
        @SuppressLint("ConstantLocale")
        val CUSTOM_TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        var CUSTOM_DATE_LONG_FORMAT = SimpleDateFormat("MMM dd',' yy 'at' HH:mm", Locale.getDefault())

        val API_URL = "http://127.0.0.1:8000/api/"
    }
}