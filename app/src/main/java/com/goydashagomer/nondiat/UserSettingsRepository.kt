package com.goydashagomer.nondiat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppThemeSetting {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class EnergyUnitSetting {
    KCAL, CAL, KJ
}

enum class MetricSystemSetting {
    METRIC, IMPERIAL
}

enum class DateFormatSetting {
    DAY_FIRST, MONTH_FIRST
}

data class UserSettings(
    val dailyStepGoal: Int = 8000,
    val theme: AppThemeSetting = AppThemeSetting.SYSTEM,
    val dynamicColors: Boolean = true,
    val energyUnit: EnergyUnitSetting = EnergyUnitSetting.KCAL,
    val metricSystem: MetricSystemSetting = MetricSystemSetting.METRIC,
    val dateFormat: DateFormatSetting = DateFormatSetting.DAY_FIRST,
    val isHealthSynced: Boolean = false
)

class UserSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("goyda_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings

    private fun loadSettings(): UserSettings {
        return UserSettings(
            dailyStepGoal = prefs.getInt("daily_goal", 8000),
            theme = try { AppThemeSetting.valueOf(prefs.getString("theme", AppThemeSetting.SYSTEM.name) ?: AppThemeSetting.SYSTEM.name) } catch (e: Exception) { AppThemeSetting.SYSTEM },
            dynamicColors = prefs.getBoolean("dynamic_colors", true),
            energyUnit = try { EnergyUnitSetting.valueOf(prefs.getString("energy_unit", EnergyUnitSetting.KCAL.name) ?: EnergyUnitSetting.KCAL.name) } catch (e: Exception) { EnergyUnitSetting.KCAL },
            metricSystem = try { MetricSystemSetting.valueOf(prefs.getString("metric_system", MetricSystemSetting.METRIC.name) ?: MetricSystemSetting.METRIC.name) } catch (e: Exception) { MetricSystemSetting.METRIC },
            dateFormat = try { DateFormatSetting.valueOf(prefs.getString("date_format", DateFormatSetting.DAY_FIRST.name) ?: DateFormatSetting.DAY_FIRST.name) } catch (e: Exception) { DateFormatSetting.DAY_FIRST },
            isHealthSynced = prefs.getBoolean("health_synced", false)
        )
    }

    fun setDailyGoal(goal: Int) {
        val validGoal = goal.coerceIn(1, 1000000)
        prefs.edit().putInt("daily_goal", validGoal).apply()
        _settings.value = _settings.value.copy(dailyStepGoal = validGoal)
    }

    fun setTheme(theme: AppThemeSetting) {
        prefs.edit().putString("theme", theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun setDynamicColors(enabled: Boolean) {
        prefs.edit().putBoolean("dynamic_colors", enabled).apply()
        _settings.value = _settings.value.copy(dynamicColors = enabled)
    }

    fun setEnergyUnit(unit: EnergyUnitSetting) {
        prefs.edit().putString("energy_unit", unit.name).apply()
        _settings.value = _settings.value.copy(energyUnit = unit)
    }

    fun setMetricSystem(system: MetricSystemSetting) {
        prefs.edit().putString("metric_system", system.name).apply()
        _settings.value = _settings.value.copy(metricSystem = system)
    }

    fun setDateFormat(format: DateFormatSetting) {
        prefs.edit().putString("date_format", format.name).apply()
        _settings.value = _settings.value.copy(dateFormat = format)
    }

    fun setHealthSynced(synced: Boolean) {
        prefs.edit().putBoolean("health_synced", synced).apply()
        _settings.value = _settings.value.copy(isHealthSynced = synced)
    }
}
