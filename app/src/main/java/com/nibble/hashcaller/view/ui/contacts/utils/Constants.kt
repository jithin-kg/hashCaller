package com.nibble.hashcaller.view.ui.contacts.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment.Companion.showHideBlockButton
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedContactAddress
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedViews
import com.nibble.hashcaller.view.utils.ContactGlobal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.HashMap

/**
 * Created by Jithin KG on 23,July,2020
 */
const val USER_PREFERENCES_NAME = "data_store_pref"
const val USER_PREFERENCES_BLOCK = "data_store_pref_block_preference"
val SAMPLE_ALIAS = "SOMETHINGNEW"

const val TOKEN_DATASTORE = "token"
const val   REQUEST_CODE_IMG_PICK = 1
const val SPAM_THREASHOLD = 0L
const val DATE_THREASHOLD = 1
const val CONTACT_ID = "contactId"
const val CONTACT_ADDRES = "contact_address"
const val SHAREDPREF_LOGGEDIN = "IsLoggedInSP"
const val PERMISSION_RESULT_CODE = 33
const val PERMISSION_REQUEST_CODE = 23
const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
const val CONTACT_NAME = "contact_name"
const val FROM_SMS_RECIEVER = "from_sms_receiver"
const val INTANT_SMS_BRECIEVER_TIME = "timeofsmssent"
const val SMS_CHAT_ID = "chatId" //intent key for single chat text sms id
const val QUERY_STRING = "queryString" //for sending intent os searchActivity, sms

const val TYPE_MUTE = 1
const val TYPE_DELETE = 2


const val TYPE_SPAM = -1

const val INTENT_TYPE_MAKE_CALL = 0
const val INTENT_TYPE_START_INDIVIDUAL_SMS = 1
const val INTENT_TYPE_MORE_INFO = 2

const val OPERATION_PENDING = 0
const val OPERATION_COMPLETED = 1 // to indicate whether a mark operation in call fragment
             //completed or not

const val ALREADY_EXISTS_IN_DB = 5
const val OPERATION_UNBLOCKED = 3
const val OPERATION_BLOCKED = 4

const val OPERATION_FAILED = 2
var smsDeletingStarted = false
var LAST_SMS_SENT = false

var markingStarted = false // to use in recyclerview long press to mark item for deleting, blocking etc..

var MESSAGE_STRING = ""

suspend fun hashUsingArgon(textToHash: String?): String? = withContext(Dispatchers.Default) {
    val argon2 = HashCaller.getArgon2()

    val result = argon2.hash(  textToHash?.toByteArray(Charset.defaultCharset()),  "samplesdlfhksdlkfjhasdjklfhasdkfljhasdkjflhsadlkfjhasdlkjfhasdlkjfhsdlfjk".toByteArray(
        Charset.defaultCharset()))
    val hash = result.hash
    val hashHex = result.hashHex
    val encoded = result.encoded

    return@withContext hashHex

//32  Mib-> c8069e9734dbe10cad37e72876c7f37d753123d1dba2ba6cddb4386662f233ed

}
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
    showHideBlockButton()

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
    var pageSpam = 0
    var totalSMSCount = 0
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
    window.setStatusBarColor(ContextCompat.getColor(activity,R.color.textColor))
}

/**
 * function to load image into imageviews using glide
 * @param context Context
 * @param view : ImageView
 *
 */
fun loadImage(context: Context, imgView: ImageView, photoUriStr: String?, selectedImageUri: Uri?=null) {

    var imgUri:Uri? = null
    if(selectedImageUri!=null){
      imgUri = selectedImageUri
    }else{
        imgUri = Uri.parse(photoUriStr)
    }
    try {
        if(photoUriStr!=null){
            Glide.with(context).load(imgUri)
                .into(imgView)
        }
    }catch (e:Exception){
        Log.d(TAG, "loadImage: exception $e")
    }
}
private const val TAG = "__Constants"
/**
 * @param informationReceivedDate : date at which the data is inserted in db
 * @param limit : number of day in which a lookup for the current number should perform
 */

fun isCurrentDateAndPrevDateisGreaterThanLimit(
    informationReceivedDate: Date,
    limit: Int
): Boolean {
    val today = Date()
    val miliSeconds: Long = today.getTime() - informationReceivedDate.getTime()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
    val minute = seconds / 60
    val hour = minute / 60
    val days = hour / 24
    if(days > limit)
        return true
    return false

}
