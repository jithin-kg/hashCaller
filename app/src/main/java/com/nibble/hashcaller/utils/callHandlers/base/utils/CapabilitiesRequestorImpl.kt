//package com.nibble.hashcaller.utils.callHandlers.base.utils
//
//import android.content.Intent
//import android.os.Build
//import androidx.appcompat.app.AlertDialog
//import com.nibble.hashcaller.R
//import java.lang.ref.WeakReference
//
//
///**
// * Interface that defines which method will be invoked in order to make capabilities
// * requestor implementation flow.
// */
//interface CapabilitiesRequestor {
//    /**
//     * Invokes capabilities request.
//     */
//    fun invokeCapabilitiesRequest()
//
//    /**
//     * Handles [BaseActivity.onActivityResult] method, invoked from [BaseActivity] that starts
//     * capabilities request.
//     */
//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
//}
//
//
///**
// * Class that invokes enabling different capabilities (like phone dialer) in order to listen for
// * phone call data.
// *
// * @author Zoran Sasko
// * @version 1.0.0
// */
//class CapabilitiesRequestorImpl : CapabilitiesRequestor {
//
//    var activityReference: WeakReference<BaseActivity>? = null
//
//    override fun invokeCapabilitiesRequest() {
//        activityReference?.get()?.let {
//            if (!it.hasDialerCapability()) {
//                requestDialerPermission()
//            }
//        }
//    }
//
//    /**
//     * Invokes selecting default dialer, required for reading call info.
//     */
//    private fun requestDialerPermission() {
//        activityReference?.get()?.let {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                it.startCallScreeningPermissionScreen(REQUEST_ID_CALL_SCREENING)
//            } else {
//                it.startSelectDialerScreen(REQUEST_ID_SET_DEFAULT_DIALER)
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_ID_CALL_SCREENING || requestCode == REQUEST_ID_SET_DEFAULT_DIALER) {
//            if (resultCode == android.app.Activity.RESULT_OK) {
//                activityReference?.get()?.let {
//                    it.uiEvent.postValue(PhoneCapabilityEnabled)
//                }
//            } else {
//                displayCallScreeningPermissionDialog {
//                    requestDialerPermission()
//                }
//            }
//        }
//    }
//
//    /**
//     * Displays a dialog asking from user to enable phone capability of the app
//     * @param positiveButtonHandler Handler that's invoked when user clicks on positive button.
//     */
//    private fun displayCallScreeningPermissionDialog(positiveButtonHandler: (() -> Unit)?) {
//        activityReference?.get()?.let {
//            AlertDialog.Builder(it)
//                    .setTitle("hi")
//                    .setMessage("hi")
//                    .setPositiveButton(android.R.string.ok) { _, _ ->
//                        positiveButtonHandler?.invoke()
//                    }
//                    .setNegativeButton(android.R.string.cancel, { dialog, item -> })
//                    .create()
//                    .show()
//        }
//    }
//
//    companion object {
//        const val REQUEST_ID_CALL_SCREENING = 9872
//        const val REQUEST_ID_SET_DEFAULT_DIALER = 1144
//    }
//}