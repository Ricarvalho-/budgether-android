package br.edu.ifsp.scl.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {

        val isFirstItem = parent.getChildAdapterPosition(view) == 0
        outRect.apply {
            if (isFirstItem) top = margin
            left =  margin
            right = margin
            bottom = margin
        }
    }
}