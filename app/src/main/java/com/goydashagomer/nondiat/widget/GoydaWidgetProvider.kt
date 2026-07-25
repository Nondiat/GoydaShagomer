package com.goydashagomer.nondiat.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.goydashagomer.nondiat.MainActivity
import com.goydashagomer.nondiat.R
import com.goydashagomer.nondiat.data.AppDatabase
import com.goydashagomer.nondiat.data.AppThemeSetting
import com.goydashagomer.nondiat.data.EnergyUnitSetting
import com.goydashagomer.nondiat.data.UserSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoydaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidgetInstance(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GoydaWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateWidgetInstance(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidgetInstance(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val settingsRepo = UserSettingsRepository(context)
                val settings = settingsRepo.settings.value

                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val todayRecords = db.stepDao().getStepsForDateSync(todayStr)

                val steps = todayRecords.sumOf { it.steps }
                val durationMins = todayRecords.sumOf { it.durationMinutes }
                val totalCals = todayRecords.sumOf { it.calories.toDouble() }.toFloat()

                val goal = settings.dailyStepGoal.coerceAtLeast(1)

                val energyFormatted = when (settings.energyUnit) {
                    EnergyUnitSetting.KCAL -> "${totalCals.toInt()}"
                    EnergyUnitSetting.CAL -> "${(totalCals * 1000).toInt()}"
                    EnergyUnitSetting.KJ -> "${(totalCals * 4.184f).toInt()}"
                }

                val durationFormatted = "$durationMins"

                val currentUiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val isNightMode = currentUiMode == Configuration.UI_MODE_NIGHT_YES
                val isDark = when (settings.theme) {
                    AppThemeSetting.DARK, AppThemeSetting.AMOLED -> true
                    AppThemeSetting.LIGHT -> false
                    AppThemeSetting.SYSTEM -> isNightMode
                }

                val bitmap = renderWidgetBitmap(
                    context = context,
                    steps = steps,
                    goal = goal,
                    durationText = durationFormatted,
                    energyText = energyFormatted,
                    isDark = isDark
                )

                val views = RemoteViews(context.packageName, R.layout.goyda_widget_layout)
                views.setImageViewBitmap(R.id.widget_canvas_image, bitmap)

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_canvas_image, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun renderWidgetBitmap(
            context: Context,
            steps: Int,
            goal: Int,
            durationText: String,
            energyText: String,
            isDark: Boolean
        ): Bitmap {
            val size = 320
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val center = size / 2f
            val radius = size * 0.40f
            val strokeWidth = size * 0.08f

            // Material You dynamic surface & accent colors for Pixel Launcher
            val cardBgColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ContextCompat.getColor(context, if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_100)
                } catch (e: Exception) {
                    if (isDark) Color.parseColor("#1C1B1F") else Color.parseColor("#F4EFF4")
                }
            } else {
                if (isDark) Color.parseColor("#1C1B1F") else Color.parseColor("#F4EFF4")
            }

            val primaryAccent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ContextCompat.getColor(context, if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600)
                } catch (e: Exception) {
                    if (isDark) Color.parseColor("#D0BCFF") else Color.parseColor("#6750A4")
                }
            } else {
                if (isDark) Color.parseColor("#D0BCFF") else Color.parseColor("#6750A4")
            }

            val trackColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ContextCompat.getColor(context, if (isDark) android.R.color.system_neutral2_700 else android.R.color.system_neutral2_200)
                } catch (e: Exception) {
                    if (isDark) Color.parseColor("#49454F") else Color.parseColor("#E7E0EC")
                }
            } else {
                if (isDark) Color.parseColor("#49454F") else Color.parseColor("#E7E0EC")
            }

            val textColorPrimary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ContextCompat.getColor(context, if (isDark) android.R.color.system_accent1_100 else android.R.color.system_neutral1_900)
                } catch (e: Exception) {
                    if (isDark) Color.parseColor("#E6E1E5") else Color.parseColor("#1C1B1F")
                }
            } else {
                if (isDark) Color.parseColor("#E6E1E5") else Color.parseColor("#1C1B1F")
            }

            val textColorSecondary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ContextCompat.getColor(context, if (isDark) android.R.color.system_neutral2_200 else android.R.color.system_neutral2_700)
                } catch (e: Exception) {
                    if (isDark) Color.parseColor("#CAC4D0") else Color.parseColor("#49454F")
                }
            } else {
                if (isDark) Color.parseColor("#CAC4D0") else Color.parseColor("#49454F")
            }

            // Card background (MD3 pill card - Material You dynamic surface)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = cardBgColor
                style = Paint.Style.FILL
            }
            val bgRect = RectF(8f, 8f, size - 8f, size - 8f)
            canvas.drawRoundRect(bgRect, 48f, 48f, bgPaint)

            // Progress Track
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = trackColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            val oval = RectF(
                center - radius,
                center - radius,
                center + radius,
                center + radius
            )
            canvas.drawArc(oval, 0f, 360f, false, trackPaint)

            // Progress Arc Fill - Solid Material You color (no gradient)
            val progressRatio = (steps.toFloat() / goal).coerceIn(0f, 1f)
            val sweepAngle = progressRatio * 360f

            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
                color = primaryAccent
            }
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)

            // Inner Center Text Paints
            val textPaintPrimary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColorPrimary
                textSize = size * 0.11f
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }

            val textPaintSecondary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColorSecondary
                textSize = size * 0.075f
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }

            val textPaintAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryAccent
                textSize = size * 0.075f
                textAlign = Paint.Align.LEFT
                isFakeBoldText = true
            }

            val iconDim = (size * 0.07f).toInt()
            val spacing = size * 0.02f

            // Load MD3 vector icons
            val stepsDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_widget_steps)?.apply {
                setTint(textColorPrimary)
            }
            val timerDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_widget_timer)?.apply {
                setTint(textColorSecondary)
            }
            val fireDrawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_widget_fire)?.apply {
                setTint(primaryAccent)
            }

            // Line 1: Steps
            val stepsStr = "$steps"
            val stepsTextWidth = textPaintPrimary.measureText(stepsStr)
            val line1Width = iconDim + spacing + stepsTextWidth
            val line1StartX = center - (line1Width / 2f)
            val line1Y = center - size * 0.08f

            stepsDrawable?.let {
                val iconTop = (line1Y - iconDim + size * 0.015f).toInt()
                it.setBounds(line1StartX.toInt(), iconTop, (line1StartX + iconDim).toInt(), iconTop + iconDim)
                it.draw(canvas)
            }
            canvas.drawText(stepsStr, line1StartX + iconDim + spacing, line1Y, textPaintPrimary)

            // Line 2: Duration in minutes
            val durationStr = durationText
            val durationTextWidth = textPaintSecondary.measureText(durationStr)
            val line2Width = iconDim + spacing + durationTextWidth
            val line2StartX = center - (line2Width / 2f)
            val line2Y = center + size * 0.07f

            timerDrawable?.let {
                val iconTop = (line2Y - iconDim + size * 0.01f).toInt()
                it.setBounds(line2StartX.toInt(), iconTop, (line2StartX + iconDim).toInt(), iconTop + iconDim)
                it.draw(canvas)
            }
            canvas.drawText(durationStr, line2StartX + iconDim + spacing, line2Y, textPaintSecondary)

            // Line 3: Energy
            val energyStr = energyText
            val energyTextWidth = textPaintAccent.measureText(energyStr)
            val line3Width = iconDim + spacing + energyTextWidth
            val line3StartX = center - (line3Width / 2f)
            val line3Y = center + size * 0.20f

            fireDrawable?.let {
                val iconTop = (line3Y - iconDim + size * 0.01f).toInt()
                it.setBounds(line3StartX.toInt(), iconTop, (line3StartX + iconDim).toInt(), iconTop + iconDim)
                it.draw(canvas)
            }
            canvas.drawText(energyStr, line3StartX + iconDim + spacing, line3Y, textPaintAccent)

            return bitmap
        }
    }
}
