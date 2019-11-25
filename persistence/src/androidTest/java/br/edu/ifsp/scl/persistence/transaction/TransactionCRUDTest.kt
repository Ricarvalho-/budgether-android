package br.edu.ifsp.scl.persistence.transaction

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference.RelativeKind.*
import org.junit.Before
import org.junit.Test
import java.util.*

class TransactionCRUDTest : DatabaseTest() {
    private val sampleDate = Date()
    private fun sampleTransactionData(
        account: Account,
        title: String = "Sample",
        category: String = "Sample"
    ) = Transaction.Data(title, category, 0.0, sampleDate, Transaction.Frequency.Single, 0, account.id)

    private fun insertAccount() = Account("Sample").run {
        copy(id = accountDao.insert(this))
    }

    private fun Transaction.afterInsert(): Transaction {
        val id = transactionDao.insert(this)
        return when(this) {
            is Transaction.Credit -> copy(id = id)
            is Transaction.Debit -> copy(id = id)
            is Transaction.Transference -> copy(id = id)
        }
    }

    @Test
    fun insertedTransactionsShouldAppearInSelect() {
        val data = sampleTransactionData(insertAccount())

        val credit = Transaction.Credit(data).afterInsert()
        val debit = Transaction.Debit(data).afterInsert()
        val transference = Transaction.Transference(data, insertAccount().id).afterInsert()

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit, transference)
    }

    @Test
    fun insertedTransactionsShouldAppearOnlyInRespectiveAccountsSelect() {
        val account = insertAccount()
        val secondAccount = insertAccount()
        val data = sampleTransactionData(account)

        val credit = Transaction.Credit(data).afterInsert()
        val debit = Transaction.Debit(data).afterInsert()
        val transference = Transaction.Transference(data, secondAccount.id).afterInsert()

        transactionDao.allTransactionsOf(account).observedValue?.shouldContain(credit, debit, transference)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldNotContain(credit, debit)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldContain(transference)
    }

    @Test
    fun insertedTransactionsShouldBeDifferent() {
        val data = sampleTransactionData(insertAccount())

        Transaction.Credit(data).afterInsert()
        Transaction.Credit(data).afterInsert()

        Transaction.Debit(data).afterInsert()
        Transaction.Debit(data).afterInsert()

        Transaction.Transference(data, insertAccount().id).afterInsert()
        Transaction.Transference(data, insertAccount().id).afterInsert()

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
        val originalTransaction = Transaction.Credit(
            sampleTransactionData(insertAccount(), "Original")
        ).afterInsert() as Transaction.Credit

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

        val transference = Transaction.Transference(sampleTransactionData(account), destinationAccount.id)
            .afterInsert() as Transaction.Transference

        transference kindRelativeTo account shouldBeEqualTo Sent
        transference kindRelativeTo destinationAccount shouldBeEqualTo Received
        transference kindRelativeTo unrelatedAccount shouldBeEqualTo Unrelated
    }

    @Test
    fun insertedTransactionsShouldDisappearAfterDeletion() {
        val data = sampleTransactionData(insertAccount())
        val transaction = Transaction.Credit(data).afterInsert()
        transactionDao.delete(transaction)
        assert(transactionDao.allTransactions().observedValue?.isEmpty() ?: false)
    }

    @Test
    fun deletingAccountShouldDeleteAssociatedTransactions() {
        val data = sampleTransactionData(insertAccount())
        val credit = Transaction.Credit(data).afterInsert()
        val debit = Transaction.Debit(data).afterInsert()

        val destinationAccount = insertAccount()
        val transference = Transaction.Transference(data, destinationAccount.id).afterInsert()

        accountDao.delete(destinationAccount)

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit)
        transactionDao.allTransactions().observedValue?.shouldNotContain(transference)
    }

    @Test
    fun insertedTitlesShouldAppearInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, "b")).afterInsert()

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldAppearSortedInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, "b")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, "a")).afterInsert()

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, "b")).afterInsert()

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, category = "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, category = "b")).afterInsert()

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearSortedInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, category = "b")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, category = "a")).afterInsert()

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, category = "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, category = "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(account, category = "b")).afterInsert()

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelectOfAssociatedAccounts() {
        val account = insertAccount()

        Transaction.Credit(sampleTransactionData(account, category = "a")).afterInsert()
        Transaction.Credit(sampleTransactionData(insertAccount(), category = "b")).afterInsert()

        transactionDao.allTransactionCategoriesOfAccount(account.id)
            .observedValue?.shouldBeEqualTo(listOf("a"))
    }
}