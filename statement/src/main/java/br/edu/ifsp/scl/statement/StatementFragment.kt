package br.edu.ifsp.scl.statement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import br.edu.ifsp.scl.persistence.FromRepositories
import br.edu.ifsp.scl.persistence.statement.StatementProvider
import kotlinx.android.synthetic.main.statement_fragment.*

class StatementFragment: Fragment() {
    // TODO: Manage account selection with view model

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.statement_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // FIXME: Refactor adapter and ListView
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)
        amounts.adapter = adapter
        val statementProvider: StatementProvider = FromRepositories with requireContext()

        DatePeriodSelectionRecyclerViewManager(dateRangeRecyclerView) { period ->
            // TODO: Implement charts
            // TODO: Fetch data source and update charts data
            // TODO: Implement kind and category filtering views
            statementProvider.amountsIn(period.dateRange).observe(this, Observer { amountsByKind ->
                adapter.clear()
                adapter.addAll(amountsByKind.map { "${it.key}: ${it.value}" })
            })
        }
    }
}