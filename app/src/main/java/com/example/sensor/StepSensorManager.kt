package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.StepRepository
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

    private var initialStepCount = -1f

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

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val stepsToRecord = if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSensorSteps = event.values[0]
            if (initialStepCount < 0) {
                initialStepCount = totalSensorSteps
                0
            } else {
                val diff = (totalSensorSteps - initialStepCount).toInt()
                if (diff > 0) {
                    initialStepCount = totalSensorSteps
                    diff
                } else 0
            }
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            event.values[0].toInt()
        } else 0

        if (stepsToRecord > 0) {
            scope.launch(Dispatchers.IO) {
                repository.addStepsForTodayHour(stepsToRecord)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
