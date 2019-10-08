package br.edu.ifsp.scl.statement

import android.text.format.DateFormat
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.scl.common.MarginItemDecoration
import br.edu.ifsp.scl.common.OnSnapPositionChangeCallback
import br.edu.ifsp.scl.common.attachSnapHelperWithListener
import br.edu.ifsp.scl.common.shortFormatted
import java.util.*
import java.util.concurrent.TimeUnit

data class DateRange(val start: Date, val end: Date) {
    companion object {
        fun actualMonthRange() = Date().let {
            DateRange(
                it.asFirst(Calendar.DAY_OF_MONTH).clearingUndesiredFields(),
                it.asLast(Calendar.DAY_OF_MONTH).clearingUndesiredFields()
            )
        }

        private val Date.calendar: Calendar get() = Calendar.getInstance().also{ it.time = this }
        private fun Date.byAdding(field: Int, amount: Int) = calendar.apply { add(field, amount) }.time
        private fun Date.asFirst(field: Int) = calendar.apply { set(field, getActualMinimum(field)) }.time
        private fun Date.asLast(field: Int) = calendar.apply { set(field, getActualMaximum(field)) }.time
        private fun Date.isFirst(field: Int) = with(calendar) { get(field) == getActualMinimum(field) }
        private fun Date.isLast(field: Int) = with(calendar) { get(field) == getActualMaximum(field) }
        private fun Date.isSameAs(other: Date, field: Int) = calendar.get(field) == other.calendar.get(field)
        private fun Date.clearingUndesiredFields() = calendar.apply {
            clear(Calendar.HOUR)
            clear(Calendar.HOUR_OF_DAY)
            clear(Calendar.HOUR_OF_DAY)
            clear(Calendar.HOUR_OF_DAY)
        }.time
    }

    val isYear by lazy {
        return@lazy when {
            !start.isSameAs(end, Calendar.YEAR) -> false
            !start.isFirst(Calendar.DAY_OF_YEAR) -> false
            !end.isLast(Calendar.DAY_OF_YEAR) -> false
            else -> true
        }
    }

    val isMonth by lazy {
        return@lazy when {
            !start.isSameAs(end, Calendar.YEAR) -> false
            !start.isSameAs(end, Calendar.MONTH) -> false
            !start.isFirst(Calendar.DAY_OF_MONTH) -> false
            !end.isLast(Calendar.DAY_OF_MONTH) -> false
            else -> true
        }
    }

    private val daysInterval by lazy {
        val millisDiff = end.calendar.timeInMillis - start.calendar.timeInMillis
        TimeUnit.MILLISECONDS.toDays(millisDiff).toInt()
    }

    fun yearRange() = DateRange(start.asFirst(Calendar.DAY_OF_YEAR), end.asLast(Calendar.DAY_OF_YEAR))
    fun atYearOf(reference: DateRange) = DateRange(
        start.calendar.apply { set(Calendar.YEAR, reference.start.calendar.get(Calendar.YEAR)) }.time,
        end.calendar.apply { set(Calendar.YEAR, reference.end.calendar.get(Calendar.YEAR)) }.time
    )

    fun stream(before: Int = 50, after: Int = 50) = List(before + 1 + after) { index ->
        val relativePosition = index - before
        when {
            relativePosition == 0 -> this
            isYear -> DateRange(
                start.byAdding(Calendar.YEAR, relativePosition),
                end.byAdding(Calendar.YEAR, relativePosition)
            )
            isMonth -> DateRange(
                start.byAdding(Calendar.MONTH, relativePosition),
                end.byAdding(Calendar.MONTH, relativePosition)
            )
            else -> DateRange(
                start.byAdding(Calendar.DATE, daysInterval * relativePosition),
                end.byAdding(Calendar.DATE, daysInterval * relativePosition)
            )
        }
    }
}

typealias OnDateRangeSelectedCallback = (DateRange) -> Unit

class DateRangeSelectionRecyclerViewManager(recyclerView: RecyclerView,
                                            private val selectedDateRange: DateRange = DateRange.actualMonthRange(),
                                            var onRangeSelected: OnDateRangeSelectedCallback) {

    private val yearManager: InnerRecyclerViewManager = InnerRecyclerViewManager(selectedDateRange) {
        val selectedMonthAtSelectedYear = monthManager.selectedDateRange.atYearOf(it)
        monthManager.selectedDateRange = selectedMonthAtSelectedYear
        onRangeSelected(it)
    }

    private val monthManager = InnerRecyclerViewManager(selectedDateRange) {
        yearManager.selectedDateRange = it.yearRange()
        onRangeSelected(it)
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
            scrollToPosition(with(selectedDateRange) {
                when {
                    isYear -> recyclerViewAdapter.positionOf(yearManager)
                    isMonth -> recyclerViewAdapter.positionOf(monthManager)
                    else -> RecyclerView.NO_POSITION
                }
            })

            attachSnapHelperWithListener(PagerSnapHelper()) { position ->
                val activeManager = recyclerViewAdapter.managerAt(position)
                onRangeSelected(activeManager.selectedDateRange)
            }
        }
    }

    private class Adapter(val innerRecyclerManagers: List<InnerRecyclerViewManager>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(RecyclerView(parent.context))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            managerAt(position).recyclerView = holder.recyclerViewItem
        }

        override fun getItemCount() = innerRecyclerManagers.size
        fun managerAt(position: Int) = innerRecyclerManagers[position]
        fun positionOf(manager: InnerRecyclerViewManager) = innerRecyclerManagers.indexOf(manager)

        class ViewHolder(val recyclerViewItem: RecyclerView) : RecyclerView.ViewHolder(recyclerViewItem)
    }
}

private class InnerRecyclerViewManager(selectedDateRange: DateRange, private val onRangeSelected: OnDateRangeSelectedCallback) {
    private val recyclerViewAdapter = Adapter(selectedDateRange)

    var selectedDateRange = selectedDateRange
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

                it.scrollToPosition(recyclerViewAdapter.positionOf(selectedDateRange))

                it.attachSnapHelperWithListener(PagerSnapHelper()) { position ->
                    selectedDateRange = recyclerViewAdapter.rangeAt(position)
                    recyclerViewAdapter.update(selectedDateRange)
                    onRangeSelected(selectedDateRange)
                }
            }
        }

    class Adapter(selectedRange: DateRange) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        private var rangeOptions: List<DateRange> = selectedRange.stream()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(TextView(parent.context))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = with(rangeAt(position)) {
                val context = holder.textView.context
                fun bestFormatted(skeleton: String) =
                    DateFormat.format(
                        DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton),
                        start
                    )
                when {
                    isYear -> bestFormatted("yyyy")
                    isMonth -> bestFormatted("MMM yyyy")
                    else -> "${start.shortFormatted(context)} - ${end.shortFormatted(context)}"
                }
            }
        }

        override fun getItemCount() = rangeOptions.size
        fun rangeAt(position: Int) = rangeOptions[position]
        fun positionOf(range: DateRange) = rangeOptions.indexOf(range)

        fun update(selectedRange: DateRange) {
            val updatedRangeOptions = selectedRange.stream()
            val diff = DiffUtil.calculateDiff(DiffCallback(rangeOptions, updatedRangeOptions), false)
            diff.dispatchUpdatesTo(this)
            rangeOptions = updatedRangeOptions
        }

        class DiffCallback(val newValues: List<DateRange>, val oldValues: List<DateRange>) : DiffUtil.Callback() {
            override fun getOldListSize() = oldValues.size
            override fun getNewListSize() = newValues.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldValues[oldItemPosition] == newValues[newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
        }

        class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }
}