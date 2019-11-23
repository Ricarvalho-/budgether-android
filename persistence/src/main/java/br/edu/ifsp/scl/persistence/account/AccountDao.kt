package br.edu.ifsp.scl.persistence.account

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Insert
    fun insert(account: Account): Long

    @Query("select * from Account")
    fun allAccounts(): LiveData<List<Account>>

    @Update
    fun update(account: Account)

    @Delete
    fun delete(account: Account)
}