package com.nibble.hashcaller.view.ui.call.dialer.util

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import java.text.SimpleDateFormat


class CallLogLiveData(private val context: Context):
    ContentProviderLiveData<List<CallLogData>>(context,
    URI
)  {
    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = CallLog.Calls.CONTENT_URI
        private const val TAG = "__CallLogLiveData"
    }
    private fun getCallLog(context: Context):List<CallLogData>{
        val listOfCallLogs = mutableListOf<CallLogData>()

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
                val id: String = cursor.getString(4)
                val dateInMilliseconds = cursor.getString(5)
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


                val log = CallLogData(id, number, callType, duration, name, dateString)
                listOfCallLogs.add(log)
            }while (cursor.moveToNext())
            cursor.close()
        }

        return listOfCallLogs

    }
    override suspend fun getContentProviderValue(text: String?): List<CallLogData> = getCallLog(context)
}