package br.edu.ifsp.scl.persistence.statement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.room.Dao
import androidx.room.Query
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference.RelativeKind.*
import br.edu.ifsp.scl.persistence.transaction.before
import java.util.*

@Dao
abstract class StatementDao {
    @Query("select * from Credit where startDate <= :startDate")
    internal abstract fun allCreditTransactionsBefore(startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where startDate <= :startDate")
    internal abstract fun allDebitTransactionsBefore(startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsBefore(startDate: Date): LiveData<List<Transference>>

    @Query("select * from Credit where accountId = :accountId and startDate <= :startDate")
    internal abstract fun allCreditTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where accountId = :accountId and startDate <= :startDate")
    internal abstract fun allDebitTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Transference>>

    @Query("select * from Credit where accountId = :accountId and category = :category and startDate <= :startDate")
    internal abstract fun allCreditTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Credit>>

    @Query("select * from Debit where accountId = :accountId and category = :category and startDate <= :startDate")
    internal abstract fun allDebitTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and category = :category and startDate <= :startDate")
    internal abstract fun allTransferenceTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Transference>>

    infix fun totalBalanceAt(date: Date) =
        Transformations.map(
            allTransactionsBefore(date).before(date)
        ) { repeatingTransactions ->
            repeatingTransactions.sumByDouble { it.transaction.relativeValue }
        } as LiveData<Double>

    fun balanceOf(account: Account, atDate: Date): LiveData<Double> =
        Transformations.map(
            allTransactionsOfAccountBefore(account, atDate).before(atDate)
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

    private infix fun allTransactionsBefore(date: Date) = MediatorLiveData<List<Transaction>>().apply {
        var credits = listOf<Credit>()
        var debits = listOf<Debit>()
        var transferences = listOf<Transference>()

        fun union() = (credits + debits + transferences)

        addSource(allCreditTransactionsBefore(date)) {
            credits = it
            value = union()
        }
        addSource(allDebitTransactionsBefore(date)) {
            debits = it
            value = union()
        }
        addSource(allTransferenceTransactionsBefore(date)) {
            transferences = it
            value = union()
        }
    } as LiveData<List<Transaction>>

    private fun allTransactionsOfAccountBefore(account: Account, date: Date) = MediatorLiveData<List<Transaction>>().apply {
        var credits = listOf<Credit>()
        var debits = listOf<Debit>()
        var transferences = listOf<Transference>()

        fun union() = (credits + debits + transferences)

        addSource(allCreditTransactionsOfAccountBefore(account.id, date)) {
            credits = it
            value = union()
        }
        addSource(allDebitTransactionsOfAccountBefore(account.id, date)) {
            debits = it
            value = union()
        }
        addSource(allTransferenceTransactionsOfAccountBefore(account.id, date)) {
            transferences = it
            value = union()
        }
    } as LiveData<List<Transaction>>
}