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
        if (stepDao.getRecordCount() > 0) return@withContext

        val cal = Calendar.getInstance()
        // Seed past 365 days of realistic data
        val sampleRecords = mutableListOf<StepRecord>()
        val todayStr = dateFormatter.format(cal.time)

        for (dayOffset in 0..365) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val dateStr = dateFormatter.format(dateCal.time)
            val isWeekend = dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            val totalDayTarget = if (isWeekend) Random.nextInt(5000, 11000) else Random.nextInt(6000, 14000)

            for (hour in 0..23) {
                val hourWeight = when (hour) {
                    in 0..5 -> 0.01f
                    in 6..8 -> 0.08f // Morning commute / walk
                    in 9..11 -> 0.05f
                    in 12..14 -> 0.12f // Lunch walk
                    in 15..17 -> 0.08f
                    in 18..21 -> 0.15f // Evening walk / exercise
                    else -> 0.02f
                }
                val hourSteps = (totalDayTarget * hourWeight * Random.nextFloat().coerceIn(0.7f, 1.3f)).toInt().coerceAtLeast(0)
                if (hourSteps > 0 || hour == cal.get(Calendar.HOUR_OF_DAY) && dateStr == todayStr) {
                    val duration = (hourSteps / 80).coerceAtLeast(if (hourSteps > 0) 1 else 0)
                    val cals = hourSteps * 0.042f
                    sampleRecords.add(
                        StepRecord(
                            dateString = dateStr,
                            hour = hour,
                            steps = hourSteps,
                            durationMinutes = duration,
                            calories = cals
                        )
                    )
                }
            }
        }
        stepDao.insertAll(sampleRecords)
        GoydaWidgetProvider.updateAppWidget(context)
    }

    suspend fun addStepsForTodayHour(addedSteps: Int) = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        val todayStr = dateFormatter.format(cal.time)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        val existing = stepDao.getRecordByDateAndHour(todayStr, hour)
        val newSteps = (existing?.steps ?: 0) + addedSteps
        val newDuration = (newSteps / 80).coerceAtLeast(1)
        val newCals = newSteps * 0.042f

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
