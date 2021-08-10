package com.hashcaller.view.ui.sms.util

import android.view.View

class SetAsDefaultSMSSnackbarListener (private val listener: SnackBarListner) : View.OnClickListener {

    override fun onClick(v: View) {
        listener.onSetAsDefaultSMSHandlerClicked()
    }

    companion object{
        const val TAG = "__MyUndoListener"
    }
    interface SnackBarListner {
        fun onSetAsDefaultSMSHandlerClicked()
    }
}