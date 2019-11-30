package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.*
import org.junit.Assert.assertTrue
import org.junit.Test

class PeriodAmountsOfAllAccountsTest : DatabaseTest() {
    private val amounts get() = statementDao amountsIn defaultRange getting { observedValue }

    private fun amountsIn(kind: Kind) =
        statementDao.categoriesAmountsIn(defaultRange, kind) getting { observedValue }

    @Test
    fun amountsShouldBeGroupedByKind() {
        credit()
        debit()
        transfer()
        amounts shouldBe mapOf(
            Pair(Credit, defaultValue),
            Pair(Debit, defaultValue),
            Pair(Transfer, defaultValue)
        )
    }

    @Test
    fun amountsShouldAppearWhenInsideDateRange() {
        credit()
        amounts?.get(Credit) shouldBe defaultValue
    }

    @Test
    fun amountsShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange)
        credit(at = dateAfterRange)
        assertTrue(amounts?.isEmpty() ?: false)
    }

    @Test
    fun amountsShouldBeEqualToSumOfAllAccountsAmounts() {
        credit()
        credit()
        debit()
        debit()
        transfer()
        transfer()
        amounts shouldBe mapOf(
            Pair(Credit, defaultValue * 2),
            Pair(Debit, defaultValue * 2),
            Pair(Transfer, defaultValue * 2)
        )
    }

    @Test
    fun amountsOfSpecificKindShouldBeGroupedByCategories() {
        val category = "Category"
        credit(about = category)
        credit()
        debit(about = category)
        debit()
        transfer(about = category)
        transfer()

        amountsIn(Credit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )

        amountsIn(Debit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )

        amountsIn(Transfer) shouldBe mapOf(
            Pair(defaultTitle, defaultValue),
            Pair(category, defaultValue)
        )
    }

    @Test
    fun amountsOfSpecificKindShouldAppearWhenInsideDateRange() {
        credit()
        amountsIn(Credit)?.get(defaultTitle) shouldBe defaultValue
    }

    @Test
    fun amountsOfSpecificKindShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange)
        credit(at = dateAfterRange)
        assertTrue(amountsIn(Credit)?.isEmpty() ?: false)
    }

    @Test
    fun amountsOfSpecificKindShouldBeEqualToSumOfAllAccountsAmounts() {
        credit()
        credit()
        debit()
        debit()
        transfer()
        transfer()

        amountsIn(Credit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue * 2)
        )

        amountsIn(Debit) shouldBe mapOf(
            Pair(defaultTitle, defaultValue * 2)
        )

        amountsIn(Transfer) shouldBe mapOf(
            Pair(defaultTitle, defaultValue * 2)
        )
    }
}