package br.edu.ifsp.scl.persistence.transaction

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import br.edu.ifsp.scl.persistence.transaction.TransferenceData.RelativeKind.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class TransactionCRUDTest : DatabaseTest() {
    private val sampleDate = Date()
    private fun sampleTransactionData(
        account: AccountEntity,
        title: String = "Sample",
        category: String = "Sample",
        date: Date = sampleDate
    ) = sampleTransactionData(title, category, startDate = date, accountId = account.id)

    @Test
    fun insertedTransactionsShouldAppearInSelect() {
        val data = sampleTransactionData(insertAccount())

        val credit = insert(CreditEntity(data))
        val debit = insert(DebitEntity(data))
        val transference = insert(TransferenceEntity(data, insertAccount().id))

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit, transference)
    }

    @Test
    fun insertedTransactionsShouldAppearOnlyInRespectiveAccountsSelect() {
        val account = insertAccount()
        val secondAccount = insertAccount()
        val data = sampleTransactionData(account)

        val credit = insert(CreditEntity(data))
        val debit = insert(DebitEntity(data))
        val transference = insert(TransferenceEntity(data, secondAccount.id))

        transactionDao.allTransactionsOf(account).observedValue?.shouldContain(credit, debit, transference)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldNotContain(credit, debit)
        transactionDao.allTransactionsOf(secondAccount).observedValue?.shouldContain(transference)
    }

    @Test
    fun insertedTransactionsShouldBeDifferent() {
        val data = sampleTransactionData(insertAccount())

        insert(CreditEntity(data))
        insert(CreditEntity(data))

        insert(DebitEntity(data))
        insert(DebitEntity(data))

        insert(TransferenceEntity(data, insertAccount().id))
        insert(TransferenceEntity(data, insertAccount().id))

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
    fun nearestTransactionShouldBeTheOneWithCloserStartDate() {
        fun Int.daysFromNow() = Calendar.getInstance().also { it.add(Calendar.DATE, this) }.time

        val account = insertAccount()
        val beforeYesterdayTransaction = sampleTransactionData(account, date = (-2).daysFromNow())
        val yesterdayTransaction = sampleTransactionData(account, date = (-1).daysFromNow())
        val tomorrowTransaction = sampleTransactionData(account, date = 1.daysFromNow())
        val afterTomorrowTransaction = sampleTransactionData(account, date = 2.daysFromNow())

        insert(CreditEntity(beforeYesterdayTransaction))
        val yesterdayCredit = insert(CreditEntity(yesterdayTransaction))
        insert(CreditEntity(afterTomorrowTransaction))

        insert(DebitEntity(beforeYesterdayTransaction))
        val tomorrowDebit = insert(DebitEntity(tomorrowTransaction))
        insert(DebitEntity(afterTomorrowTransaction))

        insert(TransferenceEntity(beforeYesterdayTransaction, insertAccount().id))
        val tomorrowSentTransference = insert(TransferenceEntity(tomorrowTransaction, insertAccount().id))
        insert(TransferenceEntity(afterTomorrowTransaction, insertAccount().id))

        transactionDao.nearestCreditTransactionOfAccount(account.id).observedValue?.let {
            it shouldBeEqualTo yesterdayCredit
        }

        transactionDao.nearestDebitTransactionOfAccount(account.id).observedValue?.let {
            it shouldBeEqualTo tomorrowDebit
        }

        transactionDao.nearestTransferenceTransactionOfAccount(account.id).observedValue?.let {
            it shouldBeEqualTo tomorrowSentTransference
        }

        val todayReceivedTransference = insert(TransferenceEntity(sampleTransactionData(insertAccount(), date = Date()), account.id))

        transactionDao.nearestTransactionOf(account).observedValue?.let {
            it shouldBeEqualTo todayReceivedTransference
        }
    }

    @Test
    fun updatedTransactionsShouldBeUpdatedInSelect() {
        val originalTransaction = insert(CreditEntity(
            sampleTransactionData(insertAccount(), "Original")
        ))

        transactionDao.allTransactions().observedValue?.first()?.let {
            it shouldBeEqualTo originalTransaction
        }

        val updatedTransaction = originalTransaction.run { copy(data.copy("Updated")) }
        runBlocking { transactionDao.update(updatedTransaction) }
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

        val transference = insert(TransferenceEntity(sampleTransactionData(account), destinationAccount.id))

        transference kindRelativeTo account shouldBeEqualTo Sent
        transference kindRelativeTo destinationAccount shouldBeEqualTo Received
        transference kindRelativeTo unrelatedAccount shouldBeEqualTo Unrelated
    }

    @Test
    fun insertedTransactionsShouldDisappearAfterDeletion() {
        val data = sampleTransactionData(insertAccount())
        val transaction = insert(CreditEntity(data))
        runBlocking { transactionDao.delete(transaction) }
        assertTrue(transactionDao.allTransactions().observedValue?.isEmpty() ?: false)
    }

    @Test
    fun deletingAccountShouldDeleteAssociatedTransactions() {
        val data = sampleTransactionData(insertAccount())
        val credit = insert(CreditEntity(data))
        val debit = insert(DebitEntity(data))

        val destinationAccount = insertAccount()
        val transference = insert(TransferenceEntity(data, destinationAccount.id))

        runBlocking { accountDao.delete(destinationAccount) }

        transactionDao.allTransactions().observedValue?.shouldContain(credit, debit)
        transactionDao.allTransactions().observedValue?.shouldNotContain(transference)
    }

    @Test
    fun insertedTitlesShouldAppearInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, "a")))
        insert(CreditEntity(sampleTransactionData(account, "b")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldAppearSortedInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, "b")))
        insert(CreditEntity(sampleTransactionData(account, "a")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedTitlesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, "a")))
        insert(CreditEntity(sampleTransactionData(account, "a")))
        insert(CreditEntity(sampleTransactionData(account, "b")))

        transactionDao.allCreditTitles().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditTitles("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, category = "a")))
        insert(CreditEntity(sampleTransactionData(account, category = "b")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearSortedInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, category = "b")))
        insert(CreditEntity(sampleTransactionData(account, category = "a")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldNotAppearDuplicatedInSelect() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, category = "a")))
        insert(CreditEntity(sampleTransactionData(account, category = "a")))
        insert(CreditEntity(sampleTransactionData(account, category = "b")))

        transactionDao.allCreditCategories().observedValue?.shouldBeEqualTo(listOf("a", "b"))
        transactionDao.allCreditCategories("a").observedValue?.shouldBeEqualTo(listOf("a"))
    }

    @Test
    fun insertedCategoriesShouldAppearInSelectOfAssociatedAccounts() {
        val account = insertAccount()

        insert(CreditEntity(sampleTransactionData(account, category = "a")))
        insert(CreditEntity(sampleTransactionData(insertAccount(), category = "b")))

        transactionDao.allTransactionCategoriesOfAccount(account.id)
            .observedValue?.shouldBeEqualTo(listOf("a"))
    }
}