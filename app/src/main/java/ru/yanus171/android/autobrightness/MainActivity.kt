package ru.yanus171.android.autobrightness

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(), SensorEventListener {
    private val BRIGHTNESS_CHANNEL = "brightness_channel"
    private val SENSOR_CHANNEL = "sensor_channel"

    private lateinit var mSensorManager: SensorManager
    private var mLightSensor: Sensor? = null
    private var sensorEventSink: EventChannel.EventSink? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Method Channel for Brightness
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BRIGHTNESS_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getBrightness" -> result.success(getBrightness())
                "setBrightness" -> {
                    val brightness = call.argument<Int>("brightness")
                    if (brightness != null) {
                        setBrightness(brightness)
                        result.success(true)
                    } else {
                        result.error("INVALID_ARG", "Brightness argument is null", null)
                    }
                }
                "hasWritePermission" -> result.success(Settings.System.canWrite(this))
                else -> result.notImplemented()
            }
        }

        // Event Channel for Light Sensor
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, SENSOR_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    sensorEventSink = events
                    mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                    mSensorManager.registerListener(this@MainActivity, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }

                override fun onCancel(arguments: Any?) {
                    mSensorManager.unregisterListener(this@MainActivity)
                    sensorEventSink = null
                }
            }
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            sensorEventSink?.success(event.values[0])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getBrightness(): Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
    }

    private fun setBrightness(value: Int) {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        }
    }
}
