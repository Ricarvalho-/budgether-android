package br.edu.ifsp.scl.persistence.statement

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import br.edu.ifsp.scl.persistence.transaction.Transaction
import java.util.*

@Dao
interface StatementDao {
    @Query("select * from Credit where startDate <= :startDate")
    fun allCreditTransactionsBefore(startDate: Date): LiveData<List<Transaction.Credit>>

    @Query("select * from Debit where startDate <= :startDate")
    fun allDebitTransactionsBefore(startDate: Date): LiveData<List<Transaction.Debit>>

    @Query("select * from Transference where startDate <= :startDate")
    fun allTransferenceTransactionsBefore(startDate: Date): LiveData<List<Transaction.Transference>>

    @Query("select * from Credit where accountId = :accountId and startDate <= :startDate")
    fun allCreditTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Transaction.Credit>>

    @Query("select * from Debit where accountId = :accountId and startDate <= :startDate")
    fun allDebitTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Transaction.Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and startDate <= :startDate")
    fun allTransferenceTransactionsOfAccountBefore(accountId: Long, startDate: Date): LiveData<List<Transaction.Transference>>

    @Query("select * from Credit where accountId = :accountId and category = :category and startDate <= :startDate")
    fun allCreditTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Transaction.Credit>>

    @Query("select * from Debit where accountId = :accountId and category = :category and startDate <= :startDate")
    fun allDebitTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Transaction.Debit>>

    @Query("select * from Transference where (accountId = :accountId or destinationAccountId = :accountId) and category = :category and startDate <= :startDate")
    fun allTransferenceTransactionsOfAccountWithCategoryBefore(accountId: Long, category: String, startDate: Date): LiveData<List<Transaction.Transference>>
}