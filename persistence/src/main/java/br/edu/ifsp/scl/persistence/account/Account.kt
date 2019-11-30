package br.edu.ifsp.scl.persistence.account

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class AccountEntity(
    override val title: String,
    @PrimaryKey(autoGenerate = true) override val id: Long = 0
) : AccountData {
    companion object {
        infix fun from(data: AccountData) = AccountEntity(data.title, data.id)
    }
}

interface AccountData {
    val title: String
    val id: Long
}

internal val AccountData.entity get() = takeIf { it is AccountEntity } as AccountEntity? ?: AccountEntity from this

data class Account(override val title: String, override val id: Long = 0L) : AccountData