package com.mrntlu.localsocialmedia.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellUserBinding
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.Constants
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.adapter.viewholder.PaginationItemViewHolder

class UserAdapter(override val interaction: UserInteraction): BaseAdapter<UserModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> PaginationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_user, parent, false), interaction)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            EMPTY.num -> {
                (holder as EmptyItemViewHolder).binding.emptyLottieView.setAnimation(R.raw.search)
                holder.binding.emptyTextView.text = "Search User"
            }
            else -> super.onBindViewHolder(holder, position)
        }
    }

    class UserHolder(itemView: View, private val interaction: UserInteraction): ItemHolder<UserModel>(itemView){
        val binding = CellUserBinding.bind(itemView)

        override fun bind(item: UserModel) {
            itemView.setOnClickListener {
                interaction.onItemSelected(adapterPosition, item)
            }

            item.apply {
                imageUri?.let {
                    Glide.with(binding.userImage.context)
                        .load(it)
                        .placeholder(ResourcesCompat.getDrawable(binding.userImage.context.resources,R.drawable.ic_account_126,null))
                        .into(binding.userImage)
                } ?: binding.userImage.setImageResource(R.drawable.ic_account_126)

                binding.userNameText.text = name
                binding.userUsernameText.text = username

                binding.userFollowButton.text = if (isFollowing)
                    "Unfollow"
                else
                    "Follow"

            }
        }
    }
}

interface UserInteraction: Interaction<UserModel>{
    fun onFollowPressed(position: Int, userModel: UserModel)
}