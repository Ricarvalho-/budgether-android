package br.edu.ifsp.scl.transaction.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction

class TransactionsViewModel : ViewModel() {
    private val _transactions = MutableLiveData<List<RepeatingTransaction>>()
    val transactions = _transactions as LiveData<List<RepeatingTransaction>>

    fun set(transactions: List<RepeatingTransaction>) {
        this._transactions.value = transactions
    }
}
