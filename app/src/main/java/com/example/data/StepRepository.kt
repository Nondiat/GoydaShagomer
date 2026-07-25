package com.example.data

import android.content.Context
import com.example.widget.GoydaWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class TimeIntervalItem(
    val label: String,      // Short label for X-axis (e.g., "12:00", "Пн", "15", "Июл")
    val fullLabel: String,  // Full label for tooltip popup (e.g., "12:00 - 13:00", "Среда, 22 июля", "22 июля 2026", "Июль 2026")
    val steps: Int,
    val calories: Float,
    val durationMinutes: Int
)

enum class TimeTab {
    DAY, WEEK, MONTH, YEAR
}

class StepRepository(
    private val stepDao: StepDao,
    private val context: Context
) {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun seedSampleDataIfNeeded() = withContext(Dispatchers.IO) {
        // Auto-seeding disabled per user requirement: clean startup with 0 steps
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        stepDao.deleteAllSteps()
        GoydaWidgetProvider.updateAppWidget(context)
    }

    suspend fun addStepsForTodayHour(addedSteps: Int) = withContext(Dispatchers.IO) {
        if (addedSteps <= 0) return@withContext
        val cal = Calendar.getInstance()
        val todayStr = dateFormatter.format(cal.time)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val existing = stepDao.getRecordByDateAndHour(todayStr, hour)
        val newSteps = (existing?.steps ?: 0) + addedSteps
        val newDuration = (newSteps / 100).coerceAtLeast(if (newSteps > 0) 1 else 0)
        val newCals = newSteps * 0.04f

        val updatedRecord = existing?.copy(
            steps = newSteps,
            durationMinutes = newDuration,
            calories = newCals
        ) ?: StepRecord(
            dateString = todayStr,
            hour = hour,
            steps = newSteps,
            durationMinutes = newDuration,
            calories = newCals
        )

        stepDao.insertOrUpdateStepRecord(updatedRecord)
        GoydaWidgetProvider.updateAppWidget(context)
    }

    fun getDayIntervals(date: Date): Flow<List<TimeIntervalItem>> {
        val dateStr = dateFormatter.format(date)
        return stepDao.getStepsForDate(dateStr).map { records ->
            val hourMap = records.associateBy { it.hour }
            (0..23).map { h ->
                val rec = hourMap[h]
                val steps = rec?.steps ?: 0
                val duration = rec?.durationMinutes ?: 0
                val cals = rec?.calories ?: 0f
                val label = String.format(Locale.getDefault(), "%02d:00", h)
                val nextH = (h + 1) % 24
                val fullLabel = String.format(Locale.getDefault(), "%02d:00 - %02d:00", h, nextH)
                TimeIntervalItem(
                    label = label,
                    fullLabel = fullLabel,
                    steps = steps,
                    calories = cals,
                    durationMinutes = duration
                )
            }
        }
    }

    fun getWeekIntervals(date: Date): Flow<List<TimeIntervalItem>> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startDateStr = dateFormatter.format(cal.time)

        val calEnd = Calendar.getInstance()
        calEnd.time = cal.time
        calEnd.add(Calendar.DAY_OF_YEAR, 6)
        val endDateStr = dateFormatter.format(calEnd.time)

        val dayNamesRu = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

        return stepDao.getStepsBetweenDates(startDateStr, endDateStr).map { records ->
            val dayGroups = records.groupBy { it.dateString }
            val items = mutableListOf<TimeIntervalItem>()

            val tempCal = Calendar.getInstance()
            tempCal.time = cal.time
            val ruFormat = SimpleDateFormat("d MMMM", Locale("ru"))

            for (i in 0..6) {
                val curDateStr = dateFormatter.format(tempCal.time)
                val dayRecords = dayGroups[curDateStr] ?: emptyList()
                val totalSteps = dayRecords.sumOf { it.steps }
                val totalCals = dayRecords.sumOf { it.calories.toDouble() }.toFloat()
                val totalDuration = dayRecords.sumOf { it.durationMinutes }

                val label = dayNamesRu[i]
                val fullLabel = "${dayNamesRu[i]}, ${ruFormat.format(tempCal.time)}"

                items.add(
                    TimeIntervalItem(
                        label = label,
                        fullLabel = fullLabel,
                        steps = totalSteps,
                        calories = totalCals,
                        durationMinutes = totalDuration
                    )
                )
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            items
        }
    }

    fun getMonthIntervals(date: Date): Flow<List<TimeIntervalItem>> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDateStr = dateFormatter.format(cal.time)

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val calEnd = Calendar.getInstance()
        calEnd.time = cal.time
        calEnd.set(Calendar.DAY_OF_MONTH, maxDays)
        val endDateStr = dateFormatter.format(calEnd.time)

        val monthNameRu = SimpleDateFormat("LLLL yyyy", Locale("ru")).format(date)

        return stepDao.getStepsBetweenDates(startDateStr, endDateStr).map { records ->
            val dayGroups = records.groupBy { it.dateString }
            val items = mutableListOf<TimeIntervalItem>()

            val tempCal = Calendar.getInstance()
            tempCal.time = cal.time

            for (dayNum in 1..maxDays) {
                val curDateStr = dateFormatter.format(tempCal.time)
                val dayRecords = dayGroups[curDateStr] ?: emptyList()
                val totalSteps = dayRecords.sumOf { it.steps }
                val totalCals = dayRecords.sumOf { it.calories.toDouble() }.toFloat()
                val totalDuration = dayRecords.sumOf { it.durationMinutes }

                items.add(
                    TimeIntervalItem(
                        label = "$dayNum",
                        fullLabel = "$dayNum $monthNameRu",
                        steps = totalSteps,
                        calories = totalCals,
                        durationMinutes = totalDuration
                    )
                )
                tempCal.add(Calendar.DAY_OF_MONTH, 1)
            }
            items
        }
    }

    fun getYearIntervals(date: Date): Flow<List<TimeIntervalItem>> {
        val cal = Calendar.getInstance()
        cal.time = date
        val year = cal.get(Calendar.YEAR)

        cal.set(year, Calendar.JANUARY, 1)
        val startDateStr = dateFormatter.format(cal.time)

        cal.set(year, Calendar.DECEMBER, 31)
        val endDateStr = dateFormatter.format(cal.time)

        val monthNamesShort = arrayOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")
        val monthNamesFull = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

        return stepDao.getStepsBetweenDates(startDateStr, endDateStr).map { records ->
            val monthGroups = records.groupBy { rec ->
                rec.dateString.substring(5, 7).toInt() - 1 // 0..11
            }
            (0..11).map { m ->
                val monthRecords = monthGroups[m] ?: emptyList()
                val totalSteps = monthRecords.sumOf { it.steps }
                val totalCals = monthRecords.sumOf { it.calories.toDouble() }.toFloat()
                val totalDuration = monthRecords.sumOf { it.durationMinutes }

                TimeIntervalItem(
                    label = monthNamesShort[m],
                    fullLabel = "${monthNamesFull[m]} $year",
                    steps = totalSteps,
                    calories = totalCals,
                    durationMinutes = totalDuration
                )
            }
        }
    }

    suspend fun getTodayTotalSteps(): Int = withContext(Dispatchers.IO) {
        val todayStr = dateFormatter.format(Date())
        stepDao.getStepsForDateSync(todayStr).sumOf { it.steps }
    }

    suspend fun getTodayTotalCalories(): Float = withContext(Dispatchers.IO) {
        val todayStr = dateFormatter.format(Date())
        stepDao.getStepsForDateSync(todayStr).sumOf { it.calories.toDouble() }.toFloat()
    }

    suspend fun getTodayTotalDurationMinutes(): Int = withContext(Dispatchers.IO) {
        val todayStr = dateFormatter.format(Date())
        stepDao.getStepsForDateSync(todayStr).sumOf { it.durationMinutes }
    }
}
