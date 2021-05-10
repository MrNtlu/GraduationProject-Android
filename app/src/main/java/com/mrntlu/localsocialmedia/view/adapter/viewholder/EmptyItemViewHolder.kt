package com.mrntlu.localsocialmedia.view.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.databinding.CellEmptyBinding

class EmptyItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val binding = CellEmptyBinding.bind(view)
}