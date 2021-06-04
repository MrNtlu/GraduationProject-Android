package com.mrntlu.localsocialmedia.view.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.CellCommentBinding
import com.mrntlu.localsocialmedia.service.model.CommentModel
import com.mrntlu.localsocialmedia.utils.isDarkThemeOn
import com.mrntlu.localsocialmedia.utils.setGone
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.adapter.viewholder.EmptyItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.LoadingItemViewHolder
import com.mrntlu.localsocialmedia.view.adapter.viewholder.PaginationItemViewHolder

class CommentAdapter(override val interaction: CommentInteraction): BaseAdapter<CommentModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EMPTY.num -> EmptyItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_empty, parent, false))
            LOADING.num -> LoadingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.loading_layout, parent, false))
            ERROR.num -> ErrorItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_error, parent, false))
            PAGINATION.num -> PaginationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_pagination, parent, false))
            else -> CommentHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_comment, parent, false), interaction)
        }
    }

    class CommentHolder(itemView: View, private val interaction: CommentInteraction?): ItemHolder<CommentModel>(itemView){
        val binding = CellCommentBinding.bind(itemView)

        override fun bind(item: CommentModel) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            binding.commentMoreButton.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.report_menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == R.id.reportMenu)
                        interaction?.onReportPressed(adapterPosition, item)
                    true
                }
                popup.show()
            }

            binding.commentFavButton.setOnClickListener {
                interaction?.onFavPressed(adapterPosition, item)
            }

            item.apply {
                author.imageUri?.let {
                    Glide.with(binding.commentAuthorImage)
                        .load(it)
                        .addListener(object: RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                binding.commentAuthorImageProgress.setGone()
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                binding.commentAuthorImageProgress.setGone()
                                return false
                            }

                        })
                        .placeholder(ResourcesCompat.getDrawable(binding.commentAuthorImage.context.resources,R.drawable.ic_account_126,null))
                        .into(binding.commentAuthorImage)
                } ?: binding.commentAuthorImage.setImageResource(R.drawable.ic_account_126)

                binding.commentAuthorNameText.text = author.name
                binding.commentAuthorUsernameText.text = author.username
                binding.commentBodyText.text = message
                binding.commentPostDateText.text = postedDate

                val favColor: Int
                val favIcon: Drawable?

                if (isLiked){
                    favColor = ContextCompat.getColor(itemView.context, R.color.redMaterial400)
                    favIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_fav)
                }else{
                    favColor = ContextCompat.getColor(itemView.context, if (itemView.context.isDarkThemeOn()) R.color.white else R.color.black)
                    favIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_unfav)
                }

                binding.commentFavButton.imageTintList = ColorStateList.valueOf(favColor)
                binding.commentFavButton.setImageDrawable(favIcon)
            }
        }
    }
}

interface CommentInteraction: Interaction<CommentModel>{
    fun onFavPressed(position: Int, commentModel: CommentModel)
    fun onReportPressed(position: Int, commentModel: CommentModel)
}