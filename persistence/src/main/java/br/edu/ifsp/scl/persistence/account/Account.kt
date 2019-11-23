package br.edu.ifsp.scl.persistence.account

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    var title: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)