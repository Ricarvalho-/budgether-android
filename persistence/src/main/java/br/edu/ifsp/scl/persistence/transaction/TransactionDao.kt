package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import br.edu.ifsp.scl.persistence.account.Account

@Dao
interface TransactionDao {
    @Insert
    fun insert(credit: Transaction.Credit): Long

    @Insert
    fun insert(debit: Transaction.Debit): Long

    @Insert
    fun insert(transference: Transaction.Transference): Long

    @Query("select * from Credit")
    fun allCreditTransactions(): LiveData<List<Transaction.Credit>>

    @Query("select * from Debit")
    fun allDebitTransactions(): LiveData<List<Transaction.Debit>>

    @Query("select * from Transference")
    fun allTransferenceTransactions(): LiveData<List<Transaction.Transference>>

    @Query("select * from Credit where accountId = :accountId")
    fun allCreditTransactionsOfAccount(accountId: Long): LiveData<List<Transaction.Credit>>

    @Query("select * from Debit where accountId = :accountId")
    fun allDebitTransactionsOfAccount(accountId: Long): LiveData<List<Transaction.Debit>>

    @Query("select * from Transference where accountId = :accountId or destinationAccountId = :accountId")
    fun allTransferenceTransactionsOfAccount(accountId: Long): LiveData<List<Transaction.Transference>>

    @Update
    fun update(credit: Transaction.Credit)

    @Update
    fun update(debit: Transaction.Debit)

    @Update
    fun update(transference: Transaction.Transference)

    @Delete
    fun delete(credit: Transaction.Credit)

    @Delete
    fun delete(debit: Transaction.Debit)

    @Delete
    fun delete(transference: Transaction.Transference)
}

fun TransactionDao.insert(transaction: Transaction) = when(transaction) {
    is Transaction.Credit -> insert(transaction)
    is Transaction.Debit -> insert(transaction)
    is Transaction.Transference -> insert(transaction)
}

fun TransactionDao.allTransactions() = MediatorLiveData<List<Transaction>>().apply {
    var credits = listOf<Transaction.Credit>()
    var debits = listOf<Transaction.Debit>()
    var transferences = listOf<Transaction.Transference>()

    fun sortedUnion() = (credits + debits + transferences).sortedBy { it.date }

    addSource(allCreditTransactions()) {
        credits = it
        value = sortedUnion()
    }
    addSource(allDebitTransactions()) {
        debits = it
        value = sortedUnion()
    }
    addSource(allTransferenceTransactions()) {
        transferences = it
        value = sortedUnion()
    }
} as LiveData<List<Transaction>>

fun TransactionDao.allTransactionsOf(account: Account) = MediatorLiveData<List<Transaction>>().apply {
    var credits = listOf<Transaction.Credit>()
    var debits = listOf<Transaction.Debit>()
    var transferences = listOf<Transaction.Transference>()

    fun sortedUnion() = (credits + debits + transferences).sortedBy { it.date }

    addSource(allCreditTransactionsOfAccount(account.id)) {
        credits = it
        value = sortedUnion()
    }
    addSource(allDebitTransactionsOfAccount(account.id)) {
        debits = it
        value = sortedUnion()
    }
    addSource(allTransferenceTransactionsOfAccount(account.id)) {
        transferences = it
        value = sortedUnion()
    }
} as LiveData<List<Transaction>>

fun TransactionDao.update(transaction: Transaction) = when(transaction) {
    is Transaction.Credit -> update(transaction)
    is Transaction.Debit -> update(transaction)
    is Transaction.Transference -> update(transaction)
}

fun TransactionDao.delete(transaction: Transaction) = when(transaction) {
    is Transaction.Credit -> delete(transaction)
    is Transaction.Debit -> delete(transaction)
    is Transaction.Transference -> delete(transaction)
}