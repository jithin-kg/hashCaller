package com.nibble.hashcaller.utils

import android.annotation.SuppressLint
import android.os.Build
import android.telecom.Call
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.utils.callHandlers.base.extensions.parseCountryCode
import com.nibble.hashcaller.utils.callHandlers.base.extensions.removeTelPrefix
import kotlin.math.log
//https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
//https://zoransasko.medium.com/detecting-and-rejecting-incoming-phone-calls-on-android-9e0cff04ef20
@RequiresApi(Build.VERSION_CODES.N)
class MyCallScreeningService: CallScreeningService() {
//    rivate val notificationManager = NotificationManagerImpl()

    @SuppressLint("LongLogTag")
    override fun onScreenCall(callDetails: Call.Details) {

        Log.d(TAG, "onScreenCall: ")
        val phoneNumber = getPhoneNumber(callDetails)
        var response = CallResponse.Builder()
        response = handlePhoneCall(response, phoneNumber)

        respondToCall(callDetails, response.build())
    }

    @SuppressLint("LongLogTag")
    private fun handlePhoneCall(
        response: CallResponse.Builder,
        phoneNumber: String
    ): CallResponse.Builder {
        Log.d(TAG, "handlePhoneCall: phone number $phoneNumber")

        if (phoneNumber == "+919495617494332") {
            response.apply {
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setSilenceCall(true)
                }
//                setRejectCall(true)
//                setDisallowCall(true)
//                setSkipCallLog(false)
                //
                displayToast(String.format("Rejected call from %s", phoneNumber))
            }
        } else {
            displayToast(String.format("Incoming call from %s", phoneNumber))
        }
        return response
    }

    private fun getPhoneNumber(callDetails: Call.Details): String {

        return callDetails.handle.toString().removeTelPrefix().parseCountryCode()

    }

    private fun displayToast(message: String) {
//        notificationManager.showToastNotification(applicationContext, message)
//        EventBus.getDefault().post(MessageEvent(message))
    }

    companion object {
        const val TAG = "__MyCallScreeningService"
    }
}