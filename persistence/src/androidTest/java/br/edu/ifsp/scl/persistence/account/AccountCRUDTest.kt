package br.edu.ifsp.scl.persistence.account

import br.edu.ifsp.scl.persistence.DatabaseTest
import br.edu.ifsp.scl.persistence.observedValue
import br.edu.ifsp.scl.persistence.shouldBeDifferentFrom
import br.edu.ifsp.scl.persistence.shouldBeEqualTo
import junit.framework.AssertionFailedError
import org.junit.Test

class AccountCRUDTest : DatabaseTest() {
    private fun insertAccount(title: String = "Sample") = Account(title).run {
        copy(id = accountDao.insert(this))
    }

    @Test
    fun insertedAccountsShouldAppearInSelect() {
        val account1 = insertAccount()
        val account2 = insertAccount()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeEqualTo account1
            it.last() shouldBeEqualTo account2
        }
    }

    @Test
    fun insertedAccountsShouldBeDifferent() {
        insertAccount()
        insertAccount()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeDifferentFrom it.last()
        }
    }

    @Test(expected = AssertionFailedError::class)
    fun wrongAssertionShouldFail() {
        insertAccount()
        insertAccount()

        accountDao.allAccounts().observedValue?.let {
            it.first() shouldBeEqualTo it.last()
        }
    }

    @Test
    fun updatedAccountShouldBeUpdatedInSelect() {
        val originalAccount = insertAccount("Original")
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
        val account = insertAccount()
        accountDao.delete(account)
        assert(accountDao.allAccounts().observedValue?.isEmpty() ?: false)
    }
}