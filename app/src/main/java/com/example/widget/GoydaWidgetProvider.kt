package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.EnergyUnitSetting
import com.example.data.UserSettingsRepository
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
                    EnergyUnitSetting.KCAL -> "${totalCals.toInt()} ккал"
                    EnergyUnitSetting.CAL -> "${(totalCals * 1000).toInt()} кал"
                    EnergyUnitSetting.KJ -> "${(totalCals * 4.184f).toInt()} кДж"
                }

                val durationFormatted = if (durationMins >= 60) {
                    val hrs = durationMins / 60
                    val mins = durationMins % 60
                    "${hrs}ч ${mins}м"
                } else {
                    "${durationMins}м"
                }

                val bitmap = renderWidgetBitmap(
                    steps = steps,
                    goal = goal,
                    durationText = durationFormatted,
                    energyText = energyFormatted,
                    isDark = true
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

            // Card background (MD3 pill card)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#12191B")
                style = Paint.Style.FILL
            }
            val bgRect = RectF(10f, 10f, size - 10f, size - 10f)
            canvas.drawRoundRect(bgRect, 48f, 48f, bgPaint)

            // Progress Track
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#263238")
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

            // Progress Arc Fill
            val progressRatio = (steps.toFloat() / goal).coerceIn(0f, 1f)
            val sweepAngle = progressRatio * 360f

            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
                shader = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    Color.parseColor("#00BCD4"),
                    Color.parseColor("#4CAF50"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)

            // Inner Center Text
            val textPaintPrimary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = size * 0.12f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val textPaintSecondary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B0BEC5")
                textSize = size * 0.075f
                textAlign = Paint.Align.CENTER
            }

            val textPaintAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#80DEEA")
                textSize = size * 0.075f
                textAlign = Paint.Align.CENTER
            }

            // Draw steps with icon
            canvas.drawText("🚶 $steps", center, center - size * 0.10f, textPaintPrimary)

            // Draw time walked
            canvas.drawText("⏱️ $durationText", center, center + size * 0.06f, textPaintSecondary)

            // Draw energy
            canvas.drawText("🔥 $energyText", center, center + size * 0.20f, textPaintAccent)

            return bitmap
        }
    }
}
