package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction
import java.util.*

fun DatabaseTest.credit(amount: Double = defaultValue,
                        at: Date = defaultDate,
                        into: Account = insertAccount(),
                        repeating: Transaction.Frequency = Transaction.Frequency.Single,
                        during: Int = 0) =
    insert(Transaction.Credit(sampleTransactionData(
        value = amount,
        startDate = at,
        frequency = repeating,
        repeat = during,
        accountId = into.id
    )))

fun DatabaseTest.debit(amount: Double = defaultValue,
                       at: Date = defaultDate,
                       from: Account = insertAccount(),
                       repeating: Transaction.Frequency = Transaction.Frequency.Single,
                       during: Int = 0) =
    insert(Transaction.Debit(sampleTransactionData(
        value = amount,
        startDate = at,
        frequency = repeating,
        repeat = during,
        accountId = from.id
    )))

fun DatabaseTest.transfer(amount: Double = defaultValue,
                          at: Date = defaultDate,
                          from: Account = insertAccount(),
                          to: Account = insertAccount(),
                          repeating: Transaction.Frequency = Transaction.Frequency.Single,
                          during: Int = 0) =
    insert(Transaction.Transference(
        sampleTransactionData(
            value = amount,
            startDate = at,
            frequency = repeating,
            repeat = during,
            accountId = from.id
        ),
        to.id
    ))