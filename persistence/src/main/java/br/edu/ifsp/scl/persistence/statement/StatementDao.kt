package br.edu.ifsp.scl.persistence.statement

import android.util.Range
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Query
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference.RelativeKind.*
import br.edu.ifsp.scl.persistence.transaction.repeatingWhenAffect
import br.edu.ifsp.scl.persistence.transaction.repeatingBefore
import java.util.*

@Dao
abstract class StatementDao {
    @Query("select * from Credit where startDate <= :startDate")
    internal abstract fun allCreditTransactionsBefore(startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where startDate <= :startDate")
    internal abstract fun allDebitTransactionsBefore(startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsBefore(startDate: Date): LiveData<List<Transference>>

    @Query("select * from Credit where category in (:categories) and startDate <= :startDate")
    internal abstract fun allCreditTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where category in (:categories) and startDate <= :startDate")
    internal abstract fun allDebitTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where category in (:categories) and startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsWithCategoriesBefore(categories: List<String>, startDate: Date): LiveData<List<Transference>>

    @Query("select * from Credit where accountId = :accountId and startDate <= :startDate")
    internal abstract fun allCreditTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where accountId = :accountId and startDate <= :startDate")
    internal abstract fun allDebitTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Transference>>

    @Query("select * from Credit where accountId = :accountId and category in (:categories) and startDate <= :startDate")
    internal abstract fun allCreditTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where accountId = :accountId and category in (:categories) and startDate <= :startDate")
    internal abstract fun allDebitTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and category in (:categories) and startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsOfAccountWithCategoriesBefore(accountId: Long, categories: List<String>, startDate: Date): LiveData<List<Transference>>

    infix fun totalBalanceAt(date: Date) =
        Transformations.map(
            allTransactionsBefore(date) repeatingBefore date
        ) { repeatingTransactions ->
            repeatingTransactions.sumByDouble { it.transaction.relativeValue }
        } as LiveData<Double>

    fun balanceAt(account: Account, date: Date): LiveData<Double> =
        Transformations.map(
            account allTransactionsBefore date repeatingBefore date
        ) { repeatingTransactions ->
            repeatingTransactions.sumByDouble { it.transaction relativeValueTo account }
        } as LiveData<Double>

    private infix fun Transaction.relativeValueTo(account: Account) =
        if (this is Transference)
            when (this kindRelativeTo account) {
                Sent -> -value
                Received -> value
                Unrelated -> 0.0
            }
        else relativeValue

    private val Transaction.relativeValue get() = when (this) {
        is Credit -> value
        is Debit -> -value
        is Transference -> 0.0
    }

    fun transactionsIn(range: Range<Date>, kind: TransactionKind? = null, categories: List<String>? = null) = when {
        kind == null -> allTransactionsBefore(range.upper)
        categories == null || categories.isEmpty() -> transactionsOfKindBefore(kind, range.upper)
        else -> transactionsOfKindWithCategoriesBefore(kind, categories, range.upper)
    } repeatingWhenAffect range

    private fun transactionsOfKindBefore(kind: TransactionKind, date: Date) = when (kind) {
        TransactionKind.Credit -> allCreditTransactionsBefore(date)
        TransactionKind.Debit -> allDebitTransactionsBefore(date)
        TransactionKind.Transference -> allTransferenceTransactionsBefore(date)
    }

    private fun transactionsOfKindWithCategoriesBefore(kind: TransactionKind, categories: List<String>, date: Date) = when (kind) {
        TransactionKind.Credit -> allCreditTransactionsWithCategoriesBefore(categories, date)
        TransactionKind.Debit -> allDebitTransactionsWithCategoriesBefore(categories, date)
        TransactionKind.Transference -> allTransferenceTransactionsWithCategoriesBefore(categories, date)
    }

    private infix fun allTransactionsBefore(date: Date) = MediatorLiveData<List<Transaction>>().apply {
        var credits = listOf<Credit>()
        var debits = listOf<Debit>()
        var transfers = listOf<Transference>()

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
    } as LiveData<List<Transaction>>

    private infix fun Account.allTransactionsBefore(date: Date) = MediatorLiveData<List<Transaction>>().apply {
        var credits = listOf<Credit>()
        var debits = listOf<Debit>()
        var transfers = listOf<Transference>()

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
    } as LiveData<List<Transaction>>

    enum class TransactionKind { Credit, Debit, Transference }
}