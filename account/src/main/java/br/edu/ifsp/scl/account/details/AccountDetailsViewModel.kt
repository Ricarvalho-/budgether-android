package br.edu.ifsp.scl.account.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.scl.common.switchedTo
import br.edu.ifsp.scl.persistence.FromRepositories
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.account.AccountRepository
import br.edu.ifsp.scl.persistence.statement.StatementProvider
import br.edu.ifsp.scl.persistence.transaction.Transaction
import br.edu.ifsp.scl.persistence.transaction.TransactionData
import br.edu.ifsp.scl.persistence.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AccountDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val accountRepository: AccountRepository = FromRepositories with application
    private val transactionRepository: TransactionRepository = FromRepositories with application
    private val statementProvider: StatementProvider = FromRepositories with application

    val selection by lazy { AccountSelectionUseCase() }
    val balance by lazy { AccountBalanceUseCase(this) }
    val list by lazy { AccountTransactionsUseCase(this) }
    val editor by lazy { AccountEditorUseCase(this) }
    val creator by lazy { TransactionCreatorUseCase(this) }

    class AccountSelectionUseCase {
        private val _lastSelected = MutableLiveData<AccountData>()
        val lastSelected = _lastSelected as LiveData<AccountData>

        infix fun select(account: AccountData) {
            _lastSelected.value = account
        }
    }

    class AccountBalanceUseCase(viewModel: AccountDetailsViewModel) {
        val selectedAccount = viewModel.selection.lastSelected switchedTo {
            viewModel.statementProvider.balanceAt(it, Date())
        }
    }

    class AccountTransactionsUseCase(private val viewModel: AccountDetailsViewModel) {
        val transactionsOfSelectedAccount = viewModel.selection.lastSelected switchedTo {
            viewModel.statementProvider.allTransactionsBefore(Date(), it)
        }
    }

    class AccountEditorUseCase(private val viewModel: AccountDetailsViewModel) {
        fun deleteSelectedAccount() {
            val selectedAccount = viewModel.selection.lastSelected.value ?: return
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                viewModel.accountRepository.delete(selectedAccount)
            }
        }

        infix fun setTitleOfSelectedAccount(title: String) {
            val selectedAccount = viewModel.selection.lastSelected.value ?: return
            val updatedAccount = Account from selectedAccount
            updatedAccount.title = title
            viewModel.selection select updatedAccount
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                viewModel.accountRepository.update(updatedAccount)
            }
        }
    }

    class TransactionCreatorUseCase(private val viewModel: AccountDetailsViewModel) {
        private val _newTransactionChannel = Channel<TransactionData>()
        val newTransactionChannel = _newTransactionChannel as ReceiveChannel<TransactionData>

        fun createTransaction() {
            val selectedAccount = viewModel.selection.lastSelected.value ?: return
            viewModel.viewModelScope.launch {
                val insertedTransaction = withContext(Dispatchers.IO) {
                    viewModel.transactionRepository.insert(
                        Transaction defaultOn selectedAccount
                    )
                }
                _newTransactionChannel.send(insertedTransaction)
            }
        }
    }
}