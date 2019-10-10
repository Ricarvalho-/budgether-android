package br.edu.ifsp.scl.statement

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.scl.common.MarginItemDecoration
import br.edu.ifsp.scl.common.attachSnapHelperWithListener
import kotlinx.android.synthetic.main.large_centered_text_view.view.*
import java.util.*

sealed class Period {
    abstract val dateRange: DateRange

    fun stream(before: Int = 25, after: Int = 25) = List(before + 1 + after) { index ->
        when (val relativePosition = index - before) {
            0 -> this
            else -> byOffsetting(relativePosition)
        }
    }

    protected abstract fun byOffsetting(amount: Int): Period

    data class Year(val year: Int): Period(), BaseCalendarProvider {
        override val dateRange by lazy {
            val calendar = baseCalendar()
            DateRange(
                calendar.apply { setAsFirst(Calendar.DAY_OF_YEAR) }.time,
                calendar.apply { setAsLast(Calendar.DAY_OF_YEAR) }.time
            )
        }

        override fun baseCalendar(): Calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
        }

        override fun byOffsetting(amount: Int) = Year(year + amount)

        companion object {
            val actual = Year(Calendar.getInstance().get(Calendar.YEAR))
        }
    }

    data class Month(private val month: Int, val year: Year): Period(), BaseCalendarProvider {
        override val dateRange by lazy {
            val calendar = baseCalendar()
            DateRange(
                calendar.apply { setAsFirst(Calendar.DAY_OF_MONTH) }.time,
                calendar.apply { setAsLast(Calendar.DAY_OF_MONTH) }.time
            )
        }

        override fun baseCalendar(): Calendar = year.baseCalendar().apply { set(Calendar.MONTH, month) }

        override fun byOffsetting(amount: Int): Period {
            val target = month + amount
            val maxValue = year.baseCalendar().getActualMaximum(Calendar.MONTH).inc()
            val clampedValue = target % maxValue
            val overflow = target / maxValue
            val normalizedClampedValue = if (clampedValue < 0) maxValue + clampedValue else clampedValue
            val normalizedOverflow = if (clampedValue < 0) overflow.dec() else overflow
            return Month(normalizedClampedValue, Year(year.year + normalizedOverflow))
        }

        fun sameMonthAt(anotherYear: Year) = Month(month, anotherYear)

        companion object {
            val actual = Month(Calendar.getInstance().get(Calendar.MONTH), Year.actual)
        }
    }

    protected fun Calendar.setAsFirst(field: Int) = set(field, getActualMinimum(field))
    protected fun Calendar.setAsLast(field: Int) = set(field, getActualMaximum(field))

    private interface BaseCalendarProvider { fun baseCalendar(): Calendar }
    data class DateRange(val start: Date, val end: Date)
}

class DatePeriodSelectionRecyclerViewManager(recyclerView: RecyclerView,
                                             private val selectedPeriod: Period = Period.Month.actual,
                                             var onPeriodSelected: (Period) -> Unit) {

    private val yearManager: PeriodRecyclerViewManager<Period.Year> = PeriodRecyclerViewManager(
        selectedPeriod.let { when (it) {
            is Period.Year -> it
            is Period.Month -> it.year
        }}
    ) {
        monthManager.selectedPeriod = monthManager.selectedPeriod.sameMonthAt(it)
        onPeriodSelected(it)
    }

    private val monthManager = PeriodRecyclerViewManager(
        selectedPeriod.let { when (it) {
            is Period.Month -> it
            is Period.Year -> Period.Month.actual.sameMonthAt(it)
        }}
    ) {
        yearManager.selectedPeriod = it.year
        onPeriodSelected(it)
    }

    private val recyclerViewAdapter = Adapter(listOf(yearManager, monthManager))

    init {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdapter
        }.also {
            val margin = it.resources.getDimension(R.dimen.date_range_selection_item_vertical_margin)
            it.addItemDecoration(MarginItemDecoration(margin.toInt()))
        }.run {
            scrollToPosition(when (selectedPeriod) {
                is Period.Year -> recyclerViewAdapter.positionOf(yearManager)
                is Period.Month -> recyclerViewAdapter.positionOf(monthManager)
            })

            attachSnapHelperWithListener(PagerSnapHelper()) { position ->
                val activeManager = recyclerViewAdapter.managerAt(position)
                onPeriodSelected(activeManager.selectedPeriod)
            }
        }
    }

    private class Adapter(val innerRecyclerManagers: List<PeriodRecyclerViewManager<*>>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(RecyclerView(parent.context).also {
            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        })

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            managerAt(position).recyclerView = holder.recyclerViewItem
        }

        override fun getItemCount() = innerRecyclerManagers.size
        fun managerAt(position: Int) = innerRecyclerManagers[position]
        fun positionOf(manager: PeriodRecyclerViewManager<*>) = innerRecyclerManagers.indexOf(manager)

        class ViewHolder(val recyclerViewItem: RecyclerView) : RecyclerView.ViewHolder(recyclerViewItem)
    }
}

private class PeriodRecyclerViewManager<P : Period>(selectedPeriod: P, private val onPeriodSelected: (P) -> Unit) {
    private val recyclerViewAdapter = Adapter(selectedPeriod)

    var selectedPeriod = selectedPeriod
        set(value) {
            field = value.also {
                recyclerViewAdapter.update(it)
                recyclerView?.scrollToPosition(recyclerViewAdapter.positionOf(it))
            }
        }

    var recyclerView: RecyclerView? = null
        set(value) {
            field?.clearOnScrollListeners()
            field = value?.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = recyclerViewAdapter
            }?.also {
                val margin = it.resources.getDimension(R.dimen.date_range_selection_item_horizontal_margin)
                it.addItemDecoration(MarginItemDecoration(margin.toInt()))

                it.scrollToPosition(recyclerViewAdapter.positionOf(selectedPeriod))

                it.attachSnapHelperWithListener(PagerSnapHelper()) { position ->
                    selectedPeriod = recyclerViewAdapter.periodAt(position) as P
                    onPeriodSelected(selectedPeriod)
                }
            }
        }

    class Adapter<P : Period>(selectedPeriod: P) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private var periodOptions = selectedPeriod.stream()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            with(LayoutInflater.from(parent.context)) {
                inflate(R.layout.large_centered_text_view, parent, false)
            }.textView
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val period = periodAt(position)
            fun bestFormatted(skeleton: String) = DateFormat.format(
                DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton),
                period.dateRange.start
            )
            holder.textView.text = when (period) {
                is Period.Year -> bestFormatted("yyyy")
                is Period.Month -> bestFormatted("MMM yyyy")
            }
        }

        override fun getItemCount() = periodOptions.size
        fun periodAt(position: Int) = periodOptions[position]
        fun positionOf(period: P) = periodOptions.indexOf(period)

        fun update(selectedPeriod: P) {
            val updatedPeriodOptions = selectedPeriod.stream()
            val diff = DiffUtil.calculateDiff(DiffCallback(periodOptions, updatedPeriodOptions), false)
            periodOptions = updatedPeriodOptions
            diff.dispatchUpdatesTo(this)
        }

        class DiffCallback(val oldValues: List<Period>, val newValues: List<Period>) : DiffUtil.Callback() {
            override fun getOldListSize() = oldValues.size
            override fun getNewListSize() = newValues.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldValues[oldItemPosition] == newValues[newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
        }

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }
}