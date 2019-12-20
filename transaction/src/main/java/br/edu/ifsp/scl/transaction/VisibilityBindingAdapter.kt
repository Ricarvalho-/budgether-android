package br.edu.ifsp.scl.transaction

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.databinding.BindingAdapter

class VisibilityBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("goneElseVisibleWhen")
        fun gone(view: View, gone: Boolean) {
            view.visibility = if (gone) GONE else VISIBLE
        }
    }
}