package br.edu.ifsp.scl.persistence.transaction

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference.RelativeKind.*
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class TransactionCRUDTest : DatabaseTest() {
    private val sampleDate = Date()
    private fun sampleTransactionData(
        account: Account,
        title: String = "Sample",
        category: String = "Sample"
    ) = sampleTransactionData(title, category, startDate = sampleDate, accountId = account.id)

    @Test
    fun insertedTransactionsShouldAppearInSelect() {
        val data = sampleTransactionData(insertAccount())

        val credit = insert(Transaction.Credit(data))
        val debit = insert(Transaction.Debit(data))
        val transference = insert(Transaction.Transference(data, insertAccount().id))

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit, transference)
    }

    @Test
    fun insertedTransactionsShouldAppearOnlyInRespectiveAccountsSelect() {
        val account = insertAccount()
        val secondAccount = insertAccount()
        val data = sampleTransactionData(account)

        val credit = insert(Transaction.Credit(data))
        val debit = insert(Transaction.Debit(data))
        val transference = insert(Transaction.Transference(data, secondAccount.id))

        transactionDao.allTransactionsOf(account).observedValue?.shouldContain(credit, debit, transference)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldNotContain(credit, debit)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldContain(transference)
    }

    @Test
    fun insertedTransactionsShouldBeDifferent() {
        val data = sampleTransactionData(insertAccount())

        insert(Transaction.Credit(data))
        insert(Transaction.Credit(data))

        insert(Transaction.Debit(data))
        insert(Transaction.Debit(data))

        insert(Transaction.Transference(data, insertAccount().id))
        insert(Transaction.Transference(data, insertAccount().id))

        transactionDao.allCreditTransactions().observedValue?.let {
            it.first() shouldBeDifferentFrom it.last()
        }

        transactionDao.allDebitTransactions().observedValue?.let {
            it.first() shouldBeDifferentFrom it.last()
        }

        transactionDao.allTransferenceTransactions().observedValue?.let {
            it.first() shouldBeDifferentFrom it.last()
        }
    }

    @Test
    fun updatedTransactionsShouldBeUpdatedInSelect() {
        val originalTransaction = insert(Transaction.Credit(
            sampleTransactionData(insertAccount(), "Original")
        ))

        transactionDao.allTransactions().observedValue?.first()?.let {
            it shouldBeEqualTo originalTransaction
        }

        val updatedTransaction = originalTransaction.run { copy(data.copy("Updated")) }
        transactionDao.update(updatedTransaction)
        transactionDao.allTransactions().observedValue?.first()?.let {
            it shouldBeDifferentFrom originalTransaction
            it shouldBeEqualTo updatedTransaction
        }
    }

    @Test
    fun transferenceRelativeKindShouldBeRight() {
        val account = insertAccount()
        val destinationAccount = insertAccount()
        val unrelatedAccount = insertAccount()

        val transference = insert(Transaction.Transference(sampleTransactionData(account), destinationAccount.id))

        transference kindRelativeTo account shouldBeEqualTo Sent
        transference kindRelativeTo destinationAccount shouldBeEqualTo Received
        transference kindRelativeTo unrelatedAccount shouldBeEqualTo Unrelated
    }

    @Test
    fun insertedTransactionsShouldDisappearAfterDeletion() {
        val data = sampleTransactionData(insertAccount())
        val transaction = insert(Transaction.Credit(data))
        transactionDao.delete(transaction)
        assertTrue(transactionDao.allTransactions().observedValue?.isEmpty() ?: false)
    }

    @Test
    fun deletingAccountShouldDeleteAssociatedTransactions() {
        val data = sampleTransactionData(insertAccount())
        val credit = insert(Transaction.Credit(data))
        val debit = insert(Transaction.Debit(data))

        val destinationAccount = insertAccount()
        val transference = insert(Transaction.Transference(data, destinationAccount.id))

        accountDao.delete(destinationAccount)

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit)
        transactionDao.allTransactions().observedValue?.shouldNotContain(transference)
    }

    @Test
    fun insertedTitlesShouldAppearInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, "a")))
        insert(Transaction.Credit(sampleTransactionData(account, "b")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldAppearSortedInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, "b")))
        insert(Transaction.Credit(sampleTransactionData(account, "a")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, "a")))
        insert(Transaction.Credit(sampleTransactionData(account, "a")))
        insert(Transaction.Credit(sampleTransactionData(account, "b")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, category = "a")))
        insert(Transaction.Credit(sampleTransactionData(account, category = "b")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearSortedInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, category = "b")))
        insert(Transaction.Credit(sampleTransactionData(account, category = "a")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, category = "a")))
        insert(Transaction.Credit(sampleTransactionData(account, category = "a")))
        insert(Transaction.Credit(sampleTransactionData(account, category = "b")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelectOfAssociatedAccounts() {
        val account = insertAccount()

        insert(Transaction.Credit(sampleTransactionData(account, category = "a")))
        insert(Transaction.Credit(sampleTransactionData(insertAccount(), category = "b")))

        transactionDao.allTransactionCategoriesOfAccount(account.id)
            .observedValue?.shouldBeEqualTo(listOf("a"))
    }
}