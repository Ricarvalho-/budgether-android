package br.edu.ifsp.scl.transaction

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import br.edu.ifsp.scl.common.currencyFormatted
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
        imageView.setImageDrawable(value?.image)
        titleTextView.text = value?.title
        valueTextView.text = value?.value?.currencyFormatted()
        dateTextView.text = value?.date?.shortFormatted(context)
    }

    interface ViewModel {
        val image: Drawable
        val title: String
        val value: Double
        val date: Date
    }
}