package com.nibble.hashcaller.view.ui.contacts.utils

import android.view.View
import android.widget.ImageView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler

/**
 * Created by Jithin KG on 23,July,2020
 */

const val CONTACT_ID = "contactId"
const val CONTACT_ADDRES = "contact_address"
const val SHAREDPREF_LOGGEDIN = "IsLoggedInSP"
const val PERMISSION_RESULT_CODE = 33
const val PERMISSION_REQUEST_CODE = 23
const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
const val CONTACT_NAME = "contact_name"
const val FROM_SMS_RECIEVER = "from_sms_receiver"
const val INTANT_SMS_BRECIEVER_TIME = "timeofsmssent"

var LAST_SMS_SENT = false

var markingStarted = false // to use in recyclerview long press to mark item for deleting, blocking etc..

/**
 * unmark all recylcelerview list item
 */
fun unMarkItems(){
    for(view in MarkedItemsHandler.markedViews){
        view.findViewById<ImageView>(R.id.smsMarked).visibility = View.INVISIBLE
    }
    MarkedItemsHandler.markedViews.clear()
}
