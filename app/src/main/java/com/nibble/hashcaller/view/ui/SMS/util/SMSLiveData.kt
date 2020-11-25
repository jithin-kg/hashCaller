package com.nibble.hashcaller.view.ui.SMS.util

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSLiveData(private val context: Context):
    ContentProviderLiveData<List<SMS>>(context,
        URI
    )  {
    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = SMSContract.ALL_SMS_URI
        private const val TAG = "__MessagesLiveData"
    }
    private fun getMessages(context: Context): MutableList<SMS> {


        val repository = SMSLocalRepository(context)
        return repository.fetchSMS(null)

//        val listOfMessages = mutableListOf<SMS>()
//        var data = ArrayList<SMS>()
//
//        val projection = arrayOf(
//            CallLog.Calls.NUMBER,
//            CallLog.Calls.TYPE,
//            CallLog.Calls.DURATION,
//            CallLog.Calls.CACHED_NAME,
//            CallLog.Calls._ID,
//            CallLog.Calls.DATE
//
//
//        )
//        var selectionArgs: Array<String>? = null
//
//        val cursor = context.contentResolver.query(
//            URI,
//            null,
//            null,
//            null,
//            SMSContract.SORT_DESC
//        )
//        if(cursor != null && cursor.moveToFirst()){
//            do{
//
//                try{
//                    //TODO check if phone number exists in contact, if then add the contact information too
//                    val objSMS = SMS()
//                    objSMS.id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
//                    val num = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//                    objSMS.address = num
//                    objSMS.msg = cursor.getString(cursor.getColumnIndexOrThrow("body"))
//                    objSMS.readState = cursor.getString(cursor.getColumnIndex("read"))
//                    val dateMilli = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
//
//
//
//
//
//                    objSMS.time = dateMilli
//
//
//                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
//                        objSMS.folderName = "inbox"
//                    } else {
//                        objSMS.folderName = "sent"
//                    }
//
//                    listOfMessages.add(objSMS)
//                }catch (e:Exception){
//                    Log.d(TAG, "getMessages: $e")
//                }
//
//            }while (cursor.moveToNext())
//
//             data = sortAndSet(listOfMessages)
//            cursor.close()
//        }

//        return data

    }

    private fun convertEpochToStd(dateMilliString: Long): Int {
        val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")

   val date =  SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date(dateMilliString * 1000))
        Log.d(TAG, "date  $date")
        val hour  = SimpleDateFormat("HH:mm:ss").format(Date(dateMilliString * 1000))
        Log.d(TAG, "hour: $hour")


        return 34

    }

    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }

    override fun getContentProviderValue(searchText:String?) = getMessages(context)
//    fun getSms(){
//
//    }
}