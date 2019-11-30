package br.edu.ifsp.scl.persistence.statement

import br.edu.ifsp.scl.persistence.*
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency
import br.edu.ifsp.scl.persistence.transaction.TransactionEntity.*
import java.util.*

internal fun DatabaseTest.credit(amount: Double = defaultValue,
                        at: Date = defaultDate,
                        into: AccountEntity = insertAccount(),
                        repeating: Frequency = Frequency.Single,
                        during: Int = 0,
                        about: String = defaultTitle) =
    insert(CreditEntity(sampleTransactionData(
        category = about,
        value = amount,
        startDate = at,
        frequency = repeating,
        repeat = during,
        accountId = into.id
    )))

internal fun DatabaseTest.debit(amount: Double = defaultValue,
                       at: Date = defaultDate,
                       from: AccountEntity = insertAccount(),
                       repeating: Frequency = Frequency.Single,
                       during: Int = 0,
                       about: String = defaultTitle) =
    insert(DebitEntity(sampleTransactionData(
        category = about,
        value = amount,
        startDate = at,
        frequency = repeating,
        repeat = during,
        accountId = from.id
    )))

internal fun DatabaseTest.transfer(amount: Double = defaultValue,
                          at: Date = defaultDate,
                          from: AccountEntity = insertAccount(),
                          to: AccountEntity = insertAccount(),
                          repeating: Frequency = Frequency.Single,
                          during: Int = 0,
                          about: String = defaultTitle) =
    insert(TransferenceEntity(
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