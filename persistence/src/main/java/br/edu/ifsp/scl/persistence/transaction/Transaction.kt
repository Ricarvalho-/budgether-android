package br.edu.ifsp.scl.persistence.transaction

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import br.edu.ifsp.scl.persistence.account.Account
import java.util.*

@Entity(indices = [Index("title"), Index("category"), Index("startDate"), Index("accountId")])
sealed class Transaction : TransactionData {
    data class Data(
        override val title: String,
        override val category: String,
        override val value: Double,
        override val startDate: Date,
        override val frequency: Frequency,
        override val repeat: Int,
        override val accountId: Long
    ) : TransactionData

    @Entity(inheritSuperIndices = true,
        foreignKeys = [ForeignKey(entity = Account::class, parentColumns = ["id"],
            childColumns = ["accountId"], onDelete = CASCADE)
        ])
    data class Credit(
        @Embedded val data: Data,
        @PrimaryKey(autoGenerate = true) val id: Long = 0
    ) : Transaction(), TransactionData by data

    @Entity(inheritSuperIndices = true,
        foreignKeys = [ForeignKey(entity = Account::class, parentColumns = ["id"],
            childColumns = ["accountId"], onDelete = CASCADE)
        ])
    data class Debit(
        @Embedded val data: Data,
        @PrimaryKey(autoGenerate = true) val id: Long = 0
    ) : Transaction(), TransactionData by data

    @Entity(inheritSuperIndices = true,
        indices = [Index("destinationAccountId")],
        foreignKeys = [
            ForeignKey(entity = Account::class, parentColumns = ["id"],
                childColumns = ["accountId"], onDelete = CASCADE),
            ForeignKey(entity = Account::class, parentColumns = ["id"],
                childColumns = ["destinationAccountId"], onDelete = CASCADE)
        ])
    data class Transference(
        @Embedded val data: Data, val destinationAccountId: Long,
        @PrimaryKey(autoGenerate = true) val id: Long = 0
    ) : Transaction(), TransactionData by data {

        infix fun kindRelativeTo(account: Account) = when(account.id) {
            accountId -> RelativeKind.Sent
            destinationAccountId -> RelativeKind.Received
            else -> RelativeKind.Unrelated
        }

        enum class RelativeKind { Sent, Received, Unrelated }
    }

    enum class Frequency { Single, Daily, Weekly, Monthly, Yearly }
}

interface TransactionData {
    val accountId: Long
    val title: String
    val category: String
    val value: Double
    val startDate: Date
    val frequency: Transaction.Frequency
    val repeat: Int
}
