package com.goydashagomer.nondiat.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.goydashagomer.nondiat.data.StepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StepSensorManager(
    private val context: Context,
    private val repository: StepRepository,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var lastSensorValue = -1f

    fun startListening() {
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun resetBaseline() {
        lastSensorValue = -1f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSensorSteps = event.values[0]
            if (lastSensorValue < 0) {
                lastSensorValue = totalSensorSteps
                return
            }
            val delta = (totalSensorSteps - lastSensorValue).toInt()
            if (delta in 1..9999) {
                lastSensorValue = totalSensorSteps
                scope.launch(Dispatchers.IO) {
                    repository.addStepsForTodayHour(delta)
                }
            } else if (delta < 0) {
                // Device rebooted, reset baseline
                lastSensorValue = totalSensorSteps
            }
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            val detectorSteps = event.values[0].toInt().coerceAtLeast(1)
            scope.launch(Dispatchers.IO) {
                repository.addStepsForTodayHour(detectorSteps)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
