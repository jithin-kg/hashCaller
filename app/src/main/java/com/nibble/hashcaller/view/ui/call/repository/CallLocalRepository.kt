package com.nibble.hashcaller.view.ui.call.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class CallLocalRepository(
    private val context: Context,
    private val countryISO: String,
    private val libCountryHelper: LibPhoneCodeHelper
    ) {
    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = CallLog.Calls.CONTENT_URI

        private const val TAG = "__CallLocalRepository"
    }
     suspend fun getCallLog():List<CallLogData> = withContext(Dispatchers.IO){
        val hashSetOfNumber : HashSet<String> = HashSet()
        val listOfCallLogs = mutableListOf<CallLogData>()
        val projection = arrayOf(
            CallLog.Calls.NUMBER  ,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls._ID,
            CallLog.Calls.DATE

        )
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC "
            )


            if(cursor != null && cursor.moveToFirst()){
                do{

                    val number = cursor.getString(0)
                    var formatedNum = formatPhoneNumber(number)
                    formatedNum = libCountryHelper.getES164Formatednumber(formatedNum, countryISO)

                    if(!hashSetOfNumber.contains(formatedNum)){
                        hashSetOfNumber.add(formatedNum)
                    }else{
                        //if contains in hashet
                        continue
                    }
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
                    /**
                     *   CallLog.Calls.INCOMING_TYPE:  "INCOMING"; ------->1
                     *   CallLog.Calls.OUTGOING_TYPE:   "OUTGOING";----> 2
                     *   CallLog.Calls.MISSED_TYPE:  "MISSED"; -------->3
                     */
                    dateInMilliseconds += name + id + Math.random().toString();

                    val log = CallLogData(id, number = formatedNum, callType, duration, name, dateString,dateInMilliseconds = dateInMilliseconds)
                    listOfCallLogs.add(log)
                }while (cursor.moveToNext())

            }
        }catch (e: Exception){
            Log.d(TAG, "getCallLog: exception $e")
        }finally {
            cursor?.close()
        }


         return@withContext listOfCallLogs

    }
}