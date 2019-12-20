package br.edu.ifsp.scl.common

import android.content.Context
import android.text.format.DateFormat
import java.text.NumberFormat
import java.util.*

fun Number.currencyFormatted(): String = NumberFormat.getCurrencyInstance().format(this)
fun Date.shortFormatted(context: Context): String = DateFormat.getDateFormat(context).format(this)