package com.example.myapplication

import android.app.Activity
import android.provider.Settings
import android.view.Window
import android.widget.Toast

class Brightness(private val mActivity: Activity) {
    //var mValue : Int = 128
    init {
        //mValue = get()
    }

    fun get(): Int {
        return Settings.System.getInt(mActivity.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
    }

    fun set(value: Int, window: Window) {
        //val brightness : Int = if ( currentAlpha > 254 ) { 1 } else { 255 - currentAlpha }
//        val lp = window.attributes
//        lp.screenBrightness = value / 255F
//        window.attributes = lp
        try {
            Settings.System.putInt(
                window.context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            );
        } catch ( e: SecurityException ) {
            e.printStackTrace();
            Toast.makeText( window.context, R.string.permissionNotGranted_WRITE_SETTINGS, Toast.LENGTH_SHORT ).show()
        }
     }
}