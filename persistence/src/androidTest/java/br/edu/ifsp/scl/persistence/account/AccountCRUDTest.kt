package br.edu.ifsp.scl.persistence.account

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.edu.ifsp.scl.persistence.Database
import br.edu.ifsp.scl.persistence.observedValue
import br.edu.ifsp.scl.persistence.shouldBeDifferentFrom
import br.edu.ifsp.scl.persistence.shouldBeEqualTo
import junit.framework.AssertionFailedError
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountCRUDTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: Database
    private lateinit var accountDao: AccountDao

    @Before
    fun prepareDependencies() {
        createDb()
        accountDao = db.accountDao()
    }

    private fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun Account.afterInsert() = copy(id = accountDao.insert(this))

    @Test
    fun insertedAccountsShouldAppearInSelect() {
        val account1 = Account("Test 1").afterInsert()
        val account2 = Account("Test 2").afterInsert()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeEqualTo account1
            it.last() shouldBeEqualTo account2
        }
    }

    @Test
    fun insertedAccountsShouldBeDifferent() {
        Account("Test").afterInsert()
        Account("Test").afterInsert()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeDifferentFrom it.last()
        }
    }

    @Test(expected = AssertionFailedError::class)
    fun wrongAssertionShouldFail() {
        Account("Test").afterInsert()
        Account("Test").afterInsert()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeEqualTo it.last()
        }
    }

    @Test
    fun updatedAccountShouldBeUpdatedInSelect() {
        val originalAccount = Account("Original").afterInsert()
        accountDao.allAccounts().observedValue?.first()?.let {
            it shouldBeEqualTo originalAccount
        }

        val updatedAccount = originalAccount.copy(title = "Updated")
        accountDao.update(updatedAccount)
        accountDao.allAccounts().observedValue?.first()?.let {
            it shouldBeDifferentFrom originalAccount
            it shouldBeEqualTo updatedAccount
        }
    }

    @Test
    fun insertedAccountsShouldDisappearAfterDeletion() {
        val account = Account("Test").afterInsert()
        accountDao.delete(account)
        assert(accountDao.allAccounts().observedValue?.isEmpty() ?: false)
    }
}