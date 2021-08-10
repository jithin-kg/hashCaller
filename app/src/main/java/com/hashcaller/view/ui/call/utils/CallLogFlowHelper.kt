package com.hashcaller.view.ui.call.utils

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log

import com.hashcaller.view.ui.call.dialer.util.CallLogData
import com.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.hashcaller.view.ui.call.setRelativeTime
import java.text.SimpleDateFormat

/**
 * helper class to get call logs as a stream/flow
 */

object CallLogFlowHelper {

    fun fetchCallLogFlow(context:Context):MutableList<CallLogData> {

        val listOfCallLogs:MutableList<CallLogData> = mutableListOf()
        val callLog1 = CallLogData()
        val callLog2 = CallLogData()
        val callLog3= CallLogData()
        val callLog4 = CallLogData()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls._ID,
            CallLog.Calls.DATE

        )
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                CallLogLiveData.URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT 10"
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

                   // val log = CallLogData(id, number, callType, duration, name, dateString,dateInMilliseconds = dateInMilliseconds.toString())
                    val log = CallLogData()
                    log.id = id
                    log.number = number
                    log.type =callType
                    log.duration = duration
                    log.name = name
                    log.date = dateString
                    log.dateInMilliseconds = dateInMilliseconds.toString()
                    setCallHashMap(log)
                   setRelativeTime(dateInMilliseconds, log)
                    listOfCallLogs.add(log)

                }while (cursor.moveToNext())

            }
        }catch (e: Exception){
            Log.d(TAG, "getCallLog: exception $e")
        }finally {
//            emit(listOfCallLogs)
            listOfCallLogs.add(callLog1)
            listOfCallLogs.add(callLog2)
            listOfCallLogs.add(callLog3)

            return listOfCallLogs
            cursor?.close()

        }

    }

    fun setCallHashMap(callLogData: CallLogData) {
        if(callLogData.id !=null){
            Log.d(TAG, "setSMSHashMap: ")

//            val mr = CallFragment.mapofIdsAndCallLogs[callLogData.id]
//            if(mr==null){
//                CallFragment.mapofIdsAndCallLogs[callLogData.id] = callLogData
//            }

//            else{
//                val timFromMap = mr.time!!.toLong()
//                val timeFromCProvider = callLogData.time!!.toLong()
//                if( timFromMap < timeFromCProvider){
//                    //new message is objsms.time
//                    Log.d(SMSHelperFlow.TAG +"setSMSHashMaptS", " lesser map: $timFromMap cp: $timeFromCProvider")
//                    SMSContainerFragment.mapofAddressAndSMS.put(callLogData.addressString!!, callLogData)
//                }else{
//                    Log.d(SMSHelperFlow.TAG, "setSMSHashMap: greater")
//                }
//            }
        }

    }
//companion object{
    const val TAG = "__CallLogFlowHelper"
//}
}