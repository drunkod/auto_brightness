package com.example.myapplication

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView


class MainActivity : SensorEventListener, Activity()  {
    private lateinit var mBrightness: Brightness
    private lateinit var mSensorValueText: TextView
    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mBrightnessSlider: SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        mBrightness = Brightness( this )
        mSensorValueText = findViewById(R.id.sensorData)
        mBrightnessSlider = findViewById<SeekBar>(R.id.seekbar)
        mBrightnessSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mBrightness.set( seekBar!!.progress, window )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        } )
    }
    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        mBrightnessSlider.progress = mBrightness.get()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if( event != null && event.sensor.type == Sensor.TYPE_LIGHT )
            mSensorValueText.text = getString( R.string.sensorData ) + ": " + event.values[0].toInt().toString()
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
