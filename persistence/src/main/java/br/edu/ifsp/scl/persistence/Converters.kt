package br.edu.ifsp.scl.persistence

import androidx.room.TypeConverter
import br.edu.ifsp.scl.persistence.transaction.TransactionData.Frequency
import java.util.*

internal class Converters {
    @TypeConverter fun dateFromTimestamp(value: Long) = Date(value)
    @TypeConverter fun dateToTimestamp(date: Date) = date.time

    @TypeConverter fun frequencyFromOrdinal(ordinal: Int) = Frequency.values()[ordinal]
    @TypeConverter fun frequencyToOrdinal(frequency: Frequency) = frequency.ordinal
}