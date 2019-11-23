package br.edu.ifsp.scl.persistence

import androidx.room.TypeConverter
import br.edu.ifsp.scl.persistence.transaction.Transaction
import java.util.*

class Converters {
    @TypeConverter fun dateFromTimestamp(value: Long) = Date(value)
    @TypeConverter fun dateToTimestamp(date: Date) = date.time

    @TypeConverter fun frequencyFromOrdinal(ordinal: Int) = Transaction.Frequency.values()[ordinal]
    @TypeConverter fun frequencyToOrdinal(frequency: Transaction.Frequency) = frequency.ordinal
}