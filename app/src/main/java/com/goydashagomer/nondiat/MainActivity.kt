package com.goydashagomer.nondiat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.goydashagomer.nondiat.data.AppDatabase
import com.goydashagomer.nondiat.data.StepRepository
import com.goydashagomer.nondiat.data.UserSettingsRepository
import com.goydashagomer.nondiat.sensor.StepSensorManager
import com.goydashagomer.nondiat.ui.screens.HealthSyncOnboardingScreen
import com.goydashagomer.nondiat.ui.screens.MainStepScreen
import com.goydashagomer.nondiat.ui.screens.SettingsScreen
import com.goydashagomer.nondiat.ui.theme.GoydaShagomerTheme
import com.goydashagomer.nondiat.widget.GoydaWidgetProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var stepRepository: StepRepository
    private lateinit var settingsRepository: UserSettingsRepository
    private lateinit var sensorManager: StepSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(this)
        stepRepository = StepRepository(database.stepDao(), this)
        settingsRepository = UserSettingsRepository(this)

        setContent {
            val settings by settingsRepository.settings.collectAsState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                sensorManager = StepSensorManager(this@MainActivity, stepRepository, scope)
                sensorManager.startListening()
                GoydaWidgetProvider.updateAppWidget(this@MainActivity)
            }

            DisposableEffect(Unit) {
                onDispose {
                    if (::sensorManager.isInitialized) {
                        sensorManager.stopListening()
                    }
                }
            }

            GoydaShagomerTheme(
                appTheme = settings.theme,
                dynamicColor = settings.dynamicColors
            ) {
                val navController = rememberNavController()
                val startDestination = if (settings.isHealthSynced) "main" else "onboarding"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("onboarding") {
                        HealthSyncOnboardingScreen(
                            onPermissionGranted = {
                                settingsRepository.setHealthSynced(true)
                                navController.navigate("main") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainStepScreen(
                            stepRepository = stepRepository,
                            settingsRepository = settingsRepository,
                            onSettingsClicked = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            settingsRepository = settingsRepository,
                            onBackClicked = {
                                navController.popBackStack()
                            },
                            onClearAllDataConfirmed = {
                                scope.launch {
                                    stepRepository.clearAllData()
                                    if (::sensorManager.isInitialized) {
                                        sensorManager.resetBaseline()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
