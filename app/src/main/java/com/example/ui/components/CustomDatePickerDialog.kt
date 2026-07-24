package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DateFormatSetting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    initialDate: Date,
    dateFormatSetting: DateFormatSetting,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    var isManualMode by remember { mutableStateOf(false) }

    if (!isManualMode) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.time
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            onDateSelected(Date(millis))
                        }
                        onDismiss()
                    }
                ) {
                    Text("Выбрать", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Календарь",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { isManualMode = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ввести вручную",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                DatePicker(state = datePickerState)
            }
        }
    } else {
        // Manual Input Mode with auto-dot jumping
        val cal = Calendar.getInstance().apply { time = initialDate }
        val dayInitial = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.DAY_OF_MONTH))
        val monthInitial = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MONTH) + 1)
        val yearInitial = String.format(Locale.getDefault(), "%04d", cal.get(Calendar.YEAR))

        var part1 by remember { mutableStateOf(if (dateFormatSetting == DateFormatSetting.DAY_FIRST) dayInitial else monthInitial) }
        var part2 by remember { mutableStateOf(if (dateFormatSetting == DateFormatSetting.DAY_FIRST) monthInitial else dayInitial) }
        var part3 by remember { mutableStateOf(yearInitial) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ввод даты вручную",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { isManualMode = false }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Открыть календарь",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (dateFormatSetting == DateFormatSetting.DAY_FIRST) "Формат: ДД . ММ . ГГГГ" else "Формат: ММ . ДД . ГГГГ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Part 1 (Day or Month)
                        OutlinedTextField(
                            value = part1,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(2)
                                part1 = digits
                            },
                            modifier = Modifier.width(68.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = " . ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Part 2 (Month or Day)
                        OutlinedTextField(
                            value = part2,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(2)
                                part2 = digits
                            },
                            modifier = Modifier.width(68.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = " . ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Part 3 (Year)
                        OutlinedTextField(
                            value = part3,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(4)
                                part3 = digits
                            },
                            modifier = Modifier.width(96.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                try {
                                    val dStr = if (dateFormatSetting == DateFormatSetting.DAY_FIRST) {
                                        "${part1.padStart(2, '0')}.${part2.padStart(2, '0')}.${part3.padStart(4, '0')}"
                                    } else {
                                        "${part2.padStart(2, '0')}.${part1.padStart(2, '0')}.${part3.padStart(4, '0')}"
                                    }
                                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.US).apply { isLenient = false }
                                    val parsedDate = sdf.parse(dStr)
                                    if (parsedDate != null) {
                                        onDateSelected(parsedDate)
                                        onDismiss()
                                    } else {
                                        errorMessage = "Неверная дата"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Введите корректную дату"
                                }
                            }
                        ) {
                            Text("Применить", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
