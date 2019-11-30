package br.edu.ifsp.scl.persistence.statement

import android.util.Range
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Query
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.statement.StatementProvider.RelativeTransactionKind
import br.edu.ifsp.scl.persistence.transaction.*
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.*
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import br.edu.ifsp.scl.persistence.transaction.TransferenceData.RelativeKind.*
import java.util.*

@Dao
internal abstract class StatementDao : StatementProvider {
    //region All accounts selects
    @Query("select * from CreditEntity where startDate <= :startDate")
    abstract fun allCreditTransactionsBefore(startDate: Date): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity where startDate <= :startDate")
    abstract fun allDebitTransactionsBefore(startDate: Date): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity where startDate <= :startDate")
    abstract fun allTransferenceTransactionsBefore(startDate: Date): LiveData<List<TransferenceEntity>>

    //region With category
    @Query("select * from CreditEntity where category in (:categories) and startDate <= :startDate")
    abstract fun allCreditTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity where category in (:categories) and startDate <= :startDate")
    abstract fun allDebitTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity where category in (:categories) and startDate <= :startDate")
    abstract fun allTransferenceTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<TransferenceEntity>>
    //endregion
    //endregion

    //region Specific account selects
    @Query("select * from CreditEntity where accountId = :accountId and startDate <= :startDate")
    abstract fun allCreditTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity where accountId = :accountId and startDate <= :startDate")
    abstract fun allDebitTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity where (accountId = :accountId or recipientAccountId = :accountId) and startDate <= :startDate")
    abstract fun allTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<TransferenceEntity>>

    @Query("select * from TransferenceEntity where accountId = :accountId and startDate <= :startDate")
    abstract fun allSentTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<TransferenceEntity>>

    @Query("select * from TransferenceEntity where recipientAccountId = :accountId and startDate <= :startDate")
    abstract fun allReceivedTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<TransferenceEntity>>

    //region With category
    @Query("select * from CreditEntity where accountId = :accountId and category in (:categories) and startDate <= :startDate")
    abstract fun allCreditTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<CreditEntity>>

    @Query("select * from DebitEntity where accountId = :accountId and category in (:categories) and startDate <= :startDate")
    abstract fun allDebitTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<DebitEntity>>

    @Query("select * from TransferenceEntity where accountId = :accountId and category in (:categories) and startDate <= :startDate")
    abstract fun allSentTransferenceTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<TransferenceEntity>>

    @Query("select * from TransferenceEntity where recipientAccountId = :accountId and category in (:categories) and startDate <= :startDate")
    abstract fun allReceivedTransferenceTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<TransferenceEntity>>
    //endregion
    //endregion

    //region Balance at date
    override infix fun totalBalanceAt(date: Date) =
        Transformations.map(
            getAllTransactionsBefore(date) repeatingBefore date
        ) { repeatingTransactions ->
            repeatingTransactions.sumByDouble { it.relativeValue }
        } as LiveData<Double>

    override fun balanceAt(account: AccountData, date: Date): LiveData<Double> =
        Transformations.map(
            account getAllTransactionsBefore date repeatingBefore date
        ) { repeatingTransactions ->
            repeatingTransactions.sumByDouble { it relativeValueTo account }
        } as LiveData<Double>

    private infix fun RepeatingTransaction.relativeValueTo(account: AccountData) =
        if (this.transaction is TransferenceEntity)
            when (this.transaction kindRelativeTo account) {
                Sent -> -value
                Received -> value
                Unrelated -> 0.0
            }
        else relativeValue

    private val RepeatingTransaction.relativeValue get() = when (kind) {
        Credit -> value
        Debit -> -value
        Transfer -> 0.0
    }
    //endregion

    //region Amounts in date range
    override infix fun amountsIn(range: Range<Date>) =
        transactionsIn(range) sumOfValuesGroupedBy { it.kind }

    override fun categoriesAmountsIn(range: Range<Date>, kind: Kind) =
        transactionsIn(range, kind) sumOfValuesGroupedBy { it.category }

    override fun amountsIn(account: AccountData, range: Range<Date>) = transactionsIn(account, range) sumOfValuesGroupedBy {
        when (it.kind) {
            Credit -> RelativeTransactionKind.Credit
            Debit -> RelativeTransactionKind.Debit
            Transfer ->
                if (it.transaction as TransferenceData kindRelativeTo account == Sent) RelativeTransactionKind.SentTransference
                else RelativeTransactionKind.ReceivedTransference
        }
    }

    override fun categoriesAmountsIn(account: AccountData, range: Range<Date>, kind: RelativeTransactionKind) =
        transactionsIn(account, range, kind) sumOfValuesGroupedBy { it.category }

    private infix fun <K> LiveData<List<RepeatingTransaction>>.sumOfValuesGroupedBy(chooser: (RepeatingTransaction) -> K) =
        Transformations.map(this) { repeatingTransactions ->
            repeatingTransactions
                .groupBy(chooser) { it.value }
                .mapValues { it.value.sum() }
        } as LiveData<Map<K, Double>>
    //endregion

    //region Transactions in date range
    override fun transactionsIn(range: Range<Date>, kind: Kind?, categories: List<String>?) = when {
        kind == null -> getAllTransactionsBefore(range.upper)
        categories == null || categories.isEmpty() -> transactionsOfKindBefore(kind, range.upper)
        else -> transactionsOfKindWithCategoriesBefore(kind, categories, range.upper)
    } repeatingWhenAffect range

    private fun transactionsOfKindBefore(kind: Kind, date: Date) = when (kind) {
        Credit -> allCreditTransactionsBefore(date)
        Debit -> allDebitTransactionsBefore(date)
        Transfer -> allTransferenceTransactionsBefore(date)
    }

    private fun transactionsOfKindWithCategoriesBefore(kind: Kind, categories: List<String>, date: Date) = when (kind) {
        Credit -> allCreditTransactionsWithCategoriesBefore(categories, date)
        Debit -> allDebitTransactionsWithCategoriesBefore(categories, date)
        Transfer -> allTransferenceTransactionsWithCategoriesBefore(categories, date)
    }

    override fun transactionsIn(account: AccountData, range: Range<Date>, kind: RelativeTransactionKind?, categories: List<String>?) = when {
        kind == null -> account getAllTransactionsBefore range.upper
        categories == null || categories.isEmpty() -> account.transactionsOfKindBefore(kind, range.upper)
        else -> account.transactionsOfKindWithCategoriesBefore(kind, categories, range.upper)
    } repeatingWhenAffect range

    private fun AccountData.transactionsOfKindBefore(kind: RelativeTransactionKind, date: Date) = when (kind) {
        RelativeTransactionKind.Credit -> allCreditTransactionsOfAccountBefore(id, date)
        RelativeTransactionKind.Debit -> allDebitTransactionsOfAccountBefore(id, date)
        RelativeTransactionKind.SentTransference -> allSentTransferenceTransactionsOfAccountBefore(id, date)
        RelativeTransactionKind.ReceivedTransference -> allReceivedTransferenceTransactionsOfAccountBefore(id, date)
    }

    private fun AccountData.transactionsOfKindWithCategoriesBefore(kind: RelativeTransactionKind, categories: List<String>, date: Date) = when (kind) {
        RelativeTransactionKind.Credit -> allCreditTransactionsOfAccountWithCategoriesBefore(id, categories, date)
        RelativeTransactionKind.Debit -> allDebitTransactionsOfAccountWithCategoriesBefore(id, categories, date)
        RelativeTransactionKind.SentTransference -> allSentTransferenceTransactionsOfAccountWithCategoriesBefore(id, categories, date)
        RelativeTransactionKind.ReceivedTransference -> allReceivedTransferenceTransactionsOfAccountWithCategoriesBefore(id, categories, date)
    }
    //endregion

    //region Common
    override fun allTransactionsBefore(date: Date, account: AccountData?): LiveData<List<RepeatingTransaction>> =
        (account?.getAllTransactionsBefore(date) ?: this getAllTransactionsBefore date) repeatingBefore date

    private infix fun getAllTransactionsBefore(date: Date) = MediatorLiveData<List<TransactionEntity>>().apply {
        var credits = listOf<CreditEntity>()
        var debits = listOf<DebitEntity>()
        var transfers = listOf<TransferenceEntity>()

        fun union() = (credits + debits + transfers)

        addSource(allCreditTransactionsBefore(date)) {
            credits = it
            value = union()
        }
        addSource(allDebitTransactionsBefore(date)) {
            debits = it
            value = union()
        }
        addSource(allTransferenceTransactionsBefore(date)) {
            transfers = it
            value = union()
        }
    } as LiveData<List<TransactionEntity>>

    private infix fun AccountData.getAllTransactionsBefore(date: Date) = MediatorLiveData<List<TransactionEntity>>().apply {
        var credits = listOf<CreditEntity>()
        var debits = listOf<DebitEntity>()
        var transfers = listOf<TransferenceEntity>()

        fun union() = (credits + debits + transfers)

        addSource(allCreditTransactionsOfAccountBefore(id, date)) {
            credits = it
            value = union()
        }
        addSource(allDebitTransactionsOfAccountBefore(id, date)) {
            debits = it
            value = union()
        }
        addSource(allTransferenceTransactionsOfAccountBefore(id, date)) {
            transfers = it
            value = union()
        }
    } as LiveData<List<TransactionEntity>>
    //endregion
}