package ru.yanus171.android.autobrightness

import android.hardware.SensorEvent
import android.util.Log
import java.util.Date

const val SENSOR_READ_DURATION_MS = 1000

class SensorValue {
    private var mReadTimer: Date? = null
    private var mValueList = mutableListOf<Int>()
    private var mValue: Int = 0
    fun get(): Int{
        return mValue
    }


    internal fun ready(event: SensorEvent ): Boolean {
        var result = false
        mValueList += event.values[0].toInt()
        if ( mReadTimer == null )
            mReadTimer = Date()
        else if ( Date().time - mReadTimer!!.time > SENSOR_READ_DURATION_MS ) {
            mReadTimer = Date()
            mValue = mValueList.map {it}.average().toInt()
            Log.v( null, "mSensorValueList.count = ${mValueList.size}" )
            mValueList.clear()
            result = true
        }

        return result
    }
}