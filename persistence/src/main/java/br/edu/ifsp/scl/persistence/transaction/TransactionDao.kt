package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction.*

@Dao
interface TransactionDao {
    @Insert
    fun insert(credit: Credit): Long

    @Insert
    fun insert(debit: Debit): Long

    @Insert
    fun insert(transference: Transference): Long

    @Query("select * from Credit")
    fun allCreditTransactions(): LiveData<List<Credit>>

    @Query("select * from Debit")
    fun allDebitTransactions(): LiveData<List<Debit>>

    @Query("select * from Transference")
    fun allTransferenceTransactions(): LiveData<List<Transference>>

    @Query("select * from Credit where accountId = :accountId")
    fun allCreditTransactionsOfAccount(accountId: Long): LiveData<List<Credit>>

    @Query("select * from Debit where accountId = :accountId")
    fun allDebitTransactionsOfAccount(accountId: Long): LiveData<List<Debit>>

    @Query("select * from Transference where accountId = :accountId or destinationAccountId = :accountId")
    fun allTransferenceTransactionsOfAccount(accountId: Long): LiveData<List<Transference>>

    @Query("select distinct title from Credit where title like '%'||:like||'%' order by title")
    fun allCreditTitles(like: String = ""): LiveData<List<String>>

    @Query("select distinct title from Debit where title like '%'||:like||'%' order by title")
    fun allDebitTitles(like: String = ""): LiveData<List<String>>

    @Query("select distinct title from Transference where title like '%'||:like||'%' order by title")
    fun allTransferenceTitles(like: String = ""): LiveData<List<String>>

    @Query("select distinct category from Credit where category like '%'||:like||'%' order by category")
    fun allCreditCategories(like: String = ""): LiveData<List<String>>

    @Query("select distinct category from Debit where category like '%'||:like||'%' order by category")
    fun allDebitCategories(like: String = ""): LiveData<List<String>>

    @Query("select distinct category from Transference where category like '%'||:like||'%' order by category")
    fun allTransferenceCategories(like: String = ""): LiveData<List<String>>

    @Query("select category from Credit union select category from Debit union select category from Transference order by category")
    fun allTransactionCategories(): LiveData<List<String>>

    @Query("select category from Credit where accountId = :accountId union select category from Debit where accountId = :accountId union select category from Transference where accountId = :accountId or destinationAccountId = :accountId order by category")
    fun allTransactionCategoriesOfAccount(accountId: Long): LiveData<List<String>>

    @Update
    fun update(credit: Credit)

    @Update
    fun update(debit: Debit)

    @Update
    fun update(transference: Transference)

    @Delete
    fun delete(credit: Credit)

    @Delete
    fun delete(debit: Debit)

    @Delete
    fun delete(transference: Transference)
}

fun TransactionDao.insert(transaction: Transaction) = when (transaction) {
    is Credit -> insert(transaction)
    is Debit -> insert(transaction)
    is Transference -> insert(transaction)
}

// Should not be used by app
fun TransactionDao.allTransactions() = MediatorLiveData<List<Transaction>>().apply {
    var credits = listOf<Credit>()
    var debits = listOf<Debit>()
    var transferences = listOf<Transference>()

    fun sortedUnion() = (credits + debits + transferences).sortedBy { it.startDate }

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
    var credits = listOf<Credit>()
    var debits = listOf<Debit>()
    var transferences = listOf<Transference>()

    fun sortedUnion() = (credits + debits + transferences).sortedBy { it.startDate }

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

fun TransactionDao.update(transaction: Transaction) = when (transaction) {
    is Credit -> update(transaction)
    is Debit -> update(transaction)
    is Transference -> update(transaction)
}

fun TransactionDao.delete(transaction: Transaction) = when (transaction) {
    is Credit -> delete(transaction)
    is Debit -> delete(transaction)
    is Transference -> delete(transaction)
}