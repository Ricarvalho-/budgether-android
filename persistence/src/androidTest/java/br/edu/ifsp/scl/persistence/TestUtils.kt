package br.edu.ifsp.scl.persistence

import androidx.core.util.rangeTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import br.edu.ifsp.scl.persistence.transaction.insert
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import java.util.*

infix fun Any?.shouldBe(value: Any?) = shouldBeEqualTo(value)

infix fun Any?.shouldBeEqualTo(value: Any?) = assertThat(this, equalTo(value))
infix fun Any?.shouldBeDifferentFrom(value: Any?) = assertThat(this, not(value))

infix fun Iterable<Any>?.shouldContain(item: Any?) = assertThat(this, hasItems(item))
fun Iterable<Any>?.shouldContain(vararg items: Any?) = items.forEach { this shouldContain it }

infix fun Iterable<Any>?.shouldNotContain(item: Any?) = assertThat(this, not(hasItems(item)))
fun Iterable<Any>?.shouldNotContain(vararg items: Any?) = items.forEach { this shouldNotContain it }

infix fun <T, R> T.getting(outcome: T.() -> R): R = outcome()

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

val defaultRange = defaultDate rangeTo date(31, 1, 2020)
val dateBeforeRange = date(1, 12, 2019)
val dateAfterRange = date(1, 2, 2020)

fun sampleTransactionData(
    title: String = defaultTitle,
    category: String = defaultTitle,
    value: Double = defaultValue,
    startDate: Date = defaultDate,
    frequency: Frequency = Frequency.Single,
    repeat: Int = 0,
    accountId: Long = 0
) = Data(title, category, value, startDate, frequency, repeat, accountId)

fun date(day: Int, month: Int, year: Int) = Calendar.getInstance().run {
    clear()
    set(year, month.dec(), day)
    time
} as Date

fun DatabaseTest.insertAccount(title: String = defaultTitle) = Account(title).run {
    runBlocking { copy(id = accountDao.insert(this@run)) }
}

fun <T : Transaction> DatabaseTest.insert(transaction: T): T {
    val id = runBlocking { transactionDao.insert(transaction) }
    val t = transaction as Transaction
    return when (t) {
        is Credit -> t.copy(id = id)
        is Debit -> t.copy(id = id)
        is Transference -> t.copy(id = id)
    } as T
}