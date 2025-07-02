package ru.yanus171.android.autobrightness

import android.preference.PreferenceManager
import android.text.TextUtils
import androidx.core.content.edit

private const val NODE_LIST_PREF = "node_list3"
private const val NODE_SEP = "|"
private const val NODE_FIELD_SEP = ";"
const val NODE_COUNT = 20
const val MAX_BRIGHTNESS = 255
const val MIN_BRIGHTNESS = 0
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
        val sStep = getSensorStep()
        var b = 0
        var s = 0
        mList.clear()
        while (s < mMaxSensorValue) {
            s += sStep
            b += bStep
            mList += Node(s, b)
        }
    }

    private fun getSensorStep(): Int = mMaxSensorValue / NODE_COUNT
    fun getBrightness(sensorValue: Int): Int {
        for (node in mList)
            if (sensorValue < node.mSensorValue)
                return node.mBrightness
        return MAX_BRIGHTNESS
    }

    fun set(sensorValue: Int, newBrightness: Int) {
        val iterator = mList.listIterator()
        for (it in iterator) {
            if (sensorValue < it.mSensorValue) {
                iterator.set(Node(it.mSensorValue, newBrightness))
                break
            }
        }
        save()
    }

    fun getString(): String {
        var result = mutableListOf<String>()
        for (node in mList)
            result += node.getString()
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