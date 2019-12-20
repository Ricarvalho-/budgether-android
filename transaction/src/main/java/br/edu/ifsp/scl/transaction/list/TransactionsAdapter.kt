package br.edu.ifsp.scl.transaction.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.scl.common.DifferentiableAdapter
import br.edu.ifsp.scl.common.currencyFormatted
import br.edu.ifsp.scl.common.shortFormatted
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Many
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Many.Times.Determinate
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Many.Times.Indeterminate
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Single
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction
import br.edu.ifsp.scl.transaction.R
import kotlinx.android.synthetic.main.transaction_item.view.*
import kotlin.properties.Delegates

class TransactionsAdapter(
    var transactionSelectionListener: (RepeatingTransaction) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>(), DifferentiableAdapter {

    var transactions: List<RepeatingTransaction> by Delegates.observable(emptyList()) { _, old, new ->
        notifyChangesBetween(old, new) { t1, t2 -> t1 == t2 }
    }

    override fun getItemCount() = transactions.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
    ).apply {
        itemView.setOnClickListener {
            transactionSelectionListener(transactions[adapterPosition])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(transactions[position]) {
        holder.titleTextView.text = title
        holder.numberTextView.run {
            when (val rpt = repeatability) {
                Single -> visibility = View.GONE
                is Many -> {
                    visibility = View.VISIBLE
                    text = when (val times = rpt.of) {
                        Indeterminate -> context.getString(R.string.transaction_number_indeterminate_template, rpt.number)
                        is Determinate -> context.getString(R.string.transaction_number_template, rpt.number, times.amount)
                    }

                }
            }
        }
        holder.valueTextView.text = value.currencyFormatted()
        holder.dateTextView.run { text = atDate.shortFormatted(context) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kindImageView: ImageView = itemView.kindImageView
        val titleTextView: TextView = itemView.titleTextView
        val numberTextView: TextView = itemView.numberTextView
        val valueTextView: TextView = itemView.valueTextView
        val dateTextView: TextView = itemView.dateTextView
    }
}