package br.edu.ifsp.scl.transaction

import android.view.View
import android.view.View.*
import androidx.databinding.BindingAdapter

class VisibilityBindingAdapter {
    @BindingAdapter("goneElseVisibleWhen")
    fun gone(view: View, gone: Boolean) {
        view.visibility = if (gone) GONE else VISIBLE
    }
}