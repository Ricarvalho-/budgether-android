package br.edu.ifsp.scl.persistence.account

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
internal abstract class AccountDao : AccountRepository {
    @Insert
    abstract suspend fun insertAccount(entity: AccountEntity): Long

    @Query("select * from AccountEntity")
    abstract fun selectAllAccounts(): LiveData<List<AccountEntity>>

    @Update
    abstract suspend fun updateAccount(account: AccountEntity)

    @Delete
    abstract suspend fun deleteAccount(account: AccountEntity)

    override suspend fun insert(account: AccountData) = AccountEntity(account.title, insertAccount(account.entity)) as AccountData

    override fun allAccounts(): LiveData<out List<AccountData>> = selectAllAccounts()

    override suspend fun update(account: AccountData) = updateAccount(account.entity)

    override suspend fun delete(account: AccountData) = deleteAccount(account.entity)
}