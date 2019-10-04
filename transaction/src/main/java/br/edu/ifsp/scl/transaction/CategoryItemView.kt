package br.edu.ifsp.scl.transaction

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import br.edu.ifsp.scl.common.setCircularImage
import kotlinx.android.synthetic.main.category_item.view.*

class CategoryItemView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyle: Int = 0) :
    ConstraintLayout(context, attrs, defStyle) {

    var viewModel: ViewModel? = null
    set(value) {
        field = value
        if (value != null) imageView.setCircularImage(resources, value.image)
        titleTextView.text = value?.title
    }

    interface ViewModel {
        val image: Int @DrawableRes get
        val title: String
    }
}