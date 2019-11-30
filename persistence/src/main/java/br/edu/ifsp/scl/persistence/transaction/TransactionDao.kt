package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.*
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import java.util.*
import kotlin.Comparator
import kotlin.math.absoluteValue

@Dao
internal abstract class TransactionDao : TransactionRepository {
    //region Insert
    @Insert
    abstract suspend fun insert(credit: CreditEntity): Long

    @Insert
    abstract suspend fun insert(debit: DebitEntity): Long

    @Insert
    abstract suspend fun insert(transference: TransferenceEntity): Long
    //endregion

    // TODO: Remove region
    //region All transactions
    @Query("select * from CreditEntity")
    abstract fun allCreditTransactions(): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity")
    abstract fun allDebitTransactions(): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity")
    abstract fun allTransferenceTransactions(): LiveData<List<TransferenceEntity>>

    @Query("select * from CreditEntity where accountId = :accountId")
    abstract fun allCreditTransactionsOfAccount(accountId: Long): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity where accountId = :accountId")
    abstract fun allDebitTransactionsOfAccount(accountId: Long): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity where accountId = :accountId or recipientAccountId = :accountId")
    abstract fun allTransferenceTransactionsOfAccount(accountId: Long): LiveData<List<TransferenceEntity>>
    //endregion

    //region Most recent transaction
    @Query("select * from CreditEntity where accountId = :accountId order by abs(julianday() - julianday(startDate / 1000, 'unixepoch')) limit 1")
    abstract fun nearestCreditTransactionOfAccount(accountId: Long): LiveData<CreditEntity>

    @Query("select *from DebitEntity where accountId = :accountId order by abs(julianday() - julianday(startDate / 1000, 'unixepoch')) limit 1")
    abstract fun nearestDebitTransactionOfAccount(accountId: Long): LiveData<DebitEntity>

    @Query("select * from TransferenceEntity where (accountId = :accountId or recipientAccountId = :accountId) order by abs(julianday() - julianday(startDate / 1000, 'unixepoch')) limit 1")
    abstract fun nearestTransferenceTransactionOfAccount(accountId: Long): LiveData<TransferenceEntity>
    //endregion

    //region Titles
    @Query("select distinct title from CreditEntity where title like '%'||:like||'%' order by title")
    abstract override fun allCreditTitles(like: String): LiveData<List<String>>

    @Query("select distinct title from DebitEntity where title like '%'||:like||'%' order by title")
    abstract override fun allDebitTitles(like: String): LiveData<List<String>>

    @Query("select distinct title from TransferenceEntity where title like '%'||:like||'%' order by title")
    abstract override fun allTransferenceTitles(like: String): LiveData<List<String>>
    //endregion

    //region Categories
    @Query("select distinct category from CreditEntity where category like '%'||:like||'%' order by category")
    abstract override fun allCreditCategories(like: String): LiveData<List<String>>

    @Query("select distinct category from DebitEntity where category like '%'||:like||'%' order by category")
    abstract override fun allDebitCategories(like: String): LiveData<List<String>>

    @Query("select distinct category from TransferenceEntity where category like '%'||:like||'%' order by category")
    abstract override fun allTransferenceCategories(like: String): LiveData<List<String>>

    // TODO: Remove
    @Query("select category from CreditEntity union select category from DebitEntity union select category from TransferenceEntity order by category")
    abstract fun allTransactionCategories(): LiveData<List<String>>

    // TODO: Remove
    @Query("select category from CreditEntity where accountId = :accountId union select category from DebitEntity where accountId = :accountId union select category from TransferenceEntity where accountId = :accountId or recipientAccountId = :accountId order by category")
    abstract fun allTransactionCategoriesOfAccount(accountId: Long): LiveData<List<String>>
    //endregion

    //region Update
    @Update
    abstract suspend fun update(credit: CreditEntity)

    @Update
    abstract suspend fun update(debit: DebitEntity)

    @Update
    abstract suspend fun update(transference: TransferenceEntity)
    //endregion

    //region Delete
    @Delete
    abstract suspend fun delete(credit: CreditEntity)

    @Delete
    abstract suspend fun delete(debit: DebitEntity)

    @Delete
    abstract suspend fun delete(transference: TransferenceEntity)
    //endregion

    override suspend fun insert(transaction: TransactionData): TransactionData = when (transaction.kind) {
        Credit -> insert(transaction.entity as CreditEntity)
        Debit -> insert(transaction.entity as DebitEntity)
        Transfer -> insert(transaction.entity as TransferenceEntity)
    }.let {
        when (val entity = transaction.entity) {
            is CreditEntity -> entity.copy(id = it)
            is DebitEntity -> entity.copy(id = it)
            is TransferenceEntity -> entity.copy(id = it)
        }
    }

    // TODO: Remove
    fun allTransactions() = MediatorLiveData<List<TransactionEntity>>().apply {
        var credits = listOf<CreditEntity>()
        var debits = listOf<DebitEntity>()
        var transferences = listOf<TransferenceEntity>()

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
    } as LiveData<List<TransactionEntity>>

    // TODO: Remove
    fun allTransactionsOf(account: AccountEntity) = MediatorLiveData<List<TransactionEntity>>().apply {
        var credits = listOf<CreditEntity>()
        var debits = listOf<DebitEntity>()
        var transferences = listOf<TransferenceEntity>()

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
    } as LiveData<List<TransactionEntity>>

    override fun nearestTransactionOf(account: AccountData) = MediatorLiveData<TransactionData>().apply {
        infix fun TransactionData?.orNearest(other: TransactionData?): TransactionData? {
            return when {
                other == null -> this
                this == null -> other
                else -> minOf(this, other, Comparator { t1, t2 ->
                    val t1Distance = t1.startDate.time - Date().time
                    val t2Distance = t2.startDate.time - Date().time
                    t1Distance.absoluteValue.compareTo(t2Distance.absoluteValue)
                })
            }
        }

        addSource(nearestCreditTransactionOfAccount(account.id)) { value = it orNearest value }
        addSource(nearestDebitTransactionOfAccount(account.id)) { value = it orNearest value }
        addSource(nearestTransferenceTransactionOfAccount(account.id)) { value = it orNearest value }
    } as LiveData<TransactionData>

    override suspend fun update(transaction: TransactionData) = when (transaction.kind) {
        Credit -> update(transaction.entity as CreditEntity)
        Debit -> update(transaction as DebitEntity)
        Transfer -> update(transaction as TransferenceEntity)
    }

    override suspend fun delete(transaction: TransactionData) = when (transaction.kind) {
        Credit -> delete(transaction as CreditEntity)
        Debit -> delete(transaction as DebitEntity)
        Transfer -> delete(transaction as TransferenceEntity)
    }
}