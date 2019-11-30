package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.Dao
import androidx.room.Query
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import org.jetbrains.annotations.TestOnly

@Dao
internal abstract class TransactionDaoForTests {
    @TestOnly @Query("select * from CreditEntity")
    abstract fun allCreditTransactions(): LiveData<List<CreditEntity>>

    @TestOnly @Query("select * from DebitEntity")
    abstract fun allDebitTransactions(): LiveData<List<DebitEntity>>

    @TestOnly @Query("select * from TransferenceEntity")
    abstract fun allTransferenceTransactions(): LiveData<List<TransferenceEntity>>

    @TestOnly @Query("select * from CreditEntity where accountId = :accountId")
    abstract fun allCreditTransactionsOfAccount(accountId: Long): LiveData<List<CreditEntity>>

    @TestOnly @Query("select * from DebitEntity where accountId = :accountId")
    abstract fun allDebitTransactionsOfAccount(accountId: Long): LiveData<List<DebitEntity>>

    @TestOnly @Query("select * from TransferenceEntity where accountId = :accountId or recipientAccountId = :accountId")
    abstract fun allTransferenceTransactionsOfAccount(accountId: Long): LiveData<List<TransferenceEntity>>

    @TestOnly @Query("select category from CreditEntity where accountId = :accountId union select category from DebitEntity where accountId = :accountId union select category from TransferenceEntity where accountId = :accountId or recipientAccountId = :accountId order by category")
    abstract fun allTransactionCategoriesOfAccount(accountId: Long): LiveData<List<String>>

    @TestOnly fun allTransactions() = MediatorLiveData<List<TransactionEntity>>().apply {
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

    @TestOnly fun allTransactionsOf(account: AccountEntity) = MediatorLiveData<List<TransactionEntity>>().apply {
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
}