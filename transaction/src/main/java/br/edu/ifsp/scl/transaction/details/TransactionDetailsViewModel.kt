package br.edu.ifsp.scl.transaction.details

import androidx.lifecycle.*
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference

class TransactionDetailsViewModel : ViewModel() {
    private val selectedTransaction = MutableLiveData<Transaction>()
    fun selectedTransaction() = selectedTransaction as LiveData<Transaction>
    infix fun selectTransaction(transaction: Transaction) {
        // TODO: Check equality with mediated fields (2 way data binding)
        // TODO: Notify before change, or just update on database (if changed)
        selectedTransaction.value = transaction
    }

    // TODO: Check if can do 2 way data binding in more fields

    val kindIcon = selectedTransaction mappedTo { TODO() }

    val category = selectedTransaction mediatedBy { it.category }

    val title = selectedTransaction mediatedBy { it.title }

    val value = selectedTransaction mediatedBy { it.value }

    val account = selectedTransaction mappedTo { TODO() }

    val recipientAccount = selectedTransaction mappedTo { TODO() }

    val transferFieldsShoulBeVisible = selectedTransaction mappedTo { it is Transference }

    val date = selectedTransaction mappedTo { TODO() }

    val frequency = selectedTransaction mappedTo { TODO() }

    val times = selectedTransaction mappedTo { TODO() }

    private infix fun <T, R> LiveData<T>.mappedTo(transform: (T) -> R) =
        Transformations.map(this) { transform(it) }

    private infix fun <T, R> LiveData<T>.mediatedBy(transform: (T) -> R) =
        MediatorLiveData<R>().apply {
            addSource(this@mediatedBy) { value = transform(it) }
        } as MutableLiveData<R>
}
