package br.edu.ifsp.scl.persistence.transaction

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.util.rangeTo
import br.edu.ifsp.scl.persistence.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DecorationsTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun beforeShouldYieldOnlyResultsBeforeSpecifiedDate() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.before(
            date(14, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2)
        ))
    }

    @Test
    fun beforeShouldYieldInclusiveResults() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.before(
            date(15, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2),
            RepeatingTransaction(credit, date(15, 1, 2020), 3)
        ))
    }

    @Test
    fun beforeShouldRespectRepeatability() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.before(
            date(1, 1, 2021)
        ).observedValue?.size?.shouldBeEqualTo(3)
    }

    @Test
    fun beforeShouldRespectFrequency() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Yearly,
            repeat = 2
        ))

        val transactions = liveDataListWith(credit)
        transactions.before(
            date(1, 1, 2023)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(1, 1, 2021), 2)
        ))
    }

    @Test
    fun beforeShouldYieldResultsOfMultipleTransactions() {
        val credit = Transaction.Credit(sampleTransactionData())
        val debit = Transaction.Debit(sampleTransactionData())
        val sampleDate = sampleTransactionData().startDate

        val transactions = liveDataListWith(credit, debit)
        transactions.before(
            sampleDate
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, sampleDate, 1),
            RepeatingTransaction(debit, sampleDate, 1)
        ))
    }

    @Test
    fun beforeShouldYieldOrderedResults() {
        val credit = Transaction.Credit(sampleTransactionData(
            startDate = date(2, 2, 2020),
            frequency = Transaction.Frequency.Monthly,
            repeat = 2
        ))

        val debit = Transaction.Debit(sampleTransactionData(
            frequency = Transaction.Frequency.Monthly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit, debit)
        transactions.before(
            date(1, 1, 2021)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(debit, date(1, 1, 2020), 1),
            RepeatingTransaction(debit, date(1, 2, 2020), 2),
            RepeatingTransaction(credit, date(2, 2, 2020), 1),
            RepeatingTransaction(debit, date(1, 3, 2020), 3),
            RepeatingTransaction(credit, date(2, 3, 2020), 2)
        ))
    }

    @Test
    fun beforeShouldYieldMostReasonableResultsForNonexistentDates() {
        val credit = Transaction.Credit(sampleTransactionData(
            startDate = date(30, 11, 2019),
            frequency = Transaction.Frequency.Monthly,
            repeat = 5
        ))

        val transactions = liveDataListWith(credit)
        transactions.before(
            date(1, 1, 2021)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(30, 11, 2019), 1),
            RepeatingTransaction(credit, date(30, 12, 2019), 2),
            RepeatingTransaction(credit, date(30, 1, 2020), 3),
            RepeatingTransaction(credit, date(29, 2, 2020), 4),
            RepeatingTransaction(credit, date(30, 3, 2020), 5)
        ))
    }

    @Test
    fun transactionStartingBeforeButNotAffectingDateRange() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.affecting(
            date(5, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun transactionStartingBeforeAndAffectingDateRange() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.affecting(
            date(5, 1, 2020) rangeTo date(15, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(8, 1, 2020), 2),
            RepeatingTransaction(credit, date(15, 1, 2020), 3)
        ))
    }

    @Test
    fun transactionInsideDateRange() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.affecting(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(2, 1, 2020), 2),
            RepeatingTransaction(credit, date(3, 1, 2020), 3)
        ))
    }

    @Test
    fun transactionStartingAtButEndingOutsideOfDateRange() {
        val credit = Transaction.Credit(sampleTransactionData(
            frequency = Transaction.Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.affecting(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2)
        ))
    }

    @Test
    fun transactionStartingAfterDateRange() {
        val credit = Transaction.Credit(sampleTransactionData(
            startDate = date(20, 1, 2020),
            frequency = Transaction.Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.affecting(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }
}