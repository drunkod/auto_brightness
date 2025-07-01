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
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

const val MAX_BRIGHTNESS_SEEKBAR = 255.0

class MainActivity : SensorEventListener, AppCompatActivity()  {
    private var mSensorValue: Int = 0
    private lateinit var mSensorValueText: TextView
    private lateinit var mBrightnessText: TextView
    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mBrightnessSlider: SeekBar
    private lateinit var mNodeListText: TextView
    private lateinit var mErrorText: TextView
    private var mNeedToSetBrigtness = true
    private var mIsUpdatingGUI = false
    private var mIsNodeListVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity)
        //window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN )
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        mErrorText = findViewById(R.id.errorText)
        mErrorText.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK )
            startActivity(intent)
        }
        mSensorValueText = findViewById(R.id.sensorData)
        mBrightnessText = findViewById(R.id.brightness)
        mBrightnessSlider = findViewById(R.id.seekbar)
        mBrightnessSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if ( mIsUpdatingGUI )
                    return
                setBrightness( seekBarToBrightness( seekBar!!.progress ) )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        } )
        mNodeListText = findViewById(R.id.nodeListInfo)
        mNodeListText.setOnClickListener {
            mIsNodeListVisible = !mIsNodeListVisible
            updateGUI()
        }
        findViewById<Button>(R.id.btnSave ).setOnClickListener {
            MainApplication.mNodeList.set(mSensorValue, getBrightness())
            updateGUI()
            Toast.makeText( this, R.string.nodeSaved, Toast.LENGTH_SHORT ).show()
        }
        findViewById<Button>(R.id.btnAuto ).setOnClickListener {
            setBrightness( MainApplication.mNodeList.getBrightness( mSensorValue ) )
            updateGUI()
            Toast.makeText( this, R.string.brightnessSetToAuto, Toast.LENGTH_SHORT ).show()
        }
        updateGUI()
    }
    private fun updateGUI() {
        mIsUpdatingGUI = true
        mNodeListText.text = if ( mIsNodeListVisible ) MainApplication.mNodeList.getString() else getString( R.string.showNodeList )
        mBrightnessSlider.progress = brightnessToSeekBar( getBrightness() )
        mBrightnessText.text = getString(R.string.brightness) + ": " + seekBarToBrightness( mBrightnessSlider.progress )
        mSensorValueText.text = getString(R.string.sensorData) + ": " + mSensorValue.toString()
        mIsUpdatingGUI = false
    }
    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mNeedToSetBrigtness = true
        mBrightnessSlider.progress = brightnessToSeekBar( getBrightness() )
        updateGUI()
    }

    override fun onPause() {
        mSensorManager.unregisterListener( this )
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if( event != null && event.sensor.type == Sensor.TYPE_LIGHT ) {
            mSensorValue = event.values[0].toInt()
            if ( mNeedToSetBrigtness ) {
                setBrightness(MainApplication.mNodeList.getBrightness(mSensorValue) )
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
            MainService.Companion.setBrightness( value )
            mErrorText.text = ""
            mErrorText.visibility = View.GONE
        } else {
            mErrorText.visibility = View.VISIBLE
            mErrorText.text = getString(R.string.permissionNotGranted_WRITE_SETTINGS)
        }
    }

    fun seekBarToBrightness( progress: Int ): Int {
        return (((progress * progress) /  (MAX_BRIGHTNESS_SEEKBAR * MAX_BRIGHTNESS_SEEKBAR)) *
                (MAX_BRIGHTNESS - MIN_BRIGHTNESS).toFloat() + MIN_BRIGHTNESS.toFloat()).toInt() // Approximate an exponential curve with x^2.
    }
    fun brightnessToSeekBar( brightness: Int ): Int {
        return (sqrt(((brightness - MIN_BRIGHTNESS) / (MAX_BRIGHTNESS - MIN_BRIGHTNESS).toFloat())
                * MAX_BRIGHTNESS_SEEKBAR * MAX_BRIGHTNESS_SEEKBAR)).toInt()
    }
}
