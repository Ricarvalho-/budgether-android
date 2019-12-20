package br.edu.ifsp.scl.common

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

typealias OnSnapPositionChangeCallback = (Int) -> Unit

class SnapHelperOnScrollListener(private val snapHelper: SnapHelper,
                                 var onSnapPositionChange: OnSnapPositionChangeCallback? = null) :
    RecyclerView.OnScrollListener() {

    private var oldSnapPosition = RecyclerView.NO_POSITION

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            notifySnapPositionIfChanged(recyclerView)
        }
    }

    private fun notifySnapPositionIfChanged(recyclerView: RecyclerView) {
        val newPosition = snapHelper.getSnapPosition(recyclerView)
        if (newPosition == oldSnapPosition) return
        oldSnapPosition = newPosition
        onSnapPositionChange?.invoke(newPosition)
    }

    private fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        return layoutManager.getPosition(snapView)
    }
}

fun RecyclerView.attachSnapHelperWithListener(snapHelper: SnapHelper,
                                              onSnapPositionChange: OnSnapPositionChangeCallback) {
    snapHelper.attachToRecyclerView(this)
    val scrollListener = SnapHelperOnScrollListener(snapHelper, onSnapPositionChange)
    addOnScrollListener(scrollListener)
}