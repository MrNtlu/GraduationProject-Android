package com.mrntlu.localsocialmedia.view.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.view.`interface`.Interaction
import com.mrntlu.localsocialmedia.view.adapter.BaseAdapter.HolderType.*
import com.mrntlu.localsocialmedia.view.adapter.viewholder.ErrorItemViewHolder

abstract class BaseAdapter<T>(open val interaction: Interaction<T>? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    enum class HolderType(val num: Int) {
        LOADING(0),
        ITEM(1),
        ERROR(2),
        EMPTY(3),
        PAGINATION(4)
    }

    //Conditions
    private var isAdapterSet = false
    private var isErrorOccurred = false
    private var isPaginationLoading = false

    protected open var errorMessage = "Error!"
    private var arrayList: ArrayList<T> = arrayListOf()

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM.num -> {
                (holder as ItemHolder<T>).bind(arrayList[position])
            }
            ERROR.num -> {
                val viewHolder = holder as ErrorItemViewHolder
                viewHolder.binding.apply {
                    errorText.text = errorMessage
                    errorRefreshButton.setOnClickListener {
                        interaction?.onErrorRefreshPressed()
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (isAdapterSet) {
            when {
                isErrorOccurred -> ERROR.num
                arrayList.size == 0 -> EMPTY.num
                isPaginationLoading && position == arrayList.size -> PAGINATION.num
                else -> ITEM.num
            }
        } else
            LOADING.num

    override fun getItemCount() =
        if (isAdapterSet && !isErrorOccurred && arrayList.size != 0 && !isPaginationLoading)
            arrayList.size
        else if (isAdapterSet && !isErrorOccurred && isPaginationLoading)
            arrayList.size + 1
        else
            1

    fun submitList(list: List<T>) {
        arrayList.apply {
            this.clear()
            this.addAll(list)
        }
        isAdapterSet = true
        isErrorOccurred = false
        notifyDataSetChanged()
    }

    fun submitLoading() {
        isAdapterSet = false
        isErrorOccurred = false
        notifyDataSetChanged()
    }

    fun submitError(message: String) {
        isAdapterSet = true
        isErrorOccurred = true
        errorMessage = message
        notifyDataSetChanged()
    }

    //Pagination
    fun submitPaginationLoading() {
        isPaginationLoading = true
        notifyDataSetChanged()
    }

    fun submitPaginationError() {
        isPaginationLoading = false
        notifyDataSetChanged()
    }

    fun updateList(newList: ArrayList<T>) {
        if (!isAdapterSet) {
            submitList(newList)
        } else {
            if (arrayList.size == 0) {
                arrayList.addAll(newList)
                notifyDataSetChanged()
            } else {
                if ((newList.size - arrayList.size) > 0) {
                    submitPaginationError()

                    val diffResult = DiffUtil.calculateDiff(GenericDiffUtil(arrayList, newList))

                    arrayList.addAll(0, newList.subList(0, (newList.size - arrayList.size)))
                    diffResult.dispatchUpdatesTo(this)
                } else
                    submitPaginationError()
            }
        }
    }

    fun updateItem(position: Int, item: T){
        arrayList[position] = item
        notifyItemChanged(position)
    }

    abstract class ItemHolder<T> constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }
}