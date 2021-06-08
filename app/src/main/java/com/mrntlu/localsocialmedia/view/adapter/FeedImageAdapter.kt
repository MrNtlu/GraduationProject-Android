package com.mrntlu.localsocialmedia.view.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellImageBinding
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.PaginationItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*

class FeedImageAdapter: BaseAdapter<Uri>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> PaginationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> ImageHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_image, parent, false))
        }
    }

    class ImageHolder(itemView: View): ItemHolder<Uri>(itemView){
        val binding = CellImageBinding.bind(itemView)

        override fun bind(item: Uri) {
            item.apply {
                Glide.with(binding.feedImageView.context)
                    .load(item)
                    .into(binding.feedImageView)
            }
        }
    }
}