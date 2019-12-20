package br.edu.ifsp.scl.account.edit

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.scl.account.R
import kotlinx.android.synthetic.main.account_title_edit_text_dialog.view.*

class TitleEditTextDialogFragment : DialogFragment() {
    private val viewModel: AccountTitleViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            val view = contentView(it.layoutInflater)
            AlertDialog.Builder(it)
                .setView(view)
                .setPositiveButton(R.string.positive_dialog_button) { _, _ ->
                    viewModel.inserted confirmWith view.titleEditText.text.toString()
                }
                .setNegativeButton(R.string.negative_dialog_button) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
        }
    }

    private fun contentView(layoutInflater: LayoutInflater) =
        layoutInflater.inflate(R.layout.account_title_edit_text_dialog, null).also {
            it.titleEditText.requestFocus()
            lifecycleScope.launchWhenResumed {
                val initialTitle = viewModel.initial.titleChannel.receive()
                it.titleEditText.text?.clear()
                it.titleEditText.append(initialTitle)
            }
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.inserted.cancel()
    }
}