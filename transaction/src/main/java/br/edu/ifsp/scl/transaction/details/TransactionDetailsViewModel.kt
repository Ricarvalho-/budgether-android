package br.edu.ifsp.scl.transaction.details

import androidx.lifecycle.*
import br.edu.ifsp.scl.persistence.transaction.TransactionData
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.Transfer

class TransactionDetailsViewModel : ViewModel() {
    private val selectedTransaction = MutableLiveData<TransactionData>()
    fun selectedTransaction() = selectedTransaction as LiveData<TransactionData>
    infix fun selectTransaction(transaction: TransactionData) {
        // TODO: Check equality with mediated fields (2 way data binding)
        // TODO: Notify before change, or just update on database (if changed)
        selectedTransaction.value = transaction
    }

    // TODO: Check if can do 2 way data binding in more fields

    val kindIcon = selectedTransaction mappedTo { TODO() } // TODO

    val category = selectedTransaction mediatedBy { it.category }

    val title = selectedTransaction mediatedBy { it.title }

    val value = selectedTransaction mediatedBy { it.value.toString() }

    val account = selectedTransaction mappedTo { "ToDo" } // TODO

    val recipientAccount = selectedTransaction mappedTo { "ToDo" } // TODO

    val transferFieldsShoulBeVisible = selectedTransaction mappedTo { it.kind == Transfer }

    val date = selectedTransaction mappedTo { "ToDo" } // TODO

    val frequency = selectedTransaction mappedTo { "ToDo" } // TODO

    val times = selectedTransaction mappedTo { "ToDo" } // TODO

    private infix fun <T, R> LiveData<T>.mappedTo(transform: (T) -> R) =
        Transformations.map(this) { transform(it) }

    private infix fun <T, R> LiveData<T>.mediatedBy(transform: (T) -> R) =
        MediatorLiveData<R>().apply {
            addSource(this@mediatedBy) { value = transform(it) }
        } as MutableLiveData<R>
}
