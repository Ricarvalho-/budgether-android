package br.edu.ifsp.scl.persistence.transaction

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import br.edu.ifsp.scl.persistence.account.AccountData
import br.edu.ifsp.scl.persistence.account.AccountEntity
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Kind.*
import br.edu.ifsp.scl.persistence.transaction.TransferenceData.RelativeKind.*
import java.util.*

@Entity(indices = [Index("title"), Index("category"), Index("startDate"), Index("accountId")])
internal sealed class TransactionEntity : TransactionData {
    companion object {
        const val INDETERMINATE_REPEAT = 1
        infix fun from(data: TransactionData) = when (data.kind) {
            Credit -> CreditEntity(Fields from data, data.id)
            Debit -> DebitEntity(Fields from data, data.id)
            Transfer ->
                if (data is TransferenceData) TransferenceEntity(Fields from data, data.recipientAccountId, data.id)
                else throw IllegalStateException("Should be transference data")
        }
    }

    @Entity(inheritSuperIndices = true,
        foreignKeys = [ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = CASCADE)])
    data class CreditEntity(@Embedded val data: Fields,
                            @PrimaryKey(autoGenerate = true) override val id: Long = 0
    ) : TransactionEntity(), TransactionData, TransactionFields by data {
        override val kind get() = Credit
    }

    @Entity(inheritSuperIndices = true,
        foreignKeys = [ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = CASCADE)])
    data class DebitEntity(@Embedded val data: Fields,
                           @PrimaryKey(autoGenerate = true) override val id: Long = 0
    ) : TransactionEntity(), TransactionData, TransactionFields by data {
        override val kind get() = Debit
    }

    @Entity(inheritSuperIndices = true, indices = [Index("recipientAccountId")],
        foreignKeys = [
            ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = CASCADE),
            ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["recipientAccountId"], onDelete = CASCADE)
        ])
    data class TransferenceEntity(@Embedded val data: Fields, override val recipientAccountId: Long,
                                  @PrimaryKey(autoGenerate = true) override val id: Long = 0
    ) : TransactionEntity(), TransferenceData, TransactionFields by data {
        override val kind get() = Transfer
    }
}

interface TransactionFields {
    val accountId: Long
    val title: String
    val category: String
    val value: Double
    val startDate: Date
    val frequency: Frequency
    val repeat: Int
}

interface TransactionData : TransactionFields {
    val id: Long
    val kind: Kind

    enum class Kind { Credit, Debit, Transfer }
    enum class Frequency { Single, Daily, Weekly, Monthly, Yearly }
}

interface TransferenceData : TransactionData {
    val recipientAccountId: Long

    infix fun kindRelativeTo(account: AccountData) = when (account.id) {
        accountId -> Sent
        recipientAccountId -> Received
        else -> Unrelated
    }

    enum class RelativeKind { Sent, Received, Unrelated }
}

data class Fields(
    override val title: String,
    override val category: String,
    override val value: Double,
    override val startDate: Date,
    override val frequency: Frequency,
    override val repeat: Int,
    override val accountId: Long
) : TransactionFields {
    companion object {
        infix fun from(data: TransactionData) = Fields(
            data.title, data.category, data.value, data.startDate,
            data.frequency, data.repeat, data.accountId
        )
    }
}

data class Transaction(
    val fields: TransactionFields, override val kind: TransactionData.Kind
) : TransactionData, TransactionFields by fields {
    override val id = 0L
}

internal val TransactionData.isIndeterminate
    get() = repeat == TransactionEntity.INDETERMINATE_REPEAT

internal val TransactionData.entity
    get() = takeIf { it is TransactionEntity } as TransactionEntity? ?: TransactionEntity from this