package br.edu.ifsp.scl.persistence.transaction

import androidx.lifecycle.LiveData
import br.edu.ifsp.scl.persistence.Repository
import br.edu.ifsp.scl.persistence.account.AccountData

interface TransactionRepository : Repository {
    //region Titles
    fun allCreditTitles(like: String = ""): LiveData<List<String>>

    fun allDebitTitles(like: String = ""): LiveData<List<String>>

    fun allTransferenceTitles(like: String = ""): LiveData<List<String>>
    //endregion

    //region Categories
    fun allCreditCategories(like: String = ""): LiveData<List<String>>

    fun allDebitCategories(like: String = ""): LiveData<List<String>>

    fun allTransferenceCategories(like: String = ""): LiveData<List<String>>
    //endregion

    fun nearestTransactionOf(account: AccountData): LiveData<TransactionData>

    suspend fun insert(transaction: TransactionData): TransactionData

    suspend fun update(transaction: TransactionData)

    suspend fun delete(transaction: TransactionData)
}