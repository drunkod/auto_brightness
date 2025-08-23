package ru.yanus171.android.autobrightness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import java.util.Date

const val SENSOR_READ_DURATION_MS = 100

class ScreenBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            if (lightSensor != null) {
                val sensorListener = object : SensorEventListener {
                    private var mReadTimer: Date? = null
                    private var mValueList = mutableListOf<Int>()

                    override fun onSensorChanged(event: SensorEvent) {
                        mValueList.add(event.values[0].toInt())
                        if (mReadTimer == null) {
                            mReadTimer = Date()
                        } else if (Date().time - mReadTimer!!.time > SENSOR_READ_DURATION_MS) {
                            sensorManager.unregisterListener(this)
                            val avgSensorValue = mValueList.average().toInt()
                            setBrightnessBasedOnSensorValue(context, avgSensorValue)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                }
                sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun setBrightnessBasedOnSensorValue(context: Context, sensorValue: Int) {
        if (Settings.System.canWrite(context)) {
            val nodeList = NodeList(context)
            val brightness = nodeList.getBrightness(sensorValue)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
            )
        }
    }
}
