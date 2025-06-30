package ru.yanus171.android.autobrightness

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import ru.yanus171.android.autobrightness.MainService.Companion.setBrightness

const val SERVICE_NOTIFICATION_CHANNEL_ID = "service"

class MainService : Service(), SensorEventListener {
    var mNodeList: NodeList? = null
    val mReceiver = Receiver()
    override fun onCreate() {

        val receiverFilter = IntentFilter()
        receiverFilter.addAction(Intent.ACTION_SCREEN_ON)
        receiverFilter.priority = 999
        registerReceiver(mReceiver, receiverFilter)

        stopForeground(true);
        if (Build.VERSION.SDK_INT >= 26)
            createNotificationChannel(SERVICE_NOTIFICATION_CHANNEL_ID, getString(R.string.persistentNotification), NotificationManager.IMPORTANCE_NONE, false);
        startForeground(R.string.notification, createPersistentNotification());
    }

    private fun createPersistentNotification(): Notification {
        val builder =
        NotificationCompat.Builder( baseContext, SERVICE_NOTIFICATION_CHANNEL_ID )
        .setSmallIcon(R.drawable.rounded_brightness_7_24);
        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel( id: String, caption: String, importance: Int, buble: Boolean ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager;

            val channel = NotificationChannel(id, caption, importance);
            channel.description = getString(R.string.app_name);
            if (buble && Build.VERSION.SDK_INT >= 29)
                channel.setAllowBubbles( true );
            try {
                manager.createNotificationChannel(channel);
            } catch ( e: Exception ) {
                e.printStackTrace();
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        fun setBrightness(value: Int) {
            if ( Settings.System.canWrite(MainApplication.context) )
                Settings.System.putInt(
                    MainApplication.context!!.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    value
                )
        }
    }
}

class Receiver() : BroadcastReceiver(), SensorEventListener {
    lateinit var mSensorManager: SensorManager
    override fun onReceive(context: Context?, intent: Intent?) {
        if ( intent == null )
            return
        if ( !intent.action.equals( Intent.ACTION_SCREEN_ON ) )
            return
        mSensorManager = context!!.getSystemService(SENSOR_SERVICE) as SensorManager
        val lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorValue = event!!.values[0].toInt()
        val brightness = MainApplication.mNodeList.getBrightness( sensorValue )
        setBrightness( brightness)
        //Toast.makeText(MainApplication.context, "Brightness $brightness was set for sensor value $sensorValue", Toast.LENGTH_SHORT ).show()
        mSensorManager.unregisterListener( this )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}