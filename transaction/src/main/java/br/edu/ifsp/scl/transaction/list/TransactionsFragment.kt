package br.edu.ifsp.scl.transaction.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.scl.persistence.transaction.TransactionData
import br.edu.ifsp.scl.transaction.R
import br.edu.ifsp.scl.transaction.details.TransactionDetailsViewModel
import kotlinx.android.synthetic.main.transactions_fragment.*

class TransactionsFragment : Fragment() {
    private val viewModel: TransactionsViewModel by activityViewModels()
    private val transactionDetailsViewModel: TransactionDetailsViewModel by activityViewModels()
    private val transactionsAdapter = TransactionsAdapter {
        navigateToDetailsOf(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.transactions_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeTransactions()
        setupRecyclerView()
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(this, Observer {
            transactionsAdapter.transactions = it
        })
    }

    private fun setupRecyclerView() = recyclerView.apply {
        layoutManager = LinearLayoutManager(context)
        setHasFixedSize(true)
        adapter = transactionsAdapter
    }

    private fun navigateToDetailsOf(transaction: TransactionData) {
        transactionDetailsViewModel.selectTransaction(transaction)
        // TODO: Navigate to details
        // Use channel to notify?
        // findNavController().navigate(R.id.)
    }
}
