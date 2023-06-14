package com.nurhaqhalim.momento.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    private val margin: Int,
    private val between: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {

            left = margin
            right = margin
            bottom = between
            top = between

            val itemPosition = parent.getChildAdapterPosition(view)
            if (itemPosition == 0) {
                top = margin
            }

            val adapter = parent.adapter
            if (adapter != null && itemPosition == adapter.itemCount - 1) {
                bottom = margin
            }

        }
    }
}