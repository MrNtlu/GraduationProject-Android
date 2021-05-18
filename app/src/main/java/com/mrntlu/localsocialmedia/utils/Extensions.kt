package com.mrntlu.localsocialmedia.utils

import android.util.Log
import android.view.View
import com.mrntlu.localsocialmedia.view.ui.main.MainActivity
import java.util.*

fun View.setGone() {
    this.visibility = View.GONE
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun View.shouldVisible(boolean: Boolean, viewType: Int = View.GONE) {
    this.visibility = if (boolean) View.VISIBLE else viewType
}

fun String.isNotEmptyOrBlank(): Boolean{
    return this.trim().isNotEmpty() && this.trim().isNotBlank()
}

fun printLog(message: String){
    Log.d("Test", message)
}

fun MainActivity.setToolbarBackButton(isEnabled: Boolean){
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(isEnabled)
        setDisplayShowHomeEnabled(isEnabled)
    }
}

fun Date.isYesterday(): Boolean{
    val now = Calendar.getInstance()
    val compareDate = Calendar.getInstance()
    compareDate.timeInMillis = this.time
    now.add(Calendar.DATE,-1)

    return now.get(Calendar.YEAR) == compareDate.get(Calendar.YEAR)
            && now.get(Calendar.MONTH) == compareDate.get(Calendar.MONTH)
            && now.get(Calendar.DATE) == compareDate.get(Calendar.DATE)
}