package br.edu.ifsp.scl.persistence.account

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: Account): Long

    @Query("select * from Account")
    fun allAccounts(): LiveData<List<Account>>

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}