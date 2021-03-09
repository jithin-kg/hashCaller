package com.nibble.hashcaller.view.ui.contacts.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedContactAddress
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedViews
import com.nibble.hashcaller.view.utils.ContactGlobal
import java.util.regex.Pattern

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
    markedViews.clear()
    markedItems.clear()
    markedContactAddress.clear()
    SMSContainerFragment.updateSelectedItemCount(markedItems.size)

}

fun unMarkItem(view:View, threadId:Long, address:String){
    view.visibility = View.INVISIBLE
    markedViews.remove(view)
    markedItems.remove(threadId)
    markedContactAddress.remove(address)
    SMSContainerFragment.updateSelectedItemCount(markedItems.size)

}



/**
 * to check if a string containing only numbers, not alphabets and special numbers
 * return true if string contains only number else false
 * @param stringValue
 * @return boolean
 */
fun isNumericOnlyString(stringValue: String): Boolean {
//    var str = stringValue
//    str = formatPhoneNumber(str)
    val regex = "[0-9]+"
    val pattern = Pattern.compile(regex)
    val m = pattern.matcher(stringValue)
    if(m.matches()){
        return true
    }
    return false

}


/**
 * contacts hashMap with key as phone number and value as
 */

var contactWithMetaDataForSms : HashMap<String, ContactGlobal> = hashMapOf()

object pageOb{
    var page = 0
}

var isSizeEqual = false // to decide whether to show shimmer in smslistrecyclerview

/**
 * function to set staus bar color according to system theme
 * ie dark and light
 * @param activity calling activity
 */

     fun setStatusBarColor(activity:Activity) {
    val window =activity.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
    window.setStatusBarColor(ContextCompat.getColor(activity,R.color.statusBar))
}

/**
 * function to load image into imageviews using glide
 * @param context Context
 * @param view : ImageView
 *
 */
fun loadImage(context: Context, imgView: ImageView, photoUri: String? ) {

    if(photoUri!=null){
        Glide.with(context).load(Uri.parse(photoUri))
            .into(imgView)
    }

}
