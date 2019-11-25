package br.edu.ifsp.scl.persistence.transaction

import br.edu.ifsp.scl.persistence.transaction.Transaction.Frequency.*
import java.util.*

data class RepeatingTransaction(val transaction: Transaction, val atDate: Date, private val number: Int) : TransactionData by transaction {
    val repeatability by lazy {
        when (transaction.frequency) {
            Single -> Repeatability.Single
            else -> Repeatability.Many(
                number,
                times
            )
        }
    }

    private val times get() =
        if (transaction.isIndeterminate) Repeatability.Many.Times.Indeterminate
        else Repeatability.Many.Times.Determinate(
            transaction.repeat
        )
}

sealed class Repeatability {
    object Single : Repeatability()
    class Many(val number: Int, val of: Times) : Repeatability() {
        sealed class Times {
            object Indeterminate : Times()
            class Determinate(val amount: Int) : Times()
        }
    }
}