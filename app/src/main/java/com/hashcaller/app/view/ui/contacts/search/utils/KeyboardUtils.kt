package com.hashcaller.app.view.ui.contacts.search.utils

import android.app.Activity
import android.os.IBinder
import android.view.inputmethod.InputMethodManager

class KeyboardUtils{

    companion object{
        /**
         * @param windowToken : view.token
         */
        fun hideKeyboard(activity: Activity, windowToken: IBinder) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(windowToken, 0)
//            imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }
}