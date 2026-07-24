package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppThemeSetting
import com.example.data.DateFormatSetting
import com.example.data.EnergyUnitSetting
import com.example.data.MetricSystemSetting
import com.example.data.UserSettingsRepository
import com.example.ui.components.RotatingFooter
import com.example.ui.components.ThemePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: UserSettingsRepository,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsRepository.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Theme Selection Section
            Text(
                text = "Тема оформления",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Grid of 4 theme preview cards (2x2)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemePreviewCard(
                        themeSetting = AppThemeSetting.SYSTEM,
                        isSelected = settings.theme == AppThemeSetting.SYSTEM,
                        onSelect = { settingsRepository.setTheme(AppThemeSetting.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemePreviewCard(
                        themeSetting = AppThemeSetting.LIGHT,
                        isSelected = settings.theme == AppThemeSetting.LIGHT,
                        onSelect = { settingsRepository.setTheme(AppThemeSetting.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemePreviewCard(
                        themeSetting = AppThemeSetting.DARK,
                        isSelected = settings.theme == AppThemeSetting.DARK,
                        onSelect = { settingsRepository.setTheme(AppThemeSetting.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemePreviewCard(
                        themeSetting = AppThemeSetting.AMOLED,
                        isSelected = settings.theme == AppThemeSetting.AMOLED,
                        onSelect = { settingsRepository.setTheme(AppThemeSetting.AMOLED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Material You Цвета",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Динамическая палитра на основе обоев системы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.dynamicColors,
                            onCheckedChange = { settingsRepository.setDynamicColors(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Energy Unit Section
            Text(
                text = "Вид сжигаемой энергии",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.energyUnit == EnergyUnitSetting.KCAL,
                    onClick = { settingsRepository.setEnergyUnit(EnergyUnitSetting.KCAL) },
                    label = { Text("ккал") }
                )
                FilterChip(
                    selected = settings.energyUnit == EnergyUnitSetting.CAL,
                    onClick = { settingsRepository.setEnergyUnit(EnergyUnitSetting.CAL) },
                    label = { Text("кал") }
                )
                FilterChip(
                    selected = settings.energyUnit == EnergyUnitSetting.KJ,
                    onClick = { settingsRepository.setEnergyUnit(EnergyUnitSetting.KJ) },
                    label = { Text("кДж") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Metric System Section
            Text(
                text = "Метрическая система",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.metricSystem == MetricSystemSetting.METRIC,
                    onClick = { settingsRepository.setMetricSystem(MetricSystemSetting.METRIC) },
                    label = { Text("Километры / кг") }
                )
                FilterChip(
                    selected = settings.metricSystem == MetricSystemSetting.IMPERIAL,
                    onClick = { settingsRepository.setMetricSystem(MetricSystemSetting.IMPERIAL) },
                    label = { Text("Мили / фунты") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Date Format Section
            Text(
                text = "Формат краткой даты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.dateFormat == DateFormatSetting.DAY_FIRST,
                    onClick = { settingsRepository.setDateFormat(DateFormatSetting.DAY_FIRST) },
                    label = { Text("День сначала (24.07.2026)") }
                )
                FilterChip(
                    selected = settings.dateFormat == DateFormatSetting.MONTH_FIRST,
                    onClick = { settingsRepository.setDateFormat(DateFormatSetting.MONTH_FIRST) },
                    label = { Text("Месяц сначала (07.24.2026)") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Multi-language Footer Signature
            RotatingFooter()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
