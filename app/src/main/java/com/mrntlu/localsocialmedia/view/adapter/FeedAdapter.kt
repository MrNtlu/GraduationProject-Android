package com.mrntlu.localsocialmedia.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellFeedBinding
import com.mrntlu.localsocialmedia.service.model.FeedModel
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.service.model.VoteType
import com.mrntlu.localsocialmedia.utils.setUI
import com.mrntlu.localsocialmedia.utils.setVoteUI
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.adapter.viewholder.PaginationItemViewHolder

class FeedAdapter(private val currentUser: UserModel, override val interaction: FeedInteraction): BaseAdapter<FeedModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> PaginationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> FeedHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_feed, parent, false), currentUser, interaction)
        }
    }

    class FeedHolder(itemView: View, private val currentUser: UserModel, private val interaction: FeedInteraction?): ItemHolder<FeedModel>(itemView){
        val binding = CellFeedBinding.bind(itemView)

        override fun bind(item: FeedModel) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            binding.feedUpVoteButton.setOnClickListener {
                interaction?.onVotePressed(VoteType.UpVote, adapterPosition, item)
            }

            binding.feedDownVoteButton.setOnClickListener {
                interaction?.onVotePressed(VoteType.DownVote, adapterPosition, item)
            }

            binding.feedMoreButton.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(
                    if (currentUser.id == item.author.id)
                        R.menu.delete_menu
                    else
                        R.menu.report_menu
                )
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.reportMenu)
                        interaction?.onReportPressed(adapterPosition, item)
                    else if (menuItem.itemId == R.id.deleteMenu)
                        interaction?.onDeletePressed(adapterPosition, item)
                    true
                }
                popup.show()
            }

            item.apply {
                binding.setUI(this)
                binding.setVoteUI(itemView.context, userVote)
            }
        }
    }
}

interface FeedInteraction: Interaction<FeedModel>{
    fun onReportPressed(position: Int, feedModel: FeedModel)
    fun onVotePressed(voteType: VoteType, position: Int, feedModel: FeedModel)
    fun onDeletePressed(position: Int, feedModel: FeedModel)
}