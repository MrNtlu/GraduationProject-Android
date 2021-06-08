package com.mrntlu.localsocialmedia.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayoutMediator
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserVoteModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.view.adapter.FeedImageListViewPagerAdapter
import com.mrntlu.localsocialmedia.view.ui.main.MainActivity
import java.util.*
import kotlin.math.roundToInt

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

fun String.convertToDate(): String{
    val date = Constants.DJANGO_DATE_FORMAT.parse(this)
    date?.let {
        val timeElapsed = Date().time - date.time

        val oneMin = 60000L
        val oneHour = 3600000L
        val oneDay = 86400000L
        val oneWeek = 604800000L

        var finalString = "0 sec"
        val unit: String

        if (timeElapsed < oneMin){
            var seconds = (timeElapsed / 1000).toDouble()
            seconds = seconds.roundToInt().toDouble()
            if (seconds < 30) {
                finalString = "Now."
            }else{
                unit = " secs ago."
                finalString = "${seconds.toInt()}$unit"
            }
        }else if (timeElapsed < oneHour){
            val minutes = (timeElapsed / 1000) / 60
            finalString = handleAgoString(minutes.toDouble(), " min")
        }else if (timeElapsed < oneDay){
            val hours = ((timeElapsed / 1000) / 60) / 60
            finalString = handleAgoString(hours.toDouble(), " hr")
        }else if (timeElapsed < oneWeek){
            val days = (((timeElapsed / 1000) / 60) / 60) / 24
            finalString = handleAgoString(days.toDouble(), " day")
        }else if (timeElapsed > oneWeek){
            val weeks = ((((timeElapsed / 1000) / 60) / 60) / 24) / 7
            finalString = if (weeks.toInt() < 4)
                handleAgoString(weeks.toDouble(), " week")
            else{
                Constants.CUSTOM_DATE_FORMAT.format(it).toString()
            }
        }
        return finalString
    }
    return this
}

private fun handleAgoString(ago: Double, agoString: String): String{
    val agoInt = ago.roundToInt()
    var returnString = agoString
    if (ago.toInt() != 1)
        returnString = "${agoString}s"
    return "$agoInt$returnString ago."
}

fun MainActivity.setToolbarBackButton(isEnabled: Boolean){
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(isEnabled)
        setDisplayShowHomeEnabled(isEnabled)
    }
}

fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
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

fun CellFeedBinding.setUI(feedModel: FeedModel, isDetailsPage: Boolean = false){
    feedModel.apply {
        author.imageUri?.let {
            Glide.with(feedAuthorImage)
                .load(it)
                .addListener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        feedAuthorImageProgress.setGone()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        feedAuthorImageProgress.setGone()
                        return false
                    }

                })
                .placeholder(
                    ResourcesCompat.getDrawable(feedAuthorImage.context.resources,
                        R.drawable.ic_account_126,null))
                .into(feedAuthorImage)
        } ?: feedAuthorImage.setImageResource(R.drawable.ic_account_126)

        feedAuthorNameText.text = author.name
        feedAuthorUsernameText.text = author.username
        feedBodyText.text = message
        feedPostDateText.text = if (!isDetailsPage)
            postedDate.convertToDate()
        else {
            Constants.DJANGO_DATE_FORMAT.parse(postedDate)?.let {
                Constants.CUSTOM_DATE_DETAILS_FORMAT.format(it)
            } ?: postedDate.convertToDate()
        }
        feedVoteText.text = (upvoteCount - downvoteCount).toString()
        feedSpamLayout.isVisible = isSpam

        printLog("$images")
        if (images.isNotEmpty()) {
            feedCardView.setVisible()
            feedImageViewPager.apply {
                this.adapter = FeedImageListViewPagerAdapter(feedModel)
                if (images.size > 1) {
                    imageIndicatorTabLayout.setVisible()
                    TabLayoutMediator(imageIndicatorTabLayout, this) { _, _ -> }.attach()
                } else
                    imageIndicatorTabLayout.setGone()
            }
        }else
            feedCardView.setGone()
    }
}

fun CellFeedBinding.setVoteUI(context: Context, userVote: UserVoteModel){
    var upVote = ContextCompat.getColor(context, if (context.isDarkThemeOn()) R.color.white else R.color.black)
    var downVote = ContextCompat.getColor(context, if (context.isDarkThemeOn()) R.color.white else R.color.black)
    if (userVote.isVoted){
        when(userVote.voteType){
            VoteType.UpVote -> {
                upVote = ContextCompat.getColor(context, R.color.greenMaterial400)
            }
            VoteType.DownVote -> {
                downVote = ContextCompat.getColor(context, R.color.redMaterial400)
            }
        }
    }
    feedUpVoteButton.imageTintList = ColorStateList.valueOf(upVote)
    feedDownVoteButton.imageTintList = ColorStateList.valueOf(downVote)
}

fun Location.getLocationName(context: Context): String {
    val locationName: String

    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses.size > 0){
            val address = addresses[0]
            locationName = address?.let {
                if (it.adminArea.isNotEmptyOrBlank() && it.subAdminArea.isNotEmptyOrBlank()){
                    "${it.subAdminArea}/${it.adminArea}"
                }else{
                    if (it.adminArea.isNotEmptyOrBlank())
                        it.adminArea
                    else if (it.subAdminArea.isNotEmptyOrBlank())
                        it.subAdminArea
                    else if (it.countryName != null && it.countryName.isNotEmptyOrBlank())
                        it.countryName
                    else
                        "Unknown"
                }
            } ?: "Unknown"
        }else
            locationName = "Unknown"

        return locationName
    }catch (e: Exception){
        return "Unknown"
    }
}