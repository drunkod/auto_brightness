package ru.yanus171.android.autobrightness

import android.preference.PreferenceManager
import android.text.TextUtils
import androidx.core.content.edit

private const val NODE_LIST_PREF = "node_list5"
private const val NODE_SEP = "|"
private const val NODE_FIELD_SEP = ";"
const val NODE_COUNT = 20
const val MAX_BRIGHTNESS = 255
const val MIN_BRIGHTNESS = 0
const val SENSOR_STEP_COEFF = 1.5
class NodeList(private val mMaxSensorValue: Int) {
    class Node {
        constructor(sensorValue: Int, brightness: Int) {
            mBrightness = brightness
            mSensorValue = sensorValue
        }

        constructor(s: String) {
            val list = s.split(NODE_FIELD_SEP)
            mBrightness = list[0].toInt()
            mSensorValue = list[1].toInt()
        }
        fun getString(): String {
            return "s=$mSensorValue, b=$mBrightness"
        }

        fun save(): String {
            return "$mBrightness$NODE_FIELD_SEP$mSensorValue"
        }

        internal var mBrightness: Int = 0
        var mSensorValue: Int = 0


    }

    private var mList: MutableList<Node> = mutableListOf()

    init {
        val settings = PreferenceManager.getDefaultSharedPreferences(MainApplication.context)
        var s = settings.getString(NODE_LIST_PREF, null)
        if (s == null)
            createDefaultList()
        else
            for (item in s.split(NODE_SEP))
                mList.add(Node(item))
    }

    private fun createDefaultList() {
        val bStep = MAX_BRIGHTNESS / NODE_COUNT
        var b = 0
        var s = 10
        mList.clear()
        while (s < mMaxSensorValue) {
            s = (s * 1.5).toInt()
            b += bStep
            mList += Node(s, b)
        }
    }

    fun getBrightness(sensorValue: Int): Int {
        val node = getNodeFor( sensorValue )
        if ( node != null )
            return node.mBrightness
        return MAX_BRIGHTNESS
    }
    private fun getNodeFor(sensorValue: Int): Node? {
        for (node in mList)
            if ( sensorValue < node.mSensorValue )
                return node
        return null
    }


    fun addNodesTo(maxSensorValue: Int ) {
        var sensorValue = mList.last().mSensorValue
        val brightness = mList.last().mBrightness
        while ( sensorValue < maxSensorValue ) {
            sensorValue = (sensorValue * SENSOR_STEP_COEFF).toInt()
            mList += Node( sensorValue, brightness )
        }
    }

    fun set(sensorValue: Int, newBrightness: Int) {
        if ( sensorValue > mList.last().mSensorValue )
            addNodesTo( sensorValue )
        for (it in mList)
            if (sensorValue < it.mSensorValue) {
                it.mBrightness = newBrightness
                makePrevNonesNotBrighterThen( it )
                makeNextNonesNotDarkerThen( it )
                break
            }
        save()
    }

    private fun makePrevNonesNotBrighterThen( node: Node ) {
        for (it in mList.reversed())
            if ( it.mSensorValue < node.mSensorValue && it.mBrightness > node.mBrightness )
                it.mBrightness = node.mBrightness
    }
    private fun makeNextNonesNotDarkerThen( node: Node ) {
        for (it in mList)
            if ( it.mSensorValue > node.mSensorValue && it.mBrightness < node.mBrightness )
                it.mBrightness = node.mBrightness
    }
    fun getString( sensorValue: Int ): String {
        val currentNode = getNodeFor( sensorValue )
        var result = mutableListOf<String>()
        for (node in mList) {
            val currentText = if ( currentNode != null && currentNode.mSensorValue == node.mSensorValue ) "* " else ""
            result += (currentText + node.getString())
        }
        return TextUtils.join("\n", result)
    }

    fun save() {
        var result = mutableListOf<String>()
        for (node in mList)
            result += node.save()
        val settings = PreferenceManager.getDefaultSharedPreferences(MainApplication.context)
        settings.edit() { putString(NODE_LIST_PREF, TextUtils.join(NODE_SEP, result)) }
    }
}