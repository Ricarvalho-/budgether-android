package br.edu.ifsp.scl.account.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.scl.account.R
import br.edu.ifsp.scl.common.ItemMarginDecoration
import kotlinx.android.synthetic.main.coordinated_toolbar_recycler_and_fab.*

class AccountsFragment : Fragment() {
    private val viewModel: AccountsViewModel by activityViewModels()
    private val accountsAdapter = AccountsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitForAccountCreationThenNavigateToDetails()
    }

    private fun waitForAccountCreationThenNavigateToDetails() = lifecycleScope.launchWhenStarted {
        for (account in viewModel.newlyCreatedAccountChannel) {
            // TODO: Navigate to details
            addFab.show()
            Toast.makeText(context, "Inserted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.coordinated_toolbar_recycler_and_fab, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        setupRecyclerView()
        viewModel.accounts.observe(this, Observer { accountsAdapter.accounts = it })
    }

    private fun setupFab() = addFab.setOnClickListener {
        // TODO: Show dialog with EditText
        // Call viewModel's insert with text from dialog
        addFab.hide()
        viewModel.createAccountWith("Test account")
    }

    private fun setupRecyclerView() = recyclerView.apply {
        addItemDecoration(ItemMarginDecoration(R.dimen.item_margin))
        layoutManager = LinearLayoutManager(context)
        setHasFixedSize(true)
        adapter = accountsAdapter
    }
}