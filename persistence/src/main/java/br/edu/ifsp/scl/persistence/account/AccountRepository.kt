package br.edu.ifsp.scl.persistence.account

import androidx.lifecycle.LiveData
import br.edu.ifsp.scl.persistence.Repository

interface AccountRepository : Repository {
    suspend fun insert(account: AccountData): AccountData

    fun allAccounts(): LiveData<out List<AccountData>>

    suspend fun update(account: AccountData)

    suspend fun delete(account: AccountData)
}