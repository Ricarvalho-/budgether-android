package br.edu.ifsp.scl.account.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.scl.account.R
import br.edu.ifsp.scl.common.DifferentiableAdapter
import br.edu.ifsp.scl.common.currencyFormatted
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.transaction.TransactionData
import kotlinx.android.synthetic.main.account_item.view.*
import kotlin.properties.Delegates

class AccountsAdapter(
    var accountSelectionListener: (AccountData) -> Unit,
    var nearestTransactionSelectionListener: (TransactionData) -> Unit
) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>(), DifferentiableAdapter {

    var accounts: List<AccountSummary> by Delegates.observable(emptyList()) { _, old, new ->
        notifyChangesBetween(old, new) { a1, a2 -> a1.accountData.id == a2.accountData.id }
    }

    override fun getItemCount() = accounts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
    ).apply {
        itemView.setOnClickListener {
            accountSelectionListener(accounts[adapterPosition].accountData)
        }
        // FIXME: Set listener on transaction view
//        transactionView.setOnClickListener {
//            val transaction = accounts[adapterPosition].nearestTransactionData
//            transaction?.let(nearestTransactionSelectionListener)
//        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(accounts[position]) {
        holder.titleTextView.text = accountData.title
        holder.valueTextView.text = balance.currencyFormatted()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.titleTextView
        val valueTextView: TextView = itemView.balanceTextView
    }

    data class AccountSummary(val accountData: AccountData,
                              val balance: Double = 0.0,
                              val nearestTransactionData: TransactionData? = null)
}