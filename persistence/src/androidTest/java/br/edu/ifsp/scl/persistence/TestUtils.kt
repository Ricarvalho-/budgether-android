package br.edu.ifsp.scl.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.insert
import org.hamcrest.CoreMatchers.*
import java.util.*


infix fun Any?.shouldBeEqualTo(value: Any?) = assertThat(this, equalTo(value))
infix fun Any?.shouldBeDifferentFrom(value: Any?) = assertThat(this, not(value))

infix fun Iterable<Any>?.shouldContain(item: Any?) = assertThat(this, hasItems(item))
fun Iterable<Any>?.shouldContain(vararg items: Any?) = items.forEach { this shouldContain it }

infix fun Iterable<Any>?.shouldNotContain(item: Any?) = assertThat(this, not(hasItems(item)))
fun Iterable<Any>?.shouldNotContain(vararg items: Any?) = items.forEach { this shouldNotContain it }

val <T> LiveData<T>.observedValue: T?
    get() {
        observeForever {}
        return value
    }

fun <T> liveDataListWith(vararg values: T) =
    MutableLiveData<List<T>>().apply {
        value = values.asList()
    } as LiveData<List<T>>

const val defaultTitle = "Sample"
const val defaultValue = 10.0
val defaultDate = date(1, 1, 2020)

fun sampleTransactionData(
    title: String = defaultTitle,
    category: String = defaultTitle,
    value: Double = defaultValue,
    startDate: Date = defaultDate,
    frequency: Transaction.Frequency = Transaction.Frequency.Single,
    repeat: Int = 0,
    accountId: Long = 0
) = Transaction.Data(title, category, value, startDate, frequency, repeat, accountId)

fun date(day: Int, month: Int, year: Int) = Calendar.getInstance().run {
    clear()
    set(year, month.dec(), day)
    time
} as Date

fun DatabaseTest.insertAccount(title: String = defaultTitle) = Account(title).run {
    copy(id = accountDao.insert(this))
}

fun <T : Transaction> DatabaseTest.insert(transaction: T): T {
    val id = transactionDao.insert(transaction)
    val t = transaction as Transaction
    return when (t) {
        is Transaction.Credit -> t.copy(id = id)
        is Transaction.Debit -> t.copy(id = id)
        is Transaction.Transference -> t.copy(id = id)
    } as T
}