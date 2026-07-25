package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.EnergyUnitSetting
import com.example.data.MetricSystemSetting
import com.example.data.StepRepository
import com.example.data.TimeTab
import com.example.data.UserSettingsRepository
import com.example.ui.components.CustomDatePickerDialog
import com.example.ui.components.GoalSegment
import com.example.ui.components.StepChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStepScreen(
    stepRepository: StepRepository,
    settingsRepository: UserSettingsRepository,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TimeTab.DAY) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val settings by settingsRepository.settings.collectAsState()

    // Query chart intervals reactively
    val intervalItemsFlow = remember(selectedTab, selectedDate) {
        when (selectedTab) {
            TimeTab.DAY -> stepRepository.getDayIntervals(selectedDate)
            TimeTab.WEEK -> stepRepository.getWeekIntervals(selectedDate)
            TimeTab.MONTH -> stepRepository.getMonthIntervals(selectedDate)
            TimeTab.YEAR -> stepRepository.getYearIntervals(selectedDate)
        }
    }
    val items by intervalItemsFlow.collectAsState(initial = emptyList())

    // Format header title text according to selected interval tab and date
    val headerTitleText = remember(selectedTab, selectedDate) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val ruLocale = Locale("ru")
        when (selectedTab) {
            TimeTab.DAY -> SimpleDateFormat("d MMMM yyyy", ruLocale).format(selectedDate)
            TimeTab.WEEK -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = SimpleDateFormat("d", ruLocale).format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val end = SimpleDateFormat("d MMMM yyyy", ruLocale).format(cal.time)
                "$start — $end"
            }
            TimeTab.MONTH -> SimpleDateFormat("LLLL yyyy", ruLocale).format(selectedDate).replaceFirstChar { it.uppercase() }
            TimeTab.YEAR -> SimpleDateFormat("yyyy 'год'", ruLocale).format(selectedDate)
        }
    }

    // Totals for selected range
    val totalSteps = remember(items) { items.sumOf { it.steps } }
    val totalCalories = remember(items) { items.sumOf { it.calories.toDouble() }.toFloat() }
    val totalDurationMins = remember(items) { items.sumOf { it.durationMinutes } }

    val formattedEnergy = remember(totalCalories, settings.energyUnit) {
        when (settings.energyUnit) {
            EnergyUnitSetting.KCAL -> "${totalCalories.toInt()} ккал"
            EnergyUnitSetting.CAL -> "${(totalCalories * 1000).toInt()} кал"
            EnergyUnitSetting.KJ -> "${(totalCalories * 4.184f).toInt()} кДж"
        }
    }

    val formattedDistance = remember(totalSteps, settings.metricSystem) {
        val km = (totalSteps * 0.000762f) // standard stride length: 0.762m
        if (settings.metricSystem == MetricSystemSetting.METRIC) {
            if (km < 1.0f) {
                "${(km * 1000).toInt()} м"
            } else {
                String.format(Locale.getDefault(), "%.2f км", km)
            }
        } else {
            val miles = km * 0.621371f
            String.format(Locale.getDefault(), "%.2f миль", miles)
        }
    }

    val formattedDuration = remember(totalDurationMins) {
        if (totalDurationMins >= 60) {
            val hrs = totalDurationMins / 60
            val mins = totalDurationMins % 60
            "${hrs} ч ${mins} мин"
        } else {
            "${totalDurationMins} мин"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GoydaShagomer",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Time Tabs (Day, Week, Month, Year)
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == TimeTab.DAY,
                    onClick = { selectedTab = TimeTab.DAY },
                    text = { Text("День", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == TimeTab.WEEK,
                    onClick = { selectedTab = TimeTab.WEEK },
                    text = { Text("Неделя", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == TimeTab.MONTH,
                    onClick = { selectedTab = TimeTab.MONTH },
                    text = { Text("Месяц", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == TimeTab.YEAR,
                    onClick = { selectedTab = TimeTab.YEAR },
                    text = { Text("Год", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Step Chart
                StepChart(
                    items = items,
                    titleText = headerTitleText,
                    onPreviousClicked = {
                        val cal = Calendar.getInstance().apply { time = selectedDate }
                        when (selectedTab) {
                            TimeTab.DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
                            TimeTab.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -7)
                            TimeTab.MONTH -> cal.add(Calendar.MONTH, -1)
                            TimeTab.YEAR -> cal.add(Calendar.YEAR, -1)
                        }
                        selectedDate = cal.time
                    },
                    onNextClicked = {
                        val cal = Calendar.getInstance().apply { time = selectedDate }
                        when (selectedTab) {
                            TimeTab.DAY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                            TimeTab.WEEK -> cal.add(Calendar.DAY_OF_YEAR, 7)
                            TimeTab.MONTH -> cal.add(Calendar.MONTH, 1)
                            TimeTab.YEAR -> cal.add(Calendar.YEAR, 1)
                        }
                        selectedDate = cal.time
                    },
                    onCalendarClicked = { showDatePickerDialog = true }
                )

                // Statistics Segment (Steps, Calories, Duration, Distance)
                Text(
                    text = "Статистика за период",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Шаги",
                        value = "$totalSteps",
                        icon = Icons.Default.DirectionsWalk,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Энергия",
                        value = formattedEnergy,
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Время",
                        value = formattedDuration,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Дистанция",
                        value = formattedDistance,
                        icon = Icons.Default.DirectionsRun,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Daily Goal Segment
                GoalSegment(
                    currentGoal = settings.dailyStepGoal,
                    onGoalChanged = { newGoal ->
                        settingsRepository.setDailyGoal(newGoal)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDatePickerDialog) {
        CustomDatePickerDialog(
            initialDate = selectedDate,
            dateFormatSetting = settings.dateFormat,
            onDateSelected = { newDate -> selectedDate = newDate },
            onDismiss = { showDatePickerDialog = false }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
