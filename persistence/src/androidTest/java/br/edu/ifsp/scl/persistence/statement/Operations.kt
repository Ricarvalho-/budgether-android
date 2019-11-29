package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.Account
import br.edu.ifsp.scl.persistence.transaction.Transaction.*
import java.util.*

fun DatabaseTest.credit(amount: Double = defaultValue,
                        at: Date = defaultDate,
                        into: Account = insertAccount(),
                        repeating: Frequency = Frequency.Single,
                        during: Int = 0,
                        about: String = defaultTitle) =
    insert(Credit(sampleTransactionData(
        category = about,
        value = amount,
        startDate = at,
        frequency = repeating,
        repeat = during,
        accountId = into.id
    )))

fun DatabaseTest.debit(amount: Double = defaultValue,
                       at: Date = defaultDate,
                       from: Account = insertAccount(),
                       repeating: Frequency = Frequency.Single,
                       during: Int = 0,
                       about: String = defaultTitle) =
    insert(Debit(sampleTransactionData(
        category = about,
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
                          repeating: Frequency = Frequency.Single,
                          during: Int = 0,
                          about: String = defaultTitle) =
    insert(Transference(
        sampleTransactionData(
            category = about,
            value = amount,
            startDate = at,
            frequency = repeating,
            repeat = during,
            accountId = from.id
        ),
        to.id
    ))