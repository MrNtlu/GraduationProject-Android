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
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserVoteModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.view.ui.main.MainActivity
import java.lang.Exception
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

fun CellFeedBinding.setUI(feedModel: FeedModel){
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
        feedPostDateText.text = postedDate
        feedVoteText.text = (upvoteCount - downvoteCount).toString()
    }
}

fun CellFeedBinding.setVoteUI(context: Context, userVote: UserVoteModel){
    var upVote = ContextCompat.getColor(context, if (context.isDarkThemeOn()) R.color.white else R.color.white)
    var downVote = ContextCompat.getColor(context, if (context.isDarkThemeOn()) R.color.white else R.color.white)
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