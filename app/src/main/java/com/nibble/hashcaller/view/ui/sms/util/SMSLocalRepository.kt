package com.nibble.hashcaller.view.ui.sms.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.CallLog
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import java.util.LinkedHashSet

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
            URI,
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