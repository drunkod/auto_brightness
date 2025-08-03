package ru.yanus171.android.autobrightness

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import ru.yanus171.android.autobrightness.R

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if ( lightSensor != null ) {
            mNodeList = NodeList(lightSensor.maximumRange.toInt())

            createNotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL_ID,
                R.string.service,
                NotificationManager.IMPORTANCE_HIGH
            )

            val intent = Intent(context, MainService::class.java);
            if (Build.VERSION.SDK_INT >= 26)
                context.startForegroundService(intent)
            else
                context.startService(intent)
        }
    }

    private fun createNotificationChannel(channelId: String, captionID: Int, importance: Int) {
        val channel = NotificationChannel(channelId, context!!.getString(captionID), importance)
        channel.description = context.getString(captionID)
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        lateinit var mNodeList: NodeList
        private const val SERVICE_NOTIFICATION_CHANNEL_ID = "Service"
        lateinit var context: Context
    }
}
