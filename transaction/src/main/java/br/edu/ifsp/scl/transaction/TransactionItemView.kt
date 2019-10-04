package br.edu.ifsp.scl.transaction

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import br.edu.ifsp.scl.common.currencyFormatted
import br.edu.ifsp.scl.common.setCircularImage
import br.edu.ifsp.scl.common.shortFormatted
import kotlinx.android.synthetic.main.transaction_item.view.*
import java.util.*

class TransactionItemView @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null,
                                                    defStyle: Int = 0) :
    ConstraintLayout(context, attrs, defStyle) {

    var viewModel: ViewModel? = null
    set(value) {
        field = value
        if (value != null) categoryImageView.setCircularImage(resources, value.image)
        titleTextView.text = value?.title
        valueTextView.text = value?.value?.currencyFormatted()
        dateTextView.text = value?.date?.shortFormatted(context)
    }

    interface ViewModel {
        val image: Int @DrawableRes get
        val title: String
        val value: Double
        val date: Date
    }
}