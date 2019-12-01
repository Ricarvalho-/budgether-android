package br.edu.ifsp.scl.account.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.scl.account.list.AccountsAdapter.AccountSummary
import br.edu.ifsp.scl.common.update
import br.edu.ifsp.scl.persistence.FromRepositories
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.account.AccountRepository
import br.edu.ifsp.scl.persistence.statement.StatementProvider
import br.edu.ifsp.scl.persistence.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val accountRepository: AccountRepository = FromRepositories with application
    private val transactionRepository: TransactionRepository = FromRepositories with application
    private val statementProvider: StatementProvider = FromRepositories with application

    val balance by lazy { TotalBalance(this) }
    val list by lazy { Accounts(this) }
    val creator by lazy { AccountCreator(this) }

    class TotalBalance(viewModel: AccountsViewModel) {
        val total = viewModel.statementProvider totalBalanceAt Date()
    }

    class Accounts(private val viewModel: AccountsViewModel) {
        val accounts = MediatorLiveData<List<AccountSummary>>().apply {
            addSource(viewModel.accountRepository.allAccounts()) { accounts ->
                val partialSummaries = accounts.associateWith { AccountSummary(it) }.toMutableMap()
                accounts.forEach(
                    observeToPostPartialSummariesFrom(partialSummaries)
                )
            }
        } as LiveData<List<AccountSummary>>

        private fun MediatorLiveData<List<AccountSummary>>.observeToPostPartialSummariesFrom(
            partialSummaries: MutableMap<AccountData, AccountSummary>
        ): (AccountData) -> Unit = { account ->
            addSource(viewModel.statementProvider.balanceAt(account, Date())) { balance ->
                partialSummaries.update(account) { it.copy(balance = balance) }
                postValue(partialSummaries.values.toList())
            }
            addSource(viewModel.transactionRepository.nearestTransactionOf(account)) { transaction ->
                partialSummaries.update(account) { it.copy(nearestTransactionData = transaction) }
                postValue(partialSummaries.values.toList())
            }
        }
    }

    class AccountCreator(private val viewModel: AccountsViewModel) {
        private val _newAccountChannel = Channel<AccountData>()
        val newAccountChannel = _newAccountChannel as ReceiveChannel<AccountData>

        infix fun createAccountWith(title: String) = viewModel.viewModelScope.launch {
            val insertedAccount = withContext(Dispatchers.IO) {
                viewModel.accountRepository.insert(Account with title)
            }
            _newAccountChannel.send(insertedAccount)
        }
    }
}