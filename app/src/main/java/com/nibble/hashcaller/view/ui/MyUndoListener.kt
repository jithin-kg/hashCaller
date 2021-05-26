package com.nibble.hashcaller.view.ui

import android.util.Log
import android.view.View

/**
 * class to call back undo clicked in snackbar
 */
class MyUndoListener (private val listener: SnackBarListner) : View.OnClickListener {

    override fun onClick(v: View) {
        listener.onUndoClicked()
    }

    companion object{
        const val TAG = "__MyUndoListener"
    }
    interface SnackBarListner {
        fun onUndoClicked()
    }
}