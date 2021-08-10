package com.hashcaller.utils.callscreening

import android.util.Log
import com.hashcaller.view.ui.call.floating.Window
import java.lang.ref.WeakReference

object WindowObj {
    private var window: WeakReference<Window>? = null

    fun setWindow(obj: Window){
        window = WeakReference(obj)
    }
    fun getWindowObj(): Window? {
        return window?.get()
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
    fun clearReference(){
        window = null
    }
const val TAG = "__WindowObj"
}