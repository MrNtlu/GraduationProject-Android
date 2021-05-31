package com.mrntlu.localsocialmedia.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFollowUserBinding
import com.mrntlu.localsocialmedia.service.model.UserFollowModel
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.PaginationItemViewHolder

class FollowAdapter(override val interaction: Interaction<UserFollowModel>): BaseAdapter<UserFollowModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> PaginationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> FollowHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_follow_user, parent, false), interaction)
        }
    }

    class FollowHolder(itemView: View, private val interaction: Interaction<UserFollowModel>?): ItemHolder<UserFollowModel>(itemView){
        val binding = CellFollowUserBinding.bind(itemView)

        override fun bind(item: UserFollowModel) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            item.apply {
                user.imageUri?.let {
                    Glide.with(binding.followUserImage.context)
                        .load(it)
                        .placeholder(ResourcesCompat.getDrawable(binding.followUserImage.context.resources,R.drawable.ic_account_126,null))
                        .into(binding.followUserImage)
                } ?: binding.followUserImage.setImageResource(R.drawable.ic_account_126)

                binding.followNameText.text = user.name
                binding.followUsernameText.text = user.username
            }
        }
    }
}