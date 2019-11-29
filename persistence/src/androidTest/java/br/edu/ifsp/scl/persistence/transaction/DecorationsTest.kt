package br.edu.ifsp.scl.persistence.transaction

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.util.rangeTo
import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DecorationsTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun beforeShouldYieldOnlyResultsBeforeSpecifiedDate() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
            date(14, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2)
        ))
    }

    @Test
    fun beforeShouldYieldInclusiveResults() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
            date(15, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2),
            RepeatingTransaction(credit, date(15, 1, 2020), 3)
        ))
    }

    @Test
    fun beforeShouldRespectRepeatability() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
            date(1, 1, 2021)
        ).observedValue?.size?.shouldBeEqualTo(3)
    }

    @Test
    fun beforeShouldRespectFrequency() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Yearly,
            repeat = 2
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
            date(1, 1, 2023)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(1, 1, 2021), 2)
        ))
    }

    @Test
    fun beforeShouldYieldResultsOfMultipleTransactions() {
        val credit = Credit(sampleTransactionData())
        val debit = Debit(sampleTransactionData())
        val sampleDate = sampleTransactionData().startDate

        val transactions = liveDataListWith(credit, debit)
        transactions.repeatingBefore(
            sampleDate
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, sampleDate, 1),
            RepeatingTransaction(debit, sampleDate, 1)
        ))
    }

    @Test
    fun beforeShouldYieldOrderedResults() {
        val credit = Credit(sampleTransactionData(
            startDate = date(2, 2, 2020),
            frequency = Frequency.Monthly,
            repeat = 2
        ))

        val debit = Debit(sampleTransactionData(
            frequency = Frequency.Monthly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit, debit)
        transactions.repeatingBefore(
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
        val credit = Credit(sampleTransactionData(
            startDate = date(30, 11, 2019),
            frequency = Frequency.Monthly,
            repeat = 5
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
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
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(5, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun transactionStartingBeforeAndAffectingDateRange() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(5, 1, 2020) rangeTo date(15, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(8, 1, 2020), 2),
            RepeatingTransaction(credit, date(15, 1, 2020), 3)
        ))
    }

    @Test
    fun transactionInsideDateRange() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(2, 1, 2020), 2),
            RepeatingTransaction(credit, date(3, 1, 2020), 3)
        ))
    }

    @Test
    fun transactionStartingAtButEndingOutsideOfDateRange() {
        val credit = Credit(sampleTransactionData(
            frequency = Frequency.Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.shouldBeEqualTo(listOf(
            RepeatingTransaction(credit, date(1, 1, 2020), 1),
            RepeatingTransaction(credit, date(8, 1, 2020), 2)
        ))
    }

    @Test
    fun transactionStartingAfterDateRange() {
        val credit = Credit(sampleTransactionData(
            startDate = date(20, 1, 2020),
            frequency = Frequency.Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }
}