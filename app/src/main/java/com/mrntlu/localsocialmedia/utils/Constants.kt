package com.mrntlu.localsocialmedia.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ConstantLocale")
class Constants {
    companion object{
        val CUSTOM_TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        var CUSTOM_DATE_FORMAT = SimpleDateFormat("MMM dd',' yyyy", Locale.getDefault())
        var CUSTOM_DATE_DETAILS_FORMAT = SimpleDateFormat("MMMM dd',' yyyy '·' HH:mm:ss", Locale.getDefault())
        val DJANGO_DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd '-' HH:mm:ss", Locale.getDefault())

        const val API_URL = "http://192.168.1.13:8000/api/"
        const val URL = "http://192.168.1.13:8000"

        const val TIME_OUT = 7500L
        const val THEME_PREF_NAME = "appTheme"
        const val DARK_THEME = 0
        const val LIGHT_THEME = 1

        const val FEED_PAGINATION_LIMIT = 15
        const val COMMENT_PAGINATION_LIMIT = 20
        const val FOLLOW_PAGINATION_LIMIT = 15
        const val SEARCH_PAGINATION_LIMIT = 25
    }
}