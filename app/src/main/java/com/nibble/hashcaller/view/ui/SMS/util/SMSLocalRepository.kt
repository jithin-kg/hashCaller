package com.nibble.hashcaller.view.ui.SMS.util

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.LinkedHashSet

class SMSLocalRepository(private val context: Context){

    companion object{
        private val URI: Uri = SMSContract.ALL_SMS_URI
        private const val TAG = "__SMSLocalRepository"
    }
    //gets sms for SMSLiveData to show all sms
    fun fetchSMS(searchText:String?): MutableList<SMS> {
       return fetch(null)
    }

    //this function fetches sms while searching
    fun getSms(searchQuery: String?): MutableList<SMS> {
        return fetch(searchQuery)
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
            URI,
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
                    objSMS.address = num
                    objSMS.msg = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                    objSMS.readState = cursor.getString(cursor.getColumnIndex("read"))
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
            cursor.close()
        }
        return listOfMessages
    }


    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }

}