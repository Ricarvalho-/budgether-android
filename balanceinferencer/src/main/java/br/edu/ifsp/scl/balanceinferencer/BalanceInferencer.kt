package br.edu.ifsp.scl.balanceinferencer

import java.util.*
import kotlin.collections.HashMap

object BalanceInferencer {
    private val knownValuesPool: Map<Date, Double> = HashMap()

    fun inferBalanceAt(dates: List<Date>) = dates.associateWith { inferBalanceAt(it) }

    fun inferBalanceAt(date: Date) = when {
        knownValuesPool.isEmpty() -> calculateBalanceTo(date)
        knownValuesPool.containsKey(date) -> knownValuesPool.getValue(date)
        else -> inferBalanceFromCloserKnownValueTo(date)
    }

    private fun calculateBalanceTo(date: Date): Double {
        TODO("not implemented")
    }

    private fun inferBalanceFromCloserKnownValueTo(date: Date): Double {
        val earlierKnownDate = knownValuesPool.keys.filter { it < date }.min()
        val knownEarlierValue = earlierKnownDate?.let {
            it to knownValuesPool.getValue(it)
        }

        val laterKnownDate = knownValuesPool.keys.filter { it > date }.max()
        val knownLaterValue = laterKnownDate?.let {
            it to knownValuesPool.getValue(it)
        }

        if (knownEarlierValue == null) return inferFromKnownLaterValue(knownLaterValue!!, date)
        if (knownLaterValue == null) return inferFromKnownEarlierValue(knownEarlierValue, date)

        val diffFromEarlier = earlierKnownDate.time - date.time
        val diffToLater = laterKnownDate.time - date.time
        val earlierIsCloser = diffFromEarlier < diffToLater

        return if (earlierIsCloser) inferFromKnownEarlierValue(knownEarlierValue, date)
        else inferFromKnownLaterValue(knownLaterValue, date)
    }

    private fun inferFromKnownLaterValue(knownLaterValue: Pair<Date, Double>, date: Date): Double {
        TODO("not implemented")
    }

    private fun inferFromKnownEarlierValue(knownEarlierValue: Pair<Date, Double>, date: Date): Double {
        TODO("not implemented")
    }
}
