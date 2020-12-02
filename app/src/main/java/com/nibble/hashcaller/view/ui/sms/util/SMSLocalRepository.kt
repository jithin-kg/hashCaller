package com.nibble.hashcaller.view.ui.sms.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.CallLog
import android.provider.Telephony
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import java.util.LinkedHashSet
import kotlin.math.log

/**
 * type 2 sent message and type 1 recieved message
 * Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT
 * Message type: drafts.
Constant Value: 3 (0x00000003)

public static final int MESSAGE_TYPE_FAILED
Message type: failed outgoing message.
Constant Value: 5 (0x00000005)

MESSAGE_TYPE_INBOX
public static final int MESSAGE_TYPE_INBOX
Message type: inbox.
Constant Value: 1 (0x00000001)

MESSAGE_TYPE_OUTBOX
public static final int MESSAGE_TYPE_OUTBOX
Message type: outbox.
Constant Value: 4 (0x00000004)

MESSAGE_TYPE_QUEUED
public static final int MESSAGE_TYPE_QUEUED
Message type: queued to send later.
Constant Value: 6 (0x00000006)

MESSAGE_TYPE_SENT
public static final int MESSAGE_TYPE_SENT
Message type: sent messages.
Constant Value: 2 (0x00000002)

PERSON
public static final String PERSON
The ID of the sender of the conversation, if present.

Type: INTEGER (reference to item in content://contacts/people)

Constant Value: "person"

READ
public static final String READ
Has the message been read?
Type: INTEGER (boolean)
Constant Value: "read"

SEEN
Added in API level 19

public static final String SEEN
Has the message been seen by the user? The "seen" flag determines whether we need to show a notification.
Type: INTEGER (boolean)
Constant Value: "seen"
 */
//

class SMSLocalRepository(private val context: Context){

    companion object{
        private val URI: Uri = SMSContract.INBOX_SMS_URI
        private const val TAG = "__SMSLocalRepository"
    }


    fun getUnreadMsgCount(): Int? {
        var cursor:Cursor? = null
        var cnt:Int? = 0
        var address = "44"
        try {
             cursor = context.contentResolver.query(
                URI,
                null,
                "read = 0",
                null,
                null

            )
             cnt = cursor?.count

            Log.d(TAG, "getUnreadMsgCount: count $cnt")

        }catch (e:Exception){

        }finally {
            cursor?.close()
        }

        return cnt
    }

    //gets sms for SMSLiveData to show all sms
    fun fetchSMS(searchText:String?): MutableList<SMS> {
       return fetch(null)
    }

    //this function fetches sms while searching
    fun getSms(searchQuery: String?): MutableList<SMS> {

        return fetch(searchQuery)
    }

     fun update(addressString: String){
        val cValues = ContentValues().apply {
            put("read", 1)

        }
        context.contentResolver.update(URI,cValues, "address='$addressString'",null)

    }
    private fun fetch(searchQuery: String?): MutableList<SMS> {
        val listOfMessages = mutableListOf<SMS>()
        var selectionArgs: Array<String>? = null
        var selection: String? = null

        if (searchQuery != null) {
            selection = SMSContract.SMS_SELECTION_SEARCH
            selectionArgs = arrayOf("%$searchQuery%", "%$searchQuery%")
        }
        var data = ArrayList<SMS>()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls._ID,
            CallLog.Calls.DATE


        )


        val cursor = context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            null,
            selection,
            selectionArgs,
            SMSContract.SORT_DESC
        )

        if(cursor != null && cursor.moveToFirst()){
            do{

                try{
                    //TODO check if phone number exists in contact, if then add the contact information too
                    val objSMS = SMS()
                    objSMS.id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                    val num = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//                    objSMS.address = num
                    objSMS.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                    val msg = cursor.getString(cursor.getColumnIndexOrThrow("body"))

                    var spannableStringBuilder: SpannableStringBuilder?

                    if(searchQuery!=null ){
                        val lowercaseMsg = msg.toLowerCase()
                        val lowerSearchQuery = searchQuery.toLowerCase()

                        if(lowercaseMsg.contains(lowerSearchQuery) && searchQuery.isNotEmpty()){
                            val startPos = lowercaseMsg.indexOf(lowerSearchQuery)
                            val endPos = startPos +lowerSearchQuery.length
                            val yellow =
                                BackgroundColorSpan(Color.YELLOW)
                           spannableStringBuilder =
                                SpannableStringBuilder(msg)
                            spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                            objSMS.msg = spannableStringBuilder
                            objSMS.address = SpannableStringBuilder(num)
                        }else if(num.contains(searchQuery) && searchQuery.isNotEmpty()){
                            val startPos = num.indexOf(searchQuery)
                            val endPos = startPos + searchQuery.length
                            val yellow = BackgroundColorSpan(Color.YELLOW)
                            spannableStringBuilder = SpannableStringBuilder(num)
                            spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                            objSMS.address = spannableStringBuilder
                            objSMS.msg = SpannableStringBuilder(msg)
                        }else{
                            spannableStringBuilder =
                                SpannableStringBuilder(msg)
                            objSMS.msg = spannableStringBuilder
                            objSMS.address = SpannableStringBuilder(num)
                        }
                    }else{
                        spannableStringBuilder =
                            SpannableStringBuilder(msg)
                        objSMS.msg = spannableStringBuilder
                        objSMS.address = SpannableStringBuilder(num)
                    }

                    objSMS.addressString = num


//                    val count =setSMSReadStatus(objSMS, objSMS.addressString!!)
//                    objSMS.unReadSMSCount = count!!


                    objSMS.readState = cursor.getInt(cursor.getColumnIndex("read"))
                    val dateMilli = cursor.getLong(cursor.getColumnIndexOrThrow("date"))





                    objSMS.time = dateMilli


                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        objSMS.folderName = "inbox"
                    } else {
                        objSMS.folderName = "sent"
                    }

                    listOfMessages.add(objSMS)
                }catch (e:Exception){
                    Log.d(TAG, "getMessages: $e")
                }

            }while (cursor.moveToNext())

            data = sortAndSet(listOfMessages)
        }
        setSMSReadStatus(data)
        return data
    }

    /**
     * Function to check whether the current message is opened/readed by the user
     */
    private fun setSMSReadStatus(
        smsList: ArrayList<SMS> ) {

       for (sms in smsList){
           if(sms.readState ==0 )
                setCount(sms)
       }

    }

    private fun setCount(sms: SMS) {
        val addressString = sms.addressString
        var cnt:Int? = 0
        val cursorSMSCount = context.contentResolver.query(
            URI,
            emptyArray<String>(),
            "read = 0 and address='$addressString'",
            null,
            null

        )
        try {
            cnt = cursorSMSCount?.count
            sms.unReadSMSCount = cnt!!
        }catch (e:Exception){
            Log.d(TAG, "setSMSReadStatus: exception $e ")
        }finally {
            cursorSMSCount?.close()
        }
    }


    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }

    fun fetchIndividualSMS(contact: String?): List<SMS> {
        var selectionArgs: Array<String>? = null
        selectionArgs = arrayOf(contact!!)
        var smslist = mutableListOf<SMS>()

        val cursor = context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            null,
            SMSContract.SMS_SELECTION,
            selectionArgs,
            SMSContract.SORT_ASC
        )
        if(cursor != null && cursor.moveToFirst()) {

            do {
                val sms = SMS()


                sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))
                sms.time = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
               sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                Log.d(TAG, "fetchIndividualSMS:person ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("person"))}")
                 Log.d(TAG, "fetchIndividualSMS:date_sent ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("date_sent"))}")
                 Log.d(TAG, "fetchIndividualSMS:protocol ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("protocol"))}")
                 Log.d(TAG, "fetchIndividualSMS:read ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("read"))}")
                 Log.d(TAG, "fetchIndividualSMS:status ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("status"))}")
                 Log.d(TAG, "fetchIndividualSMS:type ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("type"))}")
                 Log.d(TAG, "fetchIndividualSMS:reply_path_present ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("reply_path_present"))}")
                 Log.d(TAG, "fetchIndividualSMS:subject ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("subject"))}")
                 Log.d(TAG, "fetchIndividualSMS:body ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))}")
                 Log.d(TAG, "fetchIndividualSMS:service_center ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("service_center"))}")
                 Log.d(TAG, "fetchIndividualSMS:locked ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("locked"))}")
                 Log.d(TAG, "fetchIndividualSMS:sub_id ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("sub_id"))}")
                 Log.d(TAG, "fetchIndividualSMS:error_code ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("error_code"))}")
                 Log.d(TAG, "fetchIndividualSMS:creator ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("creator"))}")
                 Log.d(TAG, "fetchIndividualSMS:seen ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("seen"))}")
                 Log.d(TAG, "fetchIndividualSMS:seen ${cursor!!.getString(cursor!!.getColumnIndexOrThrow("seen"))}")

                smslist.add(sms)
                try {

                } catch (e: java.lang.Exception) {
                    Log.d(TAG, "fetchIndividualSMS: $e")
                }
            } while (cursor.moveToNext())

        }
        cursor?.close()
//        update(contact)
        return smslist
    }



}