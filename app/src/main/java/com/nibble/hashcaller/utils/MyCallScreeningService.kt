package com.nibble.hashcaller.utils

import android.annotation.SuppressLint
import android.os.Build
import android.telecom.Call
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.utils.callHandlers.base.CallScreeningHelper
import com.nibble.hashcaller.utils.callHandlers.base.extensions.parseCountryCode
import com.nibble.hashcaller.utils.callHandlers.base.extensions.removeTelPrefix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

//https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
//https://zoransasko.medium.com/detecting-and-rejecting-incoming-phone-calls-on-android-9e0cff04ef20
@RequiresApi(Build.VERSION_CODES.N)
class MyCallScreeningService: CallScreeningService() {
    private lateinit var helper: CallScreeningHelper
    /**
     * important to look into CallScreeningService source code to findout how to work with this class
     */

//    rivate val notificationManager = NotificationManagerImpl()

    @SuppressLint("LongLogTag")
    override fun onScreenCall(callDetails: Call.Details) {

         helper = CallScreeningHelper(this)
        Log.d(TAG, "onScreenCall: ")

        val phoneNumber = getPhoneNumber(callDetails)
        var response = CallResponse.Builder()
        response = handlePhoneCall(response, phoneNumber, callDetails)

    }

    @SuppressLint("LongLogTag")
    private fun handlePhoneCall(
        response: CallResponse.Builder,
        phoneNumber: String,
        callDetails: Details
    ): CallResponse.Builder {

        Log.d(TAG, "handlePhoneCall: phone number $phoneNumber")


        GlobalScope.launch {
          val isMuted =  async {  helper.isMutedNumber(phoneNumber) }.await()
            if(isMuted){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    response.setSilenceCall(true)

                }
            }
            respondToCall(callDetails, response.build())
        }
//        helper.isMutedNumber(phoneNumber).collect {
//
//        }

//        if (phoneNumber == "+91949561749432") {
//            response.apply {
//
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
////                    setSilenceCall(true)
////                }
//                setDisallowCall(true) // call gets blocked , dont even see call comming
////                setRejectCall(true)
////                setDisallowCall(true)
////                setSkipCallLog(false)
//                //
//                displayToast(String.format("Rejected call from %s", phoneNumber))
//            }
//        }


//        else {
//            displayToast(String.format("Incoming call from %s", phoneNumber))
//        }
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