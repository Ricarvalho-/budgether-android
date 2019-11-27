package br.edu.ifsp.scl.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.account.AccountDao
import br.edu.ifsp.scl.persistence.statement.StatementDao
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import br.edu.ifsp.scl.persistence.transaction.TransactionDao

@Database(version = 1, entities = [
    Account::class,
    Credit::class,
    Debit::class,
    Transference::class
])
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun statementDao(): StatementDao
}