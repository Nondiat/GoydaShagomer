package com.goydashagomer.nondiat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goydashagomer.nondiat.data.TimeIntervalItem
import com.goydashagomer.nondiat.ui.theme.GoydaCyan
import com.goydashagomer.nondiat.ui.theme.GoydaEmerald

@Composable
fun StepChart(
    items: List<TimeIntervalItem>,
    titleText: String,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onCalendarClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(items) { mutableStateOf<Int?>(null) }

    val maxSteps = remember(items) {
        val maxVal = items.maxOfOrNull { it.steps } ?: 1000
        if (maxVal == 0) 1000 else ((maxVal / 500) + 1) * 500
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Segment Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable date title in top-left
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCalendarClicked() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 3 buttons: "<", ">", Calendar (icon-only, no dark circular backgrounds)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(
                        onClick = onPreviousClicked,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Предыдущий интервал",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onNextClicked,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Следующий интервал",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onCalendarClicked,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Выбрать дату",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Y-Axis Scale labels (Left side)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${maxSteps}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "${maxSteps / 2}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor
                        )
                    }

                    // Bar Chart Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(items) {
                                    detectTapGestures { tapOffset ->
                                        val chartWidth = size.width
                                        val count = items.size
                                        if (count > 0) {
                                            val barSlotWidth = chartWidth / count
                                            val tappedIndex = (tapOffset.x / barSlotWidth).toInt().coerceIn(0, count - 1)
                                            selectedIndex = if (selectedIndex == tappedIndex) null else tappedIndex
                                        }
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height - 24.dp.toPx() // Reserve bottom space for labels
                            val count = items.size
                            if (count == 0) return@Canvas

                            val slotWidth = canvasWidth / count
                            val barWidth = (slotWidth * 0.60f).coerceAtMost(28.dp.toPx())

                            // Draw horizontal dashed grid lines
                            val gridLineEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            drawCircle(color = Color.Transparent, radius = 0f)

                            // Top grid line
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, 0f),
                                end = Offset(canvasWidth, 0f),
                                pathEffect = gridLineEffect,
                                strokeWidth = 1.dp.toPx()
                            )
                            // Mid grid line
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, canvasHeight / 2f),
                                end = Offset(canvasWidth, canvasHeight / 2f),
                                pathEffect = gridLineEffect,
                                strokeWidth = 1.dp.toPx()
                            )
                            // Bottom baseline
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, canvasHeight),
                                end = Offset(canvasWidth, canvasHeight),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // Draw Bars
                            items.forEachIndexed { index, item ->
                                // Blank empty gap when steps is 0 (do not draw bar/point)
                                if (item.steps <= 0) return@forEachIndexed

                                val xCenter = (index * slotWidth) + (slotWidth / 2f)
                                val barLeft = xCenter - (barWidth / 2f)
                                val barHeightRatio = (item.steps.toFloat() / maxSteps).coerceIn(0f, 1f)
                                val barHeight = (canvasHeight * barHeightRatio).coerceAtLeast(4.dp.toPx())
                                val barTop = canvasHeight - barHeight

                                val isSelected = (index == selectedIndex)

                                // Solid monotone colors matching Material You palette
                                val barColor = if (isSelected) {
                                    primaryColor
                                } else {
                                    primaryColor.copy(alpha = 0.45f)
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                            }
                        }

                        // X-Axis Text Labels Below
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .height(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val stepFactor = when {
                                items.size > 20 -> 5
                                items.size > 10 -> 2
                                else -> 1
                            }
                            items.forEachIndexed { index, item ->
                                if (index % stepFactor == 0 || index == items.size - 1) {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (selectedIndex == index) MaterialTheme.colorScheme.primary else onSurfaceColor
                                    )
                                }
                            }
                        }

                        // Popup Window overlay on tapped bar
                        selectedIndex?.let { idx ->
                            if (idx in items.indices) {
                                val item = items[idx]
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        ),
                                        shadowElevation = 6.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = item.fullLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "${item.steps} шагов",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
