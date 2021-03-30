package com.nibble.hashcaller.view.ui.call.dialer.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import kotlinx.coroutines.delay
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit



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
                "${CallLog.Calls.DATE} DESC"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    val number = cursor.getString(0)
                    val type: String = cursor.getString(1)
                    val duration: String = cursor.getString(2)
                    val name: String? = cursor.getString(3)
                    val id = cursor.getLong(4)
                    var dateInMilliseconds = cursor.getLong(5)
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
//                    dateInMilliseconds += name + id + Math.random().toString();
                    val log = CallLogData(id, number, callType, duration, name, dateString,dateInMilliseconds = dateInMilliseconds.toString())
                  setRelativeTime(dateInMilliseconds, log)

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

    private fun setRelativeTime(
        dateMilli: Long,
        log: CallLogData
    ) {
        val days = getDaysDifference(dateMilli)
        if(days == 0L ){

//                 val time = String.format("%02d" , c.get(Calendar.HOUR))+":"+
//                     String.format("%02d" , c.get(Calendar.MINUTE))
//                 val ftime = SimpleDateFormat("hh:mm:ss" ).format(time * 1000L)
            setHourAndMinute(log, dateMilli)


        }else if(days == 1L){

            log.relativeTime = "Yesterday"
        }else{
//                 view.tvSMSTime.text = "prev days"

            val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dateMilli))
            log.relativeTime = date
        }
    }

    private fun setHourAndMinute(objSMS: CallLogData, dateMilli: Long): String {
        val cc =  Calendar.getInstance()
        cc.timeInMillis = dateMilli
        val formatter: DateFormat = SimpleDateFormat("hh:mm")
        val formattedIn24Hr: DateFormat = SimpleDateFormat("HH")



        objSMS.relativeTime =  formatter.format(cc.time)
        val time24HrFormat = formattedIn24Hr.format(cc.time)
        if(time24HrFormat.toInt()>12){
            objSMS.relativeTime = objSMS.relativeTime + " pm"
        }else{
            objSMS.relativeTime = objSMS.relativeTime + " am"

        }
        return objSMS.relativeTime
    }

    /**
     * function to return difference between today and passed date in milli secods
     */
    private fun getDaysDifference(dateMilli: Long): Long {
        val today = Date()
        val miliSeconds: Long = today.time - dateMilli!!
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
        val minute = seconds / 60
        val hour = minute / 60
        val days = hour / 24
        return days
    }

    override suspend fun getContentProviderValue(text: String?): MutableList<CallLogData> = getCallLog(context)
}