package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import java.util.*
import kotlin.Comparator
import kotlin.math.absoluteValue

@Dao
interface TransactionDao {
    //region Insert
    @Insert
    suspend fun insert(credit: Credit): Long

    @Insert
    suspend fun insert(debit: Debit): Long

    @Insert
    suspend fun insert(transference: Transference): Long
    //endregion

    //region All transactions
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
    //endregion

    //region Most recent transaction
    @Query("select *, abs(julianday() - julianday(startDate / 1000, 'unixepoch')) as distanceFromToday from Credit where accountId = :accountId order by distanceFromToday limit 1")
    fun nearestCreditTransactionOfAccount(accountId: Long): LiveData<Credit>

    @Query("select *, abs(julianday() - julianday(startDate / 1000, 'unixepoch')) as distanceFromToday from Debit where accountId = :accountId order by distanceFromToday limit 1")
    fun nearestDebitTransactionOfAccount(accountId: Long): LiveData<Debit>

    @Query("select *, abs(julianday() - julianday(startDate / 1000, 'unixepoch')) as distanceFromToday from Transference where (accountId = :accountId or destinationAccountId = :accountId) order by distanceFromToday limit 1")
    fun nearestTransferenceTransactionOfAccount(accountId: Long): LiveData<Transference>
    //endregion

    //region Titles
    @Query("select distinct title from Credit where title like '%'||:like||'%' order by title")
    fun allCreditTitles(like: String = ""): LiveData<List<String>>

    @Query("select distinct title from Debit where title like '%'||:like||'%' order by title")
    fun allDebitTitles(like: String = ""): LiveData<List<String>>

    @Query("select distinct title from Transference where title like '%'||:like||'%' order by title")
    fun allTransferenceTitles(like: String = ""): LiveData<List<String>>
    //endregion

    //region Categories
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
    //endregion

    //region Update
    @Update
    suspend fun update(credit: Credit)

    @Update
    suspend fun update(debit: Debit)

    @Update
    suspend fun update(transference: Transference)
    //endregion

    //region Delete
    @Delete
    suspend fun delete(credit: Credit)

    @Delete
    suspend fun delete(debit: Debit)

    @Delete
    suspend fun delete(transference: Transference)
    //endregion
}

suspend fun TransactionDao.insert(transaction: Transaction) = when (transaction) {
    is Credit -> insert(transaction)
    is Debit -> insert(transaction)
    is Transference -> insert(transaction)
}

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

fun TransactionDao.nearestTransactionOf(account: Account) = MediatorLiveData<Transaction>().apply {
    infix fun Transaction.orNearest(other: Transaction?): Transaction {
        return if (other == null) this
        else minOf(this, other, Comparator { t1, t2 ->
            val t1Distance = t1.startDate.time - Date().time
            val t2Distance = t2.startDate.time - Date().time
            t1Distance.absoluteValue.compareTo(t2Distance.absoluteValue)
        })
    }

    addSource(nearestCreditTransactionOfAccount(account.id)) { value = it orNearest value }
    addSource(nearestDebitTransactionOfAccount(account.id)) { value = it orNearest value }
    addSource(nearestTransferenceTransactionOfAccount(account.id)) { value = it orNearest value }
} as LiveData<Transaction>

suspend fun TransactionDao.update(transaction: Transaction) = when (transaction) {
    is Credit -> update(transaction)
    is Debit -> update(transaction)
    is Transference -> update(transaction)
}

suspend fun TransactionDao.delete(transaction: Transaction) = when (transaction) {
    is Credit -> delete(transaction)
    is Debit -> delete(transaction)
    is Transference -> delete(transaction)
}