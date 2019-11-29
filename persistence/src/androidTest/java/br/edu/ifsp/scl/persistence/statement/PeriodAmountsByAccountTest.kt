package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.statement.StatementDao.RelativeTransactionKind
import br.edu.ifsp.scl.persistence.statement.StatementDao.RelativeTransactionKind.*
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PeriodAmountsByAccountTest : DatabaseTest() {
    private lateinit var account: Account

    private val amounts get() = statementDao.amountsIn(account, defaultRange) getting { observedValue }

    private fun amountsIn(kind: RelativeTransactionKind) =
        statementDao.categoriesAmountsIn(account, defaultRange, kind) getting { observedValue }

    @Before
    fun createAccount() {
        account = insertAccount()
    }

    @Test
    fun accountAmountsShouldBeGroupedByKind() {
        credit(into = account)
        debit(from = account)
        transfer(from = account)
        transfer(to = account)
        amounts shouldBe mapOf(
            Pair(Credit, defaultValue),
            Pair(Debit, defaultValue),
            Pair(SentTransference, defaultValue),
            Pair(ReceivedTransference, defaultValue)
        )
    }

    @Test
    fun accountAmountsShouldAppearWhenInsideDateRange() {
        credit(into = account)
        amounts?.get(Credit) shouldBe defaultValue
    }

    @Test
    fun accountAmountsShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange, into = account)
        credit(at = dateAfterRange, into = account)
        assertTrue(amounts?.isEmpty() ?: false)
    }

    @Test
    fun accountAmountsShouldBeIndependentOfAnotherAccounts() {
        credit(into = account)
        credit()
        debit(from = account)
        debit()
        transfer(from = account)
        transfer(to = account)
        transfer()
        amounts shouldBe mapOf(
            Pair(Credit, defaultValue),
            Pair(Debit, defaultValue),
            Pair(SentTransference, defaultValue),
            Pair(ReceivedTransference, defaultValue)
        )
    }

    @Test
    fun accountAmountsOfSpecificKindShouldBeGroupedByCategories() {
        val category = "Category"
        credit(into = account, about = category)
        credit(into = account)
        debit(from = account, about = category)
        debit(from = account)
        transfer(from = account, about = category)
        transfer(from = account)
        transfer(to = account, about = category)
        transfer(to = account)

        amountsIn(Credit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )

        amountsIn(Debit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )

        amountsIn(SentTransference) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )

        amountsIn(ReceivedTransference) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )
    }

    @Test
    fun accountAmountsOfSpecificKindShouldAppearWhenInsideDateRange() {
        credit(into = account)
        amountsIn(Credit)?.get(defaultTitle) shouldBe defaultValue
    }

    @Test
    fun accountAmountsOfSpecificKindShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange, into = account)
        credit(at = dateAfterRange, into = account)
        assertTrue(amountsIn(Credit)?.isEmpty() ?: false)
    }

    @Test
    fun accountAmountsOfSpecificKindShouldBeIndependentOfAnotherAccounts() {
        credit(into = account)
        credit()
        debit(from = account)
        debit()
        transfer(from = account)
        transfer(to = account)
        transfer()

        amountsIn(Credit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue)
        )

        amountsIn(Debit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue)
        )

        amountsIn(SentTransference) shouldBe mapOf(
            Pair(defaultTitle, defaultValue)
        )

        amountsIn(ReceivedTransference) shouldBe mapOf(
            Pair(defaultTitle, defaultValue)
        )
    }
}