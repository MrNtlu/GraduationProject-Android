package com.mrntlu.localsocialmedia.view.adapter.viewpager

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.view.ui.main.ProfileFollowPagerFragment

class ProfileFollowPagerAdapter(private val context: Context, fm: FragmentManager, userID: Int) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = listOf(
        ProfileFollowPagerFragment(ProfileFollowPagerFragment.PagerType.FOLLOWER, userID),
        ProfileFollowPagerFragment(ProfileFollowPagerFragment.PagerType.FOLLOWING, userID)
    )

    override fun getCount(): Int = fragmentList.count()

    override fun getItem(position: Int) = fragmentList[position]

    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0 -> context.getString(R.string.profile_followers)
            else -> context.getString(R.string.profile_following)
        }
    }
}