package br.edu.ifsp.scl.transaction

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.category_item.view.*

class CategoryItemView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyle: Int = 0) :
    ConstraintLayout(context, attrs, defStyle) {

    var viewModel: ViewModel? = null
    set(value) {
        field = value
        imageView.setImageDrawable(value?.image)
        titleTextView.text = value?.title
    }

    interface ViewModel {
        val image: Drawable
        val title: String
    }
}