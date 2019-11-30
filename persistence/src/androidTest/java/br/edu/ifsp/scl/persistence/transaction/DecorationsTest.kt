package br.edu.ifsp.scl.persistence.transaction

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.util.rangeTo
import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency.*
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.CreditEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.DebitEntity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DecorationsTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun beforeShouldYieldOnlyResultsBeforeSpecifiedDate() {
        val credit = CreditEntity(sampleTransactionData(
            frequency = Weekly,
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
        val credit = CreditEntity(sampleTransactionData(
            frequency = Weekly,
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
        val credit = CreditEntity(sampleTransactionData(
            frequency = Weekly,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingBefore(
            date(1, 1, 2021)
        ).observedValue?.size?.shouldBeEqualTo(3)
    }

    @Test
    fun beforeShouldRespectFrequency() {
        val credit = CreditEntity(sampleTransactionData(
            frequency = Yearly,
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
        val credit = CreditEntity(sampleTransactionData())
        val debit = DebitEntity(sampleTransactionData())
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
        val credit = CreditEntity(sampleTransactionData(
            startDate = date(2, 2, 2020),
            frequency = Monthly,
            repeat = 2
        ))

        val debit = DebitEntity(sampleTransactionData(
            frequency = Monthly,
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
        val credit = CreditEntity(sampleTransactionData(
            startDate = date(30, 11, 2019),
            frequency = Monthly,
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
        val credit = CreditEntity(sampleTransactionData(
            frequency = Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(5, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }

    @Test
    fun transactionStartingBeforeAndAffectingDateRange() {
        val credit = CreditEntity(sampleTransactionData(
            frequency = Weekly,
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
        val credit = CreditEntity(sampleTransactionData(
            frequency = Daily,
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
        val credit = CreditEntity(sampleTransactionData(
            frequency = Weekly,
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
        val credit = CreditEntity(sampleTransactionData(
            startDate = date(20, 1, 2020),
            frequency = Daily,
            repeat = 3
        ))

        val transactions = liveDataListWith(credit)
        transactions.repeatingWhenAffect(
            date(1, 1, 2020) rangeTo date(10, 1, 2020)
        ).observedValue?.let { assertTrue(it.isEmpty()) }
    }
}