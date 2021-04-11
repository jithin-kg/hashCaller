package com.nibble.hashcaller.view.ui.call.dialer.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.view.ui.call.db.CallLogAndInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit



class CallLogLiveData(
    private val context: Context,
    private val repository: CallContainerRepository?
):
    ContentProviderLiveData<MutableList<CallLogTable>>(
        context,
        URI
    ) {
    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri = CallLog.Calls.CONTENT_URI
        private const val TAG = "__CallLogLiveData"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)
    }
    private suspend fun getCallLog(context: Context): MutableList<CallLogTable> {
         repository!!.getFullCallLogs().apply {
             return this
         }

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

    override suspend fun getContentProviderValue(text: String?): MutableList<CallLogTable> = getCallLog(context)
}