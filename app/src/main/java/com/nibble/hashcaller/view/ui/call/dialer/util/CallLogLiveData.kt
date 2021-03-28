package com.nibble.hashcaller.view.ui.call.dialer.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import java.lang.Exception
import java.text.SimpleDateFormat


class CallLogLiveData(private val context: Context):
    ContentProviderLiveData<MutableList<CallLogData>>(context,
    URI
)  {
    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = CallLog.Calls.CONTENT_URI
        private const val TAG = "__CallLogLiveData"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)
    }
    private fun getCallLog(context: Context):MutableList<CallLogData>{
        val listOfCallLogs = mutableListOf<CallLogData>()
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls._ID,
            CallLog.Calls.DATE

        )
        var cursor:Cursor? = null

        try {
          cursor = context.contentResolver.query(
                URI,
                projection,
                null,
                null,
                null
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    val number = cursor.getString(0)
                    val type: String = cursor.getString(1)
                    val duration: String = cursor.getString(2)
                    val name: String? = cursor.getString(3)
                    val id = cursor.getLong(4)
                    var dateInMilliseconds = cursor.getString(5)
                    val fmt =
                        SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")
                    val dateInLong: Long = dateInMilliseconds.toLong()
                    val dateString = fmt.format(dateInLong)

                    val callType:Int = type.toInt()

                    /**
                     *   CallLog.Calls.INCOMING_TYPE:  "INCOMING"; ------->1
                     *   CallLog.Calls.OUTGOING_TYPE:   "OUTGOING";----> 2
                     *   CallLog.Calls.MISSED_TYPE:  "MISSED"; -------->3
                     */
                    dateInMilliseconds += name + id + Math.random().toString();

                    val log = CallLogData(id, number, callType, duration, name, dateString,dateInMilliseconds = dateInMilliseconds)
                    
                    listOfCallLogs.add(log)
                }while (cursor.moveToNext())

            }
        }catch (e:Exception){
            Log.d(TAG, "getCallLog: exception $e")
        }finally {
            cursor?.close()
            isLoading.postValue(false)
        }


        return listOfCallLogs

    }
    override suspend fun getContentProviderValue(text: String?): MutableList<CallLogData> = getCallLog(context)
}