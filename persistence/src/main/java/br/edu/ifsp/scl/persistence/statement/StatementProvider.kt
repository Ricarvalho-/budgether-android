package br.edu.ifsp.scl.persistence.statement

import android.util.Range
import androidx.lifecycle.LiveData
import br.edu.ifsp.scl.persistence.Repository
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind
import java.util.*

interface StatementProvider : Repository {
    infix fun totalBalanceAt(date: Date): LiveData<Double>
    fun balanceAt(account: AccountData, date: Date): LiveData<Double>

    infix fun amountsIn(range: Range<Date>): LiveData<Map<Kind, Double>>
    fun amountsIn(account: AccountData, range: Range<Date>): LiveData<Map<RelativeTransactionKind, Double>>

    fun categoriesAmountsIn(range: Range<Date>, kind: Kind): LiveData<Map<String, Double>>
    fun categoriesAmountsIn(account: AccountData, range: Range<Date>, kind: RelativeTransactionKind): LiveData<Map<String, Double>>

    fun transactionsIn(range: Range<Date>, kind: Kind? = null, categories: List<String>? = null): LiveData<List<RepeatingTransaction>>
    fun transactionsIn(account: AccountData, range: Range<Date>, kind: RelativeTransactionKind? = null, categories: List<String>? = null): LiveData<List<RepeatingTransaction>>

    fun allTransactionsBefore(date: Date, account: AccountData? = null): LiveData<List<RepeatingTransaction>>

    enum class RelativeTransactionKind { Credit, Debit, SentTransference, ReceivedTransference }
}