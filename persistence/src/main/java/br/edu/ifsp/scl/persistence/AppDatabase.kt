package br.edu.ifsp.scl.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.edu.ifsp.scl.persistence.account.AccountDao
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.account.AccountRepository
import br.edu.ifsp.scl.persistence.statement.StatementDao
import br.edu.ifsp.scl.persistence.statement.StatementProvider
import br.edu.ifsp.scl.persistence.transaction.TransactionDao
import br.edu.ifsp.scl.persistence.transaction.TransactionDaoForTests
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import br.edu.ifsp.scl.persistence.transaction.TransactionRepository
import org.jetbrains.annotations.TestOnly

@Database(version = 1, entities = [
    AccountEntity::class,
    CreditEntity::class,
    DebitEntity::class,
    TransferenceEntity::class
])
@TypeConverters(Converters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val transactionDao: TransactionDao
    abstract val statementDao: StatementDao
    @TestOnly abstract fun transactionDaoForTests(): TransactionDaoForTests

    companion object {
        @Volatile private var appDatabaseInstance: AppDatabase? = null

        infix fun from(context: Context) = synchronized(this) {
            appDatabaseInstance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "main_database"
            ).build().also { appDatabaseInstance = it }
        }
    }
}

object FromRepositories {
    inline infix fun <reified T : Repository> with(context: Context): T {
        return when (T::class) {
            AccountRepository::class -> context.accountRepository
            TransactionRepository::class -> context.transactionRepository
            StatementProvider::class -> context.statementProvider
            else -> throw IllegalArgumentException()
        } as T
    }

    val Context.accountRepository: AccountRepository
        get() = with(AppDatabase from this) { accountDao }

    val Context.transactionRepository: TransactionRepository
        get() = with(AppDatabase from this) { transactionDao }

    val Context.statementProvider: StatementProvider
        get() = with(AppDatabase from this) { statementDao }
}

interface Repository