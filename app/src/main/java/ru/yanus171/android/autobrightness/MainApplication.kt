package ru.yanus171.android.autobrightness

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import ru.yanus171.android.autobrightness.R

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        createNotificationChannel(
            SERVICE_NOTIFICATION_CHANNEL_ID,
            R.string.service,
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    private fun createNotificationChannel(channelId: String, captionID: Int, importance: Int) {
        val context = context
        val channel = NotificationChannel(channelId, context!!.getString(captionID), importance)
        channel.description = context.getString(captionID)
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val SERVICE_NOTIFICATION_CHANNEL_ID = "Service"
        var context: Context? = null
            private set
    }
}
