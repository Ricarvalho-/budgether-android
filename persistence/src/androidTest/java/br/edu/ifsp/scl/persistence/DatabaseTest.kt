package br.edu.ifsp.scl.persistence

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.edu.ifsp.scl.persistence.account.AccountDao
import br.edu.ifsp.scl.persistence.statement.StatementDao
import br.edu.ifsp.scl.persistence.transaction.TransactionDao
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class DatabaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    internal lateinit var transactionDao: TransactionDao
    internal lateinit var accountDao: AccountDao
    internal lateinit var statementDao: StatementDao

    @Before
    fun prepareDependencies() {
        createDb()
        createDaos()
    }

    private fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    private fun createDaos() {
        transactionDao = db.transactionDao
        accountDao = db.accountDao
        statementDao = db.statementDao
    }

    @After
    fun closeDb() {
        db.close()
    }
}