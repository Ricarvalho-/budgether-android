package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.*
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionsOfAllAccountsFilterTest : DatabaseTest() {
    private val transactions get() = transactionsOf()

    private fun transactionsOf(kind: Kind? = null, categories: List<String>? = null) =
        statementDao.transactionsIn(defaultRange, kind, categories) getting { observedValue }

    private fun repeating(transaction: TransactionEntity) = RepeatingTransaction(transaction, defaultDate, 1)

    @Test
    fun transactionsShouldAppearWhenInsideDateRange() {
        val credit = credit()
        transactions shouldContain repeating(credit)
    }

    @Test
    fun transactionsShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange)
        credit(at = dateAfterRange)
        assertTrue(transactions?.isEmpty() ?: false)
    }

    @Test
    fun transactionsShouldIncludeOfAllAccounts() {
        val atOneAccount = credit()
        val atAnotherAccount = credit()
        transactions.shouldContain(
            repeating(atOneAccount),
            repeating(atAnotherAccount)
        )
    }

    @Test
    fun onlyTransactionsOfSpecificKindShouldAppearWhenInsideDateRange() {
        val credit = repeating(credit())
        val debit = repeating(debit())
        val transfer = repeating(transfer())

        val credits = transactionsOf(Credit)
        credits shouldContain credit
        credits.shouldNotContain(debit, transfer)

        val debits = transactionsOf(Debit)
        debits shouldContain debit
        debits.shouldNotContain(credit, transfer)

        val transfers = transactionsOf(Transfer)
        transfers shouldContain transfer
        transfers.shouldNotContain(credit, debit)
    }

    @Test
    fun transactionsOfSpecificKindShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange)
        credit(at = dateAfterRange)
        debit(at = dateBeforeRange)
        debit(at = dateAfterRange)
        transfer(at = dateBeforeRange)
        transfer(at = dateAfterRange)

        assertTrue(transactionsOf(Credit)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Debit)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Transfer)?.isEmpty() ?: false)
    }

    @Test
    fun transactionsOfSpecificKindShouldIncludeOfAllAccounts() {
        val atOneAccount = credit()
        val atAnotherAccount = credit()
        transactionsOf(Credit).shouldContain(
            repeating(atOneAccount),
            repeating(atAnotherAccount)
        )
    }

    @Test
    fun onlyTransactionsOfSpecificKindFilteredByCategoriesShouldAppearWhenInsideDateRange() {
        val desiredCategory = "Desired"
        val desiredCredit = repeating(credit(about = desiredCategory))
        val anotherCredit = repeating(credit())
        val desiredDebit = repeating(debit(about = desiredCategory))
        val anotherDebit = repeating(debit())
        val desiredTransfer = repeating(transfer(about = desiredCategory))
        val anotherTransfer = repeating(transfer())

        val category = listOf(desiredCategory)
        val credits = transactionsOf(Credit, category)
        credits shouldContain desiredCredit
        credits.shouldNotContain(anotherCredit, desiredDebit, anotherDebit, desiredTransfer, anotherTransfer)

        val debits = transactionsOf(Debit, category)
        debits shouldContain desiredDebit
        debits.shouldNotContain(desiredCredit, anotherCredit, anotherDebit, desiredTransfer, anotherTransfer)

        val transfers = transactionsOf(Transfer, category)
        transfers shouldContain desiredTransfer
        transfers.shouldNotContain(desiredCredit, anotherCredit, desiredDebit, anotherDebit, anotherTransfer)
    }

    @Test
    fun transactionsOfSpecificKindFilteredByMoreThanCategoryShouldAppearWhenInsideDateRange() {
        val someCategory = "Category"
        val someCredit = repeating(credit(about = someCategory))
        val anotherCredit = repeating(credit())
        val someDebit = repeating(debit(about = someCategory))
        val anotherDebit = repeating(debit())
        val someTransfer = repeating(transfer(about = someCategory))
        val anotherTransfer = repeating(transfer())

        val categories = listOf(someCategory, defaultTitle)
        val credits = transactionsOf(Credit, categories)
        credits.shouldContain(someCredit, anotherCredit)
        credits.shouldNotContain(someDebit, anotherDebit, someTransfer, anotherTransfer)

        val debits = transactionsOf(Debit, categories)
        debits.shouldContain(someDebit, anotherDebit)
        debits.shouldNotContain(someCredit, anotherCredit, someTransfer, anotherTransfer)

        val transfers = transactionsOf(Transfer, categories)
        transfers.shouldContain(someTransfer, anotherTransfer)
        transfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit)
    }

    @Test
    fun allTransactionsOfSpecificKindFilteredByNoneCategoryShouldAppearWhenInsideDateRange() {
        val someCategory = "Category"
        val someCredit = repeating(credit(about = someCategory))
        val anotherCredit = repeating(credit())
        val someDebit = repeating(debit(about = someCategory))
        val anotherDebit = repeating(debit())
        val someTransfer = repeating(transfer(about = someCategory))
        val anotherTransfer = repeating(transfer())

        val credits = transactionsOf(Credit, listOf())
        credits.shouldContain(someCredit, anotherCredit)
        credits.shouldNotContain(someDebit, anotherDebit, someTransfer, anotherTransfer)

        val debits = transactionsOf(Debit, listOf())
        debits.shouldContain(someDebit, anotherDebit)
        debits.shouldNotContain(someCredit, anotherCredit, someTransfer, anotherTransfer)

        val transfers = transactionsOf(Transfer, listOf())
        transfers.shouldContain(someTransfer, anotherTransfer)
        transfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit)
    }

    @Test
    fun transactionsOfSpecificKindFilteredByCategoriesShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange)
        credit(at = dateAfterRange)
        debit(at = dateBeforeRange)
        debit(at = dateAfterRange)
        transfer(at = dateBeforeRange)
        transfer(at = dateAfterRange)

        val category = listOf(defaultTitle)
        assertTrue(transactionsOf(Credit, category)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Debit, category)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Transfer, category)?.isEmpty() ?: false)
    }

    @Test
    fun transactionsOfSpecificKindFilteredByCategoriesShouldIncludeOfAllAccounts() {
        val atOneAccount = credit()
        val atAnotherAccount = credit()
        transactionsOf(Credit, listOf(defaultTitle)).shouldContain(
            repeating(atOneAccount),
            repeating(atAnotherAccount)
        )
    }
}