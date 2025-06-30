package ru.yanus171.android.autobrightness

import android.app.Activity
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast


class MainActivity : SensorEventListener, Activity()  {
    //private lateinit var mBrightness: Brightness
    private var mSensorValue: Int = 0
    private lateinit var mSensorValueText: TextView
    private lateinit var mBrightnessText: TextView
    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mBrightnessSlider: SeekBar
    private lateinit var mNodeListText: TextView
    private lateinit var mErrorText: TextView
    private var mNodeList: NodeList? = null
    private var mNeedToSetBrigtness = true
    private var mIsUpdatingGUI = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        //mBrightness = Brightness( this )
        mErrorText = findViewById(R.id.errorText)
        mErrorText.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK )
            //intent.setData(Uri.parse("package:dummy"));
            startActivity(intent);
        }
        mSensorValueText = findViewById(R.id.sensorData)
        mBrightnessText = findViewById(R.id.brightness)
        mBrightnessSlider = findViewById(R.id.seekbar)
        mBrightnessSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if ( mIsUpdatingGUI )
                    return
                setBrightness( seekBar!!.progress )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        } )
        mNodeListText = findViewById(R.id.nodeListInfo)
        findViewById<Button>(R.id.btnSave ).setOnClickListener {
            mNodeList!!.set(mSensorValue, getBrightness())
            updateGUI()
        }
        findViewById<Button>(R.id.btnAuto ).setOnClickListener {
            setBrightness( mNodeList!!.getBrightness( mSensorValue ) )
            updateGUI()
        }

        updateGUI()
    }
    private fun updateGUI() {
        if ( mNodeList == null )
            return
        mIsUpdatingGUI = true
        mNodeListText.text = mNodeList!!.getString()
        mBrightnessSlider.progress = getBrightness()
        mBrightnessText.text = getString(R.string.brightness) + ": " + mBrightnessSlider.progress
        mSensorValueText.text = getString(R.string.sensorData) + ": " + mSensorValue.toString()
        mIsUpdatingGUI = false
    }
    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mNeedToSetBrigtness = true
        mBrightnessSlider.progress = getBrightness()
        updateGUI()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if( event != null && event.sensor.type == Sensor.TYPE_LIGHT ) {
            mSensorValue = event.values[0].toInt()
            if ( mNeedToSetBrigtness ) {
                mNodeList = NodeList(mLightSensor.maximumRange.toInt())
                setBrightness( mNodeList!!.getBrightness(mSensorValue) )
            }
            mNeedToSetBrigtness = false
            updateGUI()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun getBrightness(): Int {
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
    }

    fun setBrightness(value: Int) {
        if ( Settings.System.canWrite(this) ) {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            mErrorText.text = ""
            mErrorText.visibility = View.GONE
        } else
            mErrorText.visibility = View.VISIBLE
            mErrorText.text = getString(R.string.permissionNotGranted_WRITE_SETTINGS)
    }
}
