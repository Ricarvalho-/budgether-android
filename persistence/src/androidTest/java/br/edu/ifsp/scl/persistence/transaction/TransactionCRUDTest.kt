package br.edu.ifsp.scl.persistence.transaction

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.account.AccountDao
import br.edu.ifsp.scl.persistence.transaction.Transaction.Transference.RelativeKind.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class TransactionCRUDTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: Database
    private lateinit var transactionDao: TransactionDao
    private lateinit var accountDao: AccountDao
    private lateinit var sampleAccount: Account
    private lateinit var sampleSecondAccount: Account
    private lateinit var sampleData: Transaction.Data

    @Before
    fun prepareDependencies() {
        createDb()
        createDaos()
        prepareSamples()
    }

    private fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
    }

    private fun createDaos() {
        transactionDao = db.transactionDao()
        accountDao = db.accountDao()
    }

    private fun prepareSamples() {
        sampleAccount = Account("Account 1").run {
            copy(id = accountDao.insert(this))
        }
        sampleSecondAccount = Account("Account 2").run {
            copy(id = accountDao.insert(this))
        }
        sampleData = Transaction.Data(
            "Test", "Test", 10.5, Date(),
            Transaction.Frequency.Single, 0, sampleAccount.id
        )
    }

    @After
    fun closeDb() {
        db.close()
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
        val credit = Transaction.Credit(sampleData).afterInsert()
        val debit = Transaction.Debit(sampleData).afterInsert()
        val transference = Transaction.Transference(sampleData, sampleSecondAccount.id).afterInsert()

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit, transference)
    }

    @Test
    fun insertedTransactionsShouldAppearOnlyInRespectiveAccountsSelect() {
        val credit = Transaction.Credit(sampleData).afterInsert()
        val debit = Transaction.Debit(sampleData).afterInsert()
        val transference = Transaction.Transference(sampleData, sampleSecondAccount.id).afterInsert()

        transactionDao.allTransactionsOf(sampleAccount)
            .observedValue?.shouldContain(credit, debit, transference)

        transactionDao.allTransactionsOf(sampleSecondAccount)
            .observedValue?.shouldNotContain(credit, debit)

        transactionDao.allTransactionsOf(sampleSecondAccount)
            .observedValue?.shouldContain(transference)
    }

    @Test
    fun insertedTransactionsShouldBeDifferent() {
        Transaction.Credit(sampleData).afterInsert()
        Transaction.Credit(sampleData).afterInsert()

        Transaction.Debit(sampleData).afterInsert()
        Transaction.Debit(sampleData).afterInsert()

        Transaction.Transference(sampleData, sampleSecondAccount.id).afterInsert()
        Transaction.Transference(sampleData, sampleSecondAccount.id).afterInsert()

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
        val originalTransaction = Transaction.Credit(sampleData).afterInsert() as Transaction.Credit
        transactionDao.allTransactions().observedValue?.first()?.let {
            it shouldBeEqualTo originalTransaction
        }

        val updatedTransaction = originalTransaction.copy(originalTransaction.data.copy("Updated"))
        transactionDao.update(updatedTransaction)
        transactionDao.allTransactions().observedValue?.first()?.let {
            it shouldBeDifferentFrom originalTransaction
            it shouldBeEqualTo updatedTransaction
        }
    }

    @Test
    fun transferenceRelativeKindShouldBeRight() {
        val unrelatedAccount = Account("Account 3").run {
            copy(id = accountDao.insert(this))
        }
        val transference = Transaction.Transference(sampleData, sampleSecondAccount.id)
            .afterInsert() as Transaction.Transference
        transference kindRelativeTo sampleAccount shouldBeEqualTo Sent
        transference kindRelativeTo sampleSecondAccount shouldBeEqualTo Received
        transference kindRelativeTo unrelatedAccount shouldBeEqualTo Unrelated
    }

    @Test
    fun insertedTransactionsShouldDisappearAfterDeletion() {
        val transaction = Transaction.Credit(sampleData).afterInsert()
        transactionDao.delete(transaction)
        assert(transactionDao.allTransactions().observedValue?.isEmpty() ?: false)
    }

    @Test
    fun deletingAccountShouldDeleteAssociatedTransactions() {
        val credit = Transaction.Credit(sampleData).afterInsert()
        val debit = Transaction.Debit(sampleData).afterInsert()
        val transference = Transaction.Transference(sampleData, sampleSecondAccount.id).afterInsert()

        accountDao.delete(sampleSecondAccount)

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit)
        transactionDao.allTransactions().observedValue?.shouldNotContain(transference)
    }

    @Test
    fun insertedTitlesShouldAppearInSelect() {
        Transaction.Credit(sampleData.copy("b")).afterInsert()
        Transaction.Credit(sampleData.copy("b")).afterInsert()
        Transaction.Credit(sampleData.copy("a")).afterInsert()

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelect() {
        Transaction.Credit(sampleData.copy(category = "b")).afterInsert()
        Transaction.Credit(sampleData.copy(category = "b")).afterInsert()
        Transaction.Credit(sampleData.copy(category = "a")).afterInsert()

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelectOfAssociatedAccounts() {
        Transaction.Credit(sampleData.copy(category = "b")).afterInsert()
        Transaction.Credit(sampleData.copy(category = "a")).afterInsert()
        Transaction.Credit(sampleData.copy(category = "a")).afterInsert()
        Transaction.Debit(sampleData.copy(category = "b")).afterInsert()
        Transaction.Debit(sampleData.copy(category = "a", accountId = sampleSecondAccount.id)).afterInsert()
        Transaction.Debit(sampleData.copy(category = "a", accountId = sampleSecondAccount.id)).afterInsert()

        transactionDao.allTransactionCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allTransactionCategoriesOfAccount(sampleSecondAccount.id)
            .observedValue?.shouldBeEqualTo(listOf("a"))
    }
}