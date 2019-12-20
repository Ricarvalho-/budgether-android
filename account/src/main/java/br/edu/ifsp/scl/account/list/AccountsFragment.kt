package br.edu.ifsp.scl.account.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.scl.account.R
import br.edu.ifsp.scl.account.details.AccountDetailsViewModel
import br.edu.ifsp.scl.account.edit.AccountTitleViewModel
import br.edu.ifsp.scl.account.edit.TitleEditTextDialogFragment
import br.edu.ifsp.scl.common.ItemMarginDecoration
import br.edu.ifsp.scl.common.currencyFormatted
import br.edu.ifsp.scl.persistence.account.AccountData
import kotlinx.android.synthetic.main.coordinated_toolbar_recycler_and_fab.*

class AccountsFragment : Fragment() {
    private val accountsViewModel: AccountsViewModel by activityViewModels()
    private val accountDetailsViewModel: AccountDetailsViewModel by activityViewModels()
    private val titleViewModel: AccountTitleViewModel by activityViewModels()
    private val accountsAdapter = AccountsAdapter({
        navigateToDetailsOf(it)
    }, {
        // TODO: Set selection + trs list on tr det VM
        // TODO: Nav to det
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.coordinated_toolbar_recycler_and_fab, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeTotalBalance()
        observeAccountList()
        setupRecyclerView()
        setupFab()

        // TODO: Refactor statement button to menu
        statementButton.setOnClickListener {
            findNavController().navigate(R.id.action_accountsFragment_to_statement_nav_graph)
        }
    }

    private fun observeTotalBalance() = accountsViewModel.balance.total.observe(
        this, Observer { balanceTextView.text = "Total balance: ${it.currencyFormatted()}" }
    )

    private fun observeAccountList() = accountsViewModel.list.accounts.observe(
        this, Observer { accountsAdapter.accounts = it }
    )

    private fun setupRecyclerView() = recyclerView.apply {
        addItemDecoration(ItemMarginDecoration(R.dimen.item_margin))
        layoutManager = LinearLayoutManager(context)
        setHasFixedSize(true)
        adapter = accountsAdapter
    }

    private fun setupFab() {
        observeTitleDefinition()
        observeAccountCreation()
        addFab.setOnClickListener {
            observeTitleDefinition()
            showAccountCreationDialog()
        }
    }

    private fun observeTitleDefinition() = lifecycleScope.launchWhenStarted {
        val title = titleViewModel.inserted.titleChannel.receive()
        accountsViewModel.creator createAccountWith title
    }

    private fun observeAccountCreation() = lifecycleScope.launchWhenStarted {
        for (account in accountsViewModel.creator.newAccountChannel) {
            navigateToDetailsOf(account)
        }
    }

    private fun navigateToDetailsOf(account: AccountData) {
        accountDetailsViewModel.selection.select(account)
        findNavController().navigate(R.id.action_accountsFragment_to_accountDetailsFragment)
    }

    private fun showAccountCreationDialog() =
        TitleEditTextDialogFragment().show(requireFragmentManager(), null)
}