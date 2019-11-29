package br.edu.ifsp.scl.transaction.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import br.edu.ifsp.scl.transaction.R
import br.edu.ifsp.scl.transaction.databinding.TransactionDetailsFragmentBinding
import kotlinx.android.synthetic.main.transaction_details_fragment.*

class TransactionDetailsFragment : Fragment() {
    companion object {
        fun newInstance() = TransactionDetailsFragment()
    }

    private val viewModel: TransactionDetailsViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? =
        TransactionDetailsFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        kindFab.setOnClickListener {
            // TODO: Open kind selector
            // if changed, delete transaction and insert of new kind with old data
        }
    }
    // set date and times click listener to open picker dialog
    // set times value range on picker listener, wrap selector wheels = false, formatter: Indeterminate, 2x, 3x, Nx, ...

    // TODO: Use ViewModel on layout bindings
    // set kindFab icon
    // set recipient account and times visibility
    // set title, category, value, frequency and times field values as two way data binding with @={}
    // set title, category, value, account, recipient account, date, frequency and times field values
    // set title, category, account, recipient account, and frequency suggestions
    // accounts field suggestions must be filtered excluding each other's selected value
    // set title and category validators (trimmed, no multiline, not empty)
    // set swap accounts button visibility (transfer only)
    // set swap accounts button click listener
}
