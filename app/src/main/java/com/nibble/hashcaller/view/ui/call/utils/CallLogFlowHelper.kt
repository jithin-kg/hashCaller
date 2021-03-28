package com.nibble.hashcaller.view.ui.call.utils

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import com.nibble.hashcaller.view.ui.call.CallFragment
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat

/**
 * helper class to get call logs as a stream/flow
 */



object CallLogFlowHelper {

    fun fetchCallLogFlow(context:Context): Flow<CallLogData> = flow {

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
                    setCallHashMap(log)
                    emit(log)
                }while (cursor.moveToNext())

            }
        }catch (e: Exception){
            Log.d(TAG, "getCallLog: exception $e")
        }finally {
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