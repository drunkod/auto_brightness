package ru.yanus171.android.autobrightness

import android.content.Context

private const val NODE_LIST_PREF = "flutter.node_list5" // Note the 'flutter.' prefix added by the plugin
private const val NODE_SEP = "|"
private const val NODE_FIELD_SEP = ";"
const val MAX_BRIGHTNESS = 255

class Node(s: String) {
    private val list = s.split(NODE_FIELD_SEP)
    val mBrightness: Int = list[0].toInt()
    val mSensorValue: Int = list[1].toInt()
}

class NodeList(context: Context) {
    private var mList: MutableList<Node> = mutableListOf()

    init {
        val settings = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val s = settings.getString(NODE_LIST_PREF, null)
        if (s != null) {
            for (item in s.split(NODE_SEP))
                if (item.isNotEmpty()) {
                    mList.add(Node(item))
                }
        }
    }

    fun getBrightness(sensorValue: Int): Int {
        for (node in mList)
            if (sensorValue < node.mSensorValue)
                return node.mBrightness
        return MAX_BRIGHTNESS
    }
}