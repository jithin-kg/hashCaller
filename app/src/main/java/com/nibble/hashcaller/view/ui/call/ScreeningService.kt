package com.nibble.hashcaller.view.ui.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)

class ScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        //code here
        Log.d(TAG, "onScreenCall: $details")
        Log.d(TAG, "onScreenCall: ${details.handle}")
        Log.d(TAG, "onScreenCall: ${details.handle.schemeSpecificPart}")
    }
companion object{
    const val TAG = "__ScreeningService"
}
}