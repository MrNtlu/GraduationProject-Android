package com.mrntlu.localsocialmedia.view.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.utils.setGone
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.ui.main.MainActivity

class FeedAdapter(private val currentUser: UserModel, override val interaction: Interaction<FeedModel>): BaseAdapter<FeedModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> FeedHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_feed, parent, false), currentUser, interaction)
        }
    }

    class FeedHolder(itemView: View, private val currentUser: UserModel, private val interaction: Interaction<FeedModel>?): ItemHolder<FeedModel>(itemView){
        val binding = CellFeedBinding.bind(itemView)

        override fun bind(item: FeedModel) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            item.apply {
                author.imageUri?.let {
                    Glide.with(binding.feedAuthorImage)
                        .load(it)
                        .addListener(object: RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                binding.feedAuthorImageProgress.setGone()
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                binding.feedAuthorImageProgress.setGone()
                                return false
                            }

                        })
                        .placeholder(ResourcesCompat.getDrawable(binding.feedAuthorImage.context.resources,R.drawable.ic_account_126,null))
                        .into(binding.feedAuthorImage)
                } ?: binding.feedAuthorImage.setImageResource(R.drawable.ic_account_126)

                binding.feedAuthorNameText.text = author.name
                binding.feedAuthorUsernameText.text = "@${author.username}"
                binding.feedBodyText.text = message
                binding.feedPostDateText.text = postedDate
                binding.feedVoteText.text = (upvoteCount - downvoteCount).toString()

                //TODO Change it on server side and get if user voted or not and return it
                val currentUserVote = votes.filter {
                    it.user.id == currentUser.id
                }
                if(currentUserVote.isNotEmpty()){
                    val tintColor = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                    when(currentUserVote[0].voteType){
                        VoteType.UpVote->{
                            binding.feedUpVoteButton.imageTintList = tintColor
                        }
                        VoteType.DownVote->{
                            binding.feedUpVoteButton.imageTintList = tintColor
                        }
                    }
                }
            }
        }
    }
}