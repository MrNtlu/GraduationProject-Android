package com.mrntlu.localsocialmedia.view.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.utils.setGone

class FeedImageListViewPagerAdapter(val feedModel: FeedModel): RecyclerView.Adapter<FeedImageListViewPagerAdapter.FeedImageListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedImageListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_feed_image_viewpager, parent, false)
        return FeedImageListViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedImageListViewHolder, position: Int) {
        val image = feedModel.imageUris[position]
        Glide.with(holder.itemView).load(image).addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                holder.feedImageProgress.setGone()
                return true
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                holder.feedImageProgress.setGone()
                return false
            }
        }).into(holder.feedImage)
    }

    override fun getItemCount() = feedModel.images.size

    inner class FeedImageListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val feedImage: ImageView = itemView.findViewById(R.id.feedImage)
        val feedImageProgress: ProgressBar = itemView.findViewById(R.id.feedImageProgress)
    }
}