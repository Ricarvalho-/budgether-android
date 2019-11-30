package br.edu.ifsp.scl.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.edu.ifsp.scl.persistence.account.AccountDao
import br.edu.ifsp.scl.persistence.statement.StatementDao
import br.edu.ifsp.scl.persistence.transaction.TransactionDao
import br.edu.ifsp.scl.persistence.transaction.TransactionDaoForTests
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal abstract class DatabaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    lateinit var transactionDaoForTests: TransactionDaoForTests
    lateinit var transactionDao: TransactionDao
    lateinit var accountDao: AccountDao
    lateinit var statementDao: StatementDao

    @Before
    fun prepareDependencies() {
        createDb()
        createDAOs()
    }

    private fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    private fun createDAOs() {
        transactionDaoForTests = db.transactionDaoForTests()
        transactionDao = db.transactionDao
        accountDao = db.accountDao
        statementDao = db.statementDao
    }

    @After
    fun closeDb() {
        db.close()
    }
}