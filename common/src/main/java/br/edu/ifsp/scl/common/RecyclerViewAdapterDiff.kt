package br.edu.ifsp.scl.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface DifferentiableAdapter {
    fun <T> RecyclerView.Adapter<*>.notifyChangesBetween(
        oldList: List<T>, newList: List<T>, isSameItem: (T, T) -> Boolean
    ) = DiffUtil.calculateDiff(
        object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                isSameItem(oldList[oldItemPosition], newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]
        }
    ).dispatchUpdatesTo(this)
}