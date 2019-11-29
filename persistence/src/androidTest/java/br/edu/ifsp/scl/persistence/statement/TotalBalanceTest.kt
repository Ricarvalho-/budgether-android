package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.transaction.Transaction
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class TotalBalanceTest : DatabaseTest() {
    private val currentTotalBalance get() = totalBalanceAt(defaultDate)
    private fun totalBalanceAt(date: Date) = statementDao totalBalanceAt date getting { observedValue!! }

    @Test
    fun totalBalanceBeforeFirstTransactionShouldBeZero() {
        credit(at = date(2, 1, 2020))
        totalBalanceAt(date(1, 1, 2020)) shouldBe 0.0
    }

    @Test
    fun totalBalanceShouldIncrementAfterCredit() {
        val before = currentTotalBalance
        credit()
        val after = currentTotalBalance
        assertTrue(after > before)
    }

    @Test
    fun totalBalanceShouldDecrementAfterDebit() {
        val before = currentTotalBalance
        debit()
        val after = currentTotalBalance
        assertTrue(after < before)
    }

    @Test
    fun totalBalanceShouldStayTheSameAfterTransference() {
        val before = currentTotalBalance
        transfer()
        val after = currentTotalBalance
        assertTrue(after == before)
    }

    @Test
    fun totalBalanceAtTransactionDateShouldConsiderIt() {
        credit()
        currentTotalBalance shouldBe defaultValue
    }

    @Test
    fun totalBalanceAfterTransactionDateShouldConsiderRepetitionAndFrequency() {
        credit(repeating = Transaction.Frequency.Daily, during = 5)
        totalBalanceAt(date(6, 1, 2020)) shouldBe defaultValue * 5
    }

    @Test
    fun totalBalanceShouldConsiderRepeatingTransactionsWithDifferentFrequencies() {
        credit(repeating = Transaction.Frequency.Daily, during = 5)
        debit(repeating = Transaction.Frequency.Weekly, during = 3)
        totalBalanceAt(date(16, 1, 2020)) shouldBe defaultValue * (5 - 3)

    }

    @Test
    fun totalBalanceShouldBeEqualToSumOfAllAccountsBalances() {
        credit(20.0, into = insertAccount())
        credit(45.0, into = insertAccount())
        currentTotalBalance shouldBe 65.0
    }
}