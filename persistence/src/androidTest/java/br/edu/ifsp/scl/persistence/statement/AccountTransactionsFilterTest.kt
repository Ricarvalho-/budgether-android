package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.statement.StatementProvider.RelativeTransactionKind
import br.edu.ifsp.scl.persistence.statement.StatementProvider.RelativeTransactionKind.*
import br.edu.ifsp.scl.persistence.transaction.RepeatingTransaction
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class AccountTransactionsFilterTest : DatabaseTest() {
    private lateinit var account: AccountEntity

    private val transactions get() = transactionsOf()

    private fun transactionsOf(kind: RelativeTransactionKind? = null, categories: List<String>? = null) =
        statementDao.transactionsIn(account, defaultRange, kind, categories) getting { observedValue }

    private fun repeating(transaction: TransactionEntity) = RepeatingTransaction(transaction, defaultDate, 1)

    @Before
    fun createAccount() {
        account = insertAccount()
    }

    @Test
    fun accountTransactionsShouldAppearWhenInsideDateRange() {
        val credit = credit(into = account)
        transactions shouldContain repeating(credit)
    }

    @Test
    fun accountTransactionsShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange, into = account)
        credit(at = dateAfterRange, into = account)
        assertTrue(transactions?.isEmpty() ?: false)
    }

    @Test
    fun accountTransactionsShouldBeIndependentOfAnotherAccounts() {
        val atAccount = credit(into = account)
        val atAnotherAccount = credit()

        transactions shouldContain repeating(atAccount)
        transactions shouldNotContain repeating(atAnotherAccount)
    }

    @Test
    fun onlyAccountTransactionsOfSpecificKindShouldAppearWhenInsideDateRange() {
        val credit = repeating(credit(into = account))
        val debit = repeating(debit(from = account))
        val sentTransfer = repeating(transfer(from = account))
        val receivedTransfer = repeating(transfer(to = account))

        val credits = transactionsOf(Credit)
        credits shouldContain credit
        credits.shouldNotContain(debit, sentTransfer, receivedTransfer)

        val debits = transactionsOf(Debit)
        debits shouldContain debit
        debits.shouldNotContain(credit, sentTransfer, receivedTransfer)

        val sentTransfers = transactionsOf(SentTransference)
        sentTransfers shouldContain sentTransfer
        sentTransfers.shouldNotContain(credit, debit, receivedTransfer)

        val receivedTransfers = transactionsOf(ReceivedTransference)
        receivedTransfers shouldContain receivedTransfer
        receivedTransfers.shouldNotContain(credit, debit, sentTransfer)
    }

    @Test
    fun accountTransactionsOfSpecificKindShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange, into = account)
        credit(at = dateAfterRange, into = account)
        debit(at = dateBeforeRange, from = account)
        debit(at = dateAfterRange, from = account)
        transfer(at = dateBeforeRange, from = account)
        transfer(at = dateAfterRange, from = account)
        transfer(at = dateBeforeRange, to = account)
        transfer(at = dateAfterRange, to = account)

        assertTrue(transactionsOf(Credit)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Debit)?.isEmpty() ?: false)
        assertTrue(transactionsOf(SentTransference)?.isEmpty() ?: false)
        assertTrue(transactionsOf(ReceivedTransference)?.isEmpty() ?: false)
    }

    @Test
    fun accountTransactionsOfSpecificKindShouldBeIndependentOfAnotherAccounts() {
        val atAccount = credit(into = account)
        val atAnotherAccount = credit()
        val credits = transactionsOf(Credit)

        credits shouldContain repeating(atAccount)
        credits shouldNotContain repeating(atAnotherAccount)
    }

    @Test
    fun onlyAccountTransactionsOfSpecificKindFilteredByCategoriesShouldAppearWhenInsideDateRange() {
        val desiredCategory = "Desired"
        val desiredCredit = repeating(credit(into = account, about = desiredCategory))
        val anotherCredit = repeating(credit(into = account))
        val desiredDebit = repeating(debit(from = account, about = desiredCategory))
        val anotherDebit = repeating(debit(from = account))
        val desiredSentTransfer = repeating(transfer(from = account, about = desiredCategory))
        val anotherSentTransfer = repeating(transfer(from = account))
        val desiredReceivedTransfer = repeating(transfer(to = account, about = desiredCategory))
        val anotherReceivedTransfer = repeating(transfer(to = account))

        val category = listOf(desiredCategory)
        val credits = transactionsOf(Credit, category)
        credits shouldContain desiredCredit
        credits.shouldNotContain(anotherCredit, desiredDebit, anotherDebit, desiredSentTransfer, anotherSentTransfer, desiredReceivedTransfer, anotherReceivedTransfer)

        val debits = transactionsOf(Debit, category)
        debits shouldContain desiredDebit
        debits.shouldNotContain(desiredCredit, anotherCredit, anotherDebit, desiredSentTransfer, anotherSentTransfer, desiredReceivedTransfer, anotherReceivedTransfer)

        val sentTransfers = transactionsOf(SentTransference, category)
        sentTransfers shouldContain desiredSentTransfer
        sentTransfers.shouldNotContain(desiredCredit, anotherCredit, desiredDebit, anotherDebit, anotherSentTransfer, desiredReceivedTransfer, anotherReceivedTransfer)

        val receivedTransfers = transactionsOf(ReceivedTransference, category)
        receivedTransfers shouldContain desiredReceivedTransfer
        receivedTransfers.shouldNotContain(desiredCredit, anotherCredit, desiredDebit, anotherDebit, desiredSentTransfer, anotherSentTransfer, anotherReceivedTransfer)
    }

    @Test
    fun accountTransactionsOfSpecificKindFilteredByMoreThanCategoryShouldAppearWhenInsideDateRange() {
        val someCategory = "Category"
        val someCredit = repeating(credit(into = account, about = someCategory))
        val anotherCredit = repeating(credit(into = account))
        val someDebit = repeating(debit(from = account, about = someCategory))
        val anotherDebit = repeating(debit(from = account))
        val someSentTransfer = repeating(transfer(from = account, about = someCategory))
        val anotherSentTransfer = repeating(transfer(from = account))
        val someReceivedTransfer = repeating(transfer(to = account, about = someCategory))
        val anotherReceivedTransfer = repeating(transfer(to = account))

        val categories = listOf(someCategory, defaultTitle)
        val credits = transactionsOf(Credit, categories)
        credits.shouldContain(someCredit, anotherCredit)
        credits.shouldNotContain(someDebit, anotherDebit, someSentTransfer, anotherSentTransfer, someReceivedTransfer, anotherReceivedTransfer)

        val debits = transactionsOf(Debit, categories)
        debits.shouldContain(someDebit, anotherDebit)
        debits.shouldNotContain(someCredit, anotherCredit, someSentTransfer, anotherSentTransfer, someReceivedTransfer, anotherReceivedTransfer)

        val sentTransfers = transactionsOf(SentTransference, categories)
        sentTransfers.shouldContain(someSentTransfer, anotherSentTransfer)
        sentTransfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit, someReceivedTransfer, anotherReceivedTransfer)

        val receivedTransfers = transactionsOf(ReceivedTransference, categories)
        receivedTransfers.shouldContain(someReceivedTransfer, anotherReceivedTransfer)
        receivedTransfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit, someSentTransfer, anotherSentTransfer)
    }

    @Test
    fun allAccountTransactionsOfSpecificKindFilteredByNoneCategoryShouldAppearWhenInsideDateRange() {
        val someCategory = "Category"
        val someCredit = repeating(credit(into = account, about = someCategory))
        val anotherCredit = repeating(credit(into = account))
        val someDebit = repeating(debit(from = account, about = someCategory))
        val anotherDebit = repeating(debit(from = account))
        val someSentTransfer = repeating(transfer(from = account, about = someCategory))
        val anotherSentTransfer = repeating(transfer(from = account))
        val someReceivedTransfer = repeating(transfer(to = account, about = someCategory))
        val anotherReceivedTransfer = repeating(transfer(to = account))

        val credits = transactionsOf(Credit, listOf())
        credits.shouldContain(someCredit, anotherCredit)
        credits.shouldNotContain(someDebit, anotherDebit, someSentTransfer, anotherSentTransfer, someReceivedTransfer, anotherReceivedTransfer)

        val debits = transactionsOf(Debit, listOf())
        debits.shouldContain(someDebit, anotherDebit)
        debits.shouldNotContain(someCredit, anotherCredit, someSentTransfer, anotherSentTransfer, someReceivedTransfer, anotherReceivedTransfer)

        val sentTransfers = transactionsOf(SentTransference, listOf())
        sentTransfers.shouldContain(someSentTransfer, anotherSentTransfer)
        sentTransfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit, someReceivedTransfer, anotherReceivedTransfer)

        val receivedTransfers = transactionsOf(ReceivedTransference, listOf())
        receivedTransfers.shouldContain(someReceivedTransfer, anotherReceivedTransfer)
        receivedTransfers.shouldNotContain(someCredit, anotherCredit, someDebit, anotherDebit, someSentTransfer, anotherSentTransfer)
    }

    @Test
    fun accountTransactionsOfSpecificKindFilteredByCategoriesShouldNotAppearWhenOutsideDateRange() {
        credit(at = dateBeforeRange, into = account)
        credit(at = dateAfterRange, into = account)
        debit(at = dateBeforeRange, from = account)
        debit(at = dateAfterRange, from = account)
        transfer(at = dateBeforeRange, from = account)
        transfer(at = dateAfterRange, from = account)
        transfer(at = dateBeforeRange, to = account)
        transfer(at = dateAfterRange, to = account)

        val category = listOf(defaultTitle)
        assertTrue(transactionsOf(Credit, category)?.isEmpty() ?: false)
        assertTrue(transactionsOf(Debit, category)?.isEmpty() ?: false)
        assertTrue(transactionsOf(SentTransference, category)?.isEmpty() ?: false)
        assertTrue(transactionsOf(ReceivedTransference, category)?.isEmpty() ?: false)
    }

    @Test
    fun accountTransactionsOfSpecificKindFilteredByCategoriesShouldBeIndependentOfAnotherAccounts() {
        val atAccount = credit(into = account)
        val atAnotherAccount = credit()
        val credits = transactionsOf(Credit, listOf(defaultTitle))
        credits shouldContain repeating(atAccount)
        credits shouldNotContain repeating(atAnotherAccount)
    }
}