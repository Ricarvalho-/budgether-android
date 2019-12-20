package br.edu.ifsp.scl.account.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.edu.ifsp.scl.account.R
import br.edu.ifsp.scl.account.edit.AccountTitleViewModel
import br.edu.ifsp.scl.account.edit.TitleEditTextDialogFragment
import br.edu.ifsp.scl.common.currencyFormatted
import br.edu.ifsp.scl.transaction.details.TransactionDetailsViewModel
import br.edu.ifsp.scl.transaction.list.TransactionsViewModel
import kotlinx.android.synthetic.main.coordinated_toolbar_frame_and_fab.*

class AccountDetailsFragment : Fragment() {
    private val detailsViewModel: AccountDetailsViewModel by activityViewModels()
    private val titleViewModel: AccountTitleViewModel by activityViewModels()
    private val transactionsViewModel: TransactionsViewModel by activityViewModels()
    private val transactionDetailsViewModel: TransactionDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.coordinated_toolbar_frame_and_fab, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeAccountBalance()
        setupContent()
        setupFab()
        setupEditButton()
        setupDeleteButton()

        // TODO: Refactor statement button to menu
        statementButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountDetailsFragment_to_statement_nav_graph)
        }
    }

    private fun observeAccountBalance() = detailsViewModel.balance.selectedAccount.observe(
        this, Observer { balanceTextView.text = "${detailsViewModel.selection.lastSelected.value?.title}: ${it.currencyFormatted()}" }
    )

    private fun setupContent() {
        observeAccountTransactions()
        // TODO: Setup transactions recycler fragment
    }

    private fun observeAccountTransactions() =
        detailsViewModel.list.transactionsOfSelectedAccount.observe(
            this, Observer {
                transactionsViewModel.set(it)
            }
        )

    private fun setupFab() {
        observeTransactionCreation()
        addFab.setOnClickListener {
            detailsViewModel.creator.createTransaction()
        }
    }

    private fun observeTransactionCreation() = lifecycleScope.launchWhenStarted {
        for (transaction in detailsViewModel.creator.newTransactionChannel) {
            transactionDetailsViewModel.selectTransaction(transaction)
            findNavController().navigate(R.id.action_accountDetailsFragment_to_transaction_nav_graph)
        }
    }

    private fun setupEditButton() {
        observeTitleDefinition()
//        showAccountEditDialog() // TODO: Put inside click listener
    }

    private fun observeTitleDefinition() = lifecycleScope.launchWhenStarted {
        val title = titleViewModel.inserted.titleChannel.receive()
        detailsViewModel.editor setTitleOfSelectedAccount title
    }

    private fun showAccountEditDialog() =
        TitleEditTextDialogFragment().show(requireFragmentManager(), null)

    private fun setupDeleteButton() {} // TODO: Show confirmation dialog and call viewModel's delete and navigate back
}