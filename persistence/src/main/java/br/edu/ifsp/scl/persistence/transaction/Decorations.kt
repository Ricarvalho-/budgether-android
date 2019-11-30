package br.edu.ifsp.scl.persistence.transaction

import android.util.Range
import androidx.core.util.rangeTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Many.Times.Determinate
import br.edu.ifsp.scl.persistence.transaction.Repeatability.Many.Times.Indeterminate
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency.*
import java.util.*

internal infix fun <T : TransactionEntity> LiveData<out List<T>>.repeatingBefore(date: Date) = Transformations.map(this) { items ->
    items.flatMap { it.repeatWhileAffecting(it.startDate.coerceAtMost(date) rangeTo date) }
        .sortedBy { it.atDate }
} as LiveData<List<RepeatingTransaction>>

internal infix fun <T : TransactionEntity> LiveData<out List<T>>.repeatingWhenAffect(dateRange: Range<Date>) = Transformations.map(this) { items ->
    items.flatMap { it.repeatWhileAffecting(dateRange) }
        .sortedBy { it.atDate }
} as LiveData<List<RepeatingTransaction>>

private infix fun TransactionEntity.repeatWhileAffecting(dateRange: Range<Date>) = sequence {
    var currentDate = startDate
    var currentRepetition = 1

    fun notExceededRangeEnd() = currentDate <= dateRange.upper
    fun notReachedEndOfRepetitions() = when (frequency) {
        Single -> currentRepetition == 1
        else -> isIndeterminate || currentRepetition <= repeat
    }
    fun mayAffectRange() = notExceededRangeEnd() && notReachedEndOfRepetitions()

    while (mayAffectRange()) {
        if (currentDate in dateRange)
            yield(
                RepeatingTransaction(
                    this@repeatWhileAffecting,
                    currentDate,
                    currentRepetition
                )
            )

        currentDate = startDate.shiftedBy(frequency, currentRepetition)
        currentRepetition++
    }
}.toList()

private fun Date.shiftedBy(frequency: Frequency, amount: Int) = when (frequency) {
    Single -> this
    Daily -> this plusDays amount
    Weekly -> this plusWeeks amount
    Monthly -> this plusMonths amount
    Yearly -> this plusYears amount
}

private infix fun Date.plusDays(amount: Int) = calendar.apply { add(Calendar.DAY_OF_MONTH, amount) }.time
private infix fun Date.plusWeeks(amount: Int) = calendar.apply { add(Calendar.WEEK_OF_YEAR, amount) }.time
private infix fun Date.plusMonths(amount: Int) = calendar.apply { add(Calendar.MONTH, amount) }.time
private infix fun Date.plusYears(amount: Int) = calendar.apply { add(Calendar.YEAR, amount) }.time
private val Date.calendar get() = Calendar.getInstance().also { it.time = this }

data class RepeatingTransaction(val transaction: TransactionData, val atDate: Date, private val number: Int) : TransactionData by transaction {
    val repeatability by lazy {
        when (transaction.frequency) {
            Single -> Repeatability.Single
            else -> Repeatability.Many(
                number,
                times
            )
        }
    }

    private val times get() =
        if (transaction.isIndeterminate) Indeterminate
        else Determinate(
            transaction.repeat
        )
}

sealed class Repeatability {
    object Single : Repeatability()
    class Many(val number: Int, val of: Times) : Repeatability() {
        sealed class Times {
            object Indeterminate : Times()
            class Determinate(val amount: Int) : Times()
        }
    }
}