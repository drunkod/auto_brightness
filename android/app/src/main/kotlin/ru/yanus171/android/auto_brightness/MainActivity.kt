package ru.yanus171.android.auto_brightness

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val SENSOR_CHANNEL = "sensor_channel"
    private val BRIGHTNESS_CHANNEL = "brightness_channel"

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var sensorEventListener: SensorEventListener? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Sensor EventChannel setup
        val sensorEventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, SENSOR_CHANNEL)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        sensorEventChannel.setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    if (lightSensor == null) {
                        // Notify Dart side there is no light sensor; it may switch to a debug fallback.
                        events?.error("no_sensor", "Light sensor not available on this device", null)
                        return
                    }
                    sensorEventListener = createSensorEventListener(events)
                    sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                }

                override fun onCancel(arguments: Any?) {
                    if (sensorEventListener != null) {
                        sensorManager.unregisterListener(sensorEventListener)
                    }
                }
            }
        )

        // Brightness MethodChannel setup
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BRIGHTNESS_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getBrightness" -> {
                    val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                    result.success(brightness)
                }
                "setBrightness" -> {
                    val brightness = call.argument<Int>("brightness")
                    if (brightness != null) {
                        try {
                            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                            val lp = window.attributes
                            lp.screenBrightness = brightness / 255.0f
                            window.attributes = lp
                            result.success(true)
                        } catch (e: Exception) {
                            result.error("error", e.message, null)
                        }
                    } else {
                        result.error("error", "brightness argument is null", null)
                    }
                }
                "hasWritePermission" -> {
                    val hasPermission = Settings.System.canWrite(this)
                    result.success(hasPermission)
                }
                "openWriteSettings" -> {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = Uri.parse("package:$packageName")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("error", e.message, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun createSensorEventListener(events: EventChannel.EventSink?): SensorEventListener {
        return object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    events?.success(event.values[0])
                }
            }
        }
    }
}
