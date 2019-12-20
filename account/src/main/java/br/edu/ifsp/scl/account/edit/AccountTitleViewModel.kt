package br.edu.ifsp.scl.account.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class AccountTitleViewModel : ViewModel() {
    val initial by lazy { InitialValueUseCase(this) }
    val inserted by lazy { InsertedValueUseCase(this) }

    class InitialValueUseCase(private val viewModel: ViewModel) {
        private var _titleChannel = Channel<String>()
        val titleChannel get() = _titleChannel as ReceiveChannel<String>

        infix fun with(title: String) = viewModel.viewModelScope.launch {
            _titleChannel.send(title)
        }
    }

    class InsertedValueUseCase(private val viewModel: ViewModel) {
        private var _titleChannel = Channel<String>()
        val titleChannel : ReceiveChannel<String>
            get() {
                _titleChannel.cancel()
                _titleChannel = Channel()
                return _titleChannel
            }

        infix fun confirmWith(title: String) = viewModel.viewModelScope.launch {
            _titleChannel.send(title)
        }

        fun cancel() = viewModel.viewModelScope.launch {
            _titleChannel.cancel()
            _titleChannel = Channel()
        }
    }
}