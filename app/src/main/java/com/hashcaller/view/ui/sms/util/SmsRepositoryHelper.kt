package com.hashcaller.view.ui.sms.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.hashcaller.view.ui.contacts.getAllSMSCursor
import com.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SmsRepositoryHelper(private val context: Context?) {
    @SuppressLint("LongLogTag")
    suspend fun fetchWithRawData(): MutableList<SMS>  = withContext(
        Dispatchers.IO) {
        var cursor:Cursor? = null
        var data = ArrayList<SMS>()
        val listOfMessages = mutableListOf<SMS>()

        var prevAddress = ""
        var prevTime = 0L
//       val r1= GlobalScope.async {
        try {


            var deleteViewAdded = false
            var setOfAddress:MutableSet<String> = mutableSetOf()
            var count = 0
            var map: HashMap<String?, String?> = HashMap()

             cursor = context?.getAllSMSCursor()
            //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
            if (cursor != null && cursor.moveToFirst()) {
                //                    val spammersList = spamListDAO?.getAll()
                do {

                    try {
                        val objSMS = SMS()
                        objSMS.id =
                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadID =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                        var num =
                            cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        objSMS.addresStringNonFormated = num
                        num = num.replace("+", "")
                        //                    objSMS.address = num

                        objSMS.type =
                            cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                        var msg =
                            cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        objSMS.msgString = msg
                        objSMS.body = msg
                        objSMS.addressString = formatPhoneNumber(num)
                        objSMS.nameForDisplay = objSMS.addressString!!
//                        emptySpanPositions(objSMS)

                        objSMS.readState =
                            cursor.getInt(cursor.getColumnIndex("read"))

                        val dateMilli =
                            cursor.getLong(cursor.getColumnIndexOrThrow("date"))
//                        if(prevAddress != objSMS.addressString){
//                            prevAddress = objSMS.addressString!!
//                        }else{
//                            //equal
//                            continue
//                        }
                        objSMS.time = dateMilli
                        setRelativeTime(objSMS, dateMilli)

                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                        } else {
                            objSMS.folderName = "sent"

                        }


                        listOfMessages.add(objSMS)

                    } catch (e: Exception) {
                        Log.d(TAG, "getMessages: exception $e")
                    }

                } while (cursor.moveToNext())
            }


        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetch: exception $e")
        }finally {
            cursor?.close()
        }
//        }

        return@withContext listOfMessages
    }

     fun emptySpanPositions(objSMS: SMS) {
        objSMS.spanStartPos = 0
        objSMS.spanEndPos = 0
        objSMS.spanStartPosNameCp = 0
        objSMS.spanEndPosNameCp = 0
        objSMS.spanStartPosMsgPeek = 0
        objSMS.spanEndPosMsgPeek = 0
    }

    companion object {
        const val TAG = "__SearchHelper"
    }

    fun setRelativeTime(objSMS: SMS, dateMilli: Long) {
        val days = getDaysDifference(dateMilli)
        if(days == 0L ){

//                 val time = String.format("%02d" , c.get(Calendar.HOUR))+":"+
//                     String.format("%02d" , c.get(Calendar.MINUTE))
//                 val ftime = SimpleDateFormat("hh:mm:ss" ).format(time * 1000L)
            setHourAndMinute(objSMS, dateMilli)


        }else if(days == 1L){

            objSMS.relativeTime = "Yesterday"
        }else{
//                 view.tvSMSTime.text = "prev days"

            val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dateMilli))
            objSMS.relativeTime = date
        }
    }


    /**
     * function to return difference between today and passed date in milli secods
     */
     fun getDaysDifference(dateMilli: Long): Long {
        val today = Date()
        val miliSeconds: Long = today.time - dateMilli!!
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
        val minute = seconds / 60
        val hour = minute / 60
        val days = hour / 24
        return days
    }
     fun setHourAndMinute(objSMS: SMS, dateMilli: Long): String {
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
}