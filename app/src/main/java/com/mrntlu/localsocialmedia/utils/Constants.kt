package com.mrntlu.localsocialmedia.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class Constants {
    companion object{
        @SuppressLint("ConstantLocale")
        val CUSTOM_TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        var CUSTOM_DATE_LONG_FORMAT = SimpleDateFormat("MMM dd',' yy 'at' HH:mm", Locale.getDefault())

        const val API_URL = "http://192.168.1.18:8000/api/"

        const val TIME_OUT=7500L
        const val THEME_PREF_NAME="appTheme"
        const val DARK_THEME=0
        const val LIGHT_THEME=1
    }
}