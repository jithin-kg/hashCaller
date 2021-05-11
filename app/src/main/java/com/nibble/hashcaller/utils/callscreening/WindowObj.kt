package com.nibble.hashcaller.utils.callscreening

import android.util.Log
import com.nibble.hashcaller.view.ui.call.floating.Window
import java.lang.ref.WeakReference

object WindowObj {
    private var window: WeakReference<Window>? = null

    fun setWindow(obj: Window){
        window = WeakReference(obj)
    }

    fun closeWindow(){
       val windowStrong =  window?.get() //get strong reference
        windowStrong?.close()
        if(windowStrong == null){
            Log.d(TAG, "closeWindow: window is null")
        }else{
            Log.d(TAG, "closeWindow: window is not null")
        }
    }
const val TAG = "__WindowObj"
}