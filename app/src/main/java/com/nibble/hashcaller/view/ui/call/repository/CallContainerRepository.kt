package com.nibble.hashcaller.view.ui.call.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.call.CallFragment.Companion.pageCall
import com.nibble.hashcaller.view.ui.call.db.*
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_FROM_CONTENT_PROVIDER
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_FROM_DB
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CallContainerRepository(
    val context: Context,
    val dao: CallersInfoFromServerDAO,
    val mutedCallersDAO: IMutedCallersDAO?,
    private val callLogDAO: ICallLogDAO?
) {

    private var retrofitService:ICallService? = null

    /**
     * @return all sms senders numbers list in the localDB which contains
     * ____________________________________________________
     * contact_address | spammeReportCount | informationRecivedDate | name | type (business or general user) |
     * -----------------------------------------------------
     *
     * this is the table schema
     */
    suspend fun geSmsSendersStoredInLocalDB(): List<CallersInfoFromServer> {
        val list =  dao.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownCallersInfoResponse> {
        retrofitService = RetrofitClient.createaService(ICallService::class.java)
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val tokenManager = TokenManager(sp)
        val token = tokenManager.getToken()

        val response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        Log.d(TAG, "uploadNumbersToGetInfo: response is ${response}")
        return response
    }

    suspend fun getCallerInfoForAddressFromDB(number: String): CallersInfoFromServer? {
        val numWithoutSpecialChars = formatPhoneNumber(number)
//        var numberForQuery =numWithoutSpecialChars
//        if(isNumericOnlyString(numWithoutSpecialChars)){
//            numberForQuery = formatPhoneNumber(numWithoutSpecialChars)
//        }
        var result: CallersInfoFromServer? = null
        GlobalScope.launch {
            result= async { dao.find(numWithoutSpecialChars) }.await()
        }.join()

        return result

    }

    /**
     * function to delete call logs in db by id, ie marked items
     */
    suspend fun deleteCallLogsFromDBByid(id: Long) {
//            callLogDAO?.delete(id)
            callLogDAO?.markAsDeleted(id, true)
        delay(400L)
    }

    /**
     * delete call logs in content provider
     */
    @SuppressLint("LongLogTag")
     fun deleteLog(id: Long) {

//        val queryString = "NUMBER=$number"
//        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, queryString, null);


//        smsDeletingStarted = true
//        var numRowsDeleted = 0

        try {

                Log.d(TAG, "deleteSmsThread: threadid $id")
                var uri = CallLog.Calls.CONTENT_URI
                val selection = "${CallLog.Calls._ID} = ?"
//                callLogDAO?.delete(id)
                val selectionArgs = arrayOf(id.toString())
                    context.contentResolver.delete(uri, selection, selectionArgs)

        }catch (e: Exception) {
            Log.d(TAG, "deleteSmsThread: exception $e")
        }
        finally {
            clearDeleteditems()
        }



    }

    /**
     * function to add contact address to mutedCallers
     */
    suspend fun muteContactAddress(address: String): String {
        mutedCallersDAO!!.insert(listOf(MutedCallers(address))).apply {
            return address
        }

    }

    @SuppressLint("LongLogTag")
    suspend fun unmuteByAddress(contactAdders: String): String {
        Log.d(TAG, "unmuteByAddress:${contactAdders}")
        mutedCallersDAO!!.delete(contactAdders).apply {
            return contactAdders
        }
    }

    /**
     * Function to check whether a number is muted or not,
     * if contactaddress contains in db then it is  muted
     */
    suspend fun isMmuted(address: String): Boolean {
        var isMutedNum = false
        mutedCallersDAO!!.find(address).apply {
            if(this!=null){
                isMutedNum = true
            }
            return isMutedNum
        }
    }

    suspend fun markCallerAsSpamer(formatPhoneNumber: String, spammerType: Int, s: String, s1: String) {
        dao.find(formatPhoneNumber).apply {
            if(this !=null){
                //number exist in db
                dao.update(this.spamReportCount+1, this.contactAddress, true)
            }else{

                val callerInfoTobeSavedInDatabase = CallersInfoFromServer(null,
                    formatPhoneNumber(formatPhoneNumber), spammerType, "",
                    Date(), 1)
                dao.insert(listOf(callerInfoTobeSavedInDatabase))
            }
        }
    }

    @SuppressLint("LongLogTag")
    suspend fun getSMSByPage(): MutableList<CallLogData> {
        val listOfCallLogs = mutableListOf<CallLogData>()
        val item1 = CallLogData()
        val item2 = CallLogData()
        val item3 = CallLogData()
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
                "${CallLog.Calls.DATE} DESC LIMIT 10 OFFSET $pageCall"
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
                    var isMarked = false
                    if(markedIds.contains(id)){
                        isMarked = true
                    }
                    val log = CallLogData(id, number, callType, duration, name,
                        dateString,dateInMilliseconds = dateInMilliseconds.toString(), isMarked = isMarked)
//                    val log = CallLogData()
//                        log.id = id
//                        log.number = number
//                        log.type =callType
//                        log.duration = duration
//                        log.name = name
//                        log.date = dateString
//                        log.dateInMilliseconds = dateInMilliseconds.toString()


                            setRelativeTime(dateInMilliseconds, log)

                    GlobalScope.launch {
//                        async { setInfoFromServer(log) }.await()
                    }.join()
                    if(!deletedIds.contains(id)) {
                        listOfCallLogs.add(log)
                    }
                }while (cursor.moveToNext())

            }
        }catch (e: java.lang.Exception){
            Log.d(TAG, "getSMSByPage: exception $e")
        }finally {
            cursor?.close()

        }
        listOfCallLogs.add(item1)
        listOfCallLogs.add(item2)
        listOfCallLogs.add(item3)
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

    fun getCallLogLiveDAtaFromDB(): LiveData<List<CallersInfoFromServer>> {
        return dao.getAllLiveData()
    }

    @SuppressLint("LongLogTag")
    suspend fun getFullCallLogs(): MutableList<CallLogTable> {

        return getRawCallLogs()
    }

    @SuppressLint("LongLogTag")
    private fun getRawCallLogs(): MutableList<CallLogTable> {
        val listOfCallLogs = mutableListOf<CallLogTable>()
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
                CallLogLiveData.URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{
                    var number = cursor.getString(0)
                    val type: Int = cursor.getInt(1)
                    val duration: String = cursor.getString(2)
                    val name:String? = cursor.getString(3)
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
                    var isMarked = false
                    if(markedIds.contains(id)){
                        isMarked = true
                    }
                    val log = CallLogTable(id, name,
                        formatPhoneNumber(number), type, duration, dateInMilliseconds = dateInMilliseconds)
//                  val callerInfo = CallersInfoFromServer(null, informationReceivedDate =Date())
//                    val logAndServerInfo = CallLogAndInfoFromServer(log, callerInfo )

//                    val log = CallLogData()
//                    log.id = id
//                    log.number = number
//                    log.type =callType
//                    log.duration = duration
//                    log.name = name
//                    log.date = dateString
//                    log.dateInMilliseconds = dateInMilliseconds.toString()

//                    setRelativeTime(dateInMilliseconds, log)

//                    GlobalScope.launch {
//                        async { setInfoFromServer(log) }.await()
//                    }.join()

//                    if(!deletedIds.contains(id)) {
                    listOfCallLogs.add(log)
//                    }
                }while (cursor.moveToNext())

            }
        }catch (e: java.lang.Exception){

            Log.d(TAG, "getFullCallLogs: exception $e")
        }finally {
            cursor?.close()
        }

        return listOfCallLogs
    }

    private suspend fun setInfoFromServer(log: CallLogTable) {
        dao.find(formatPhoneNumber(log.number)).apply {
            if(this !=null){
                if(log.name.isNullOrEmpty()){
                    log.name = this.title
                    log.callerInfoFoundFrom = SENDER_INFO_FROM_DB
                }else{
                    //if there is name already in the log then it would be got from content provider
                    log.callerInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                }
//                log.spamReportCount = this.spamReportCount
            }
        }
    }

    @SuppressLint("LongLogTag")
    suspend fun fetchFirst10(): MutableList<CallLogData> {
        val listOfCallLogs = mutableListOf<CallLogData>()
        val item1 = CallLogData()
        val item2 = CallLogData()
        val item3 = CallLogData()
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
                CallLogLiveData.URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC LIMIT 10"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    var number = cursor.getString(0)
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
                    var isMarked = false
                    if(markedIds.contains(id)){
                        isMarked = true
                    }
                    val log = CallLogData(id, number, callType, duration, name,
                        dateString,dateInMilliseconds = dateInMilliseconds.toString(), isMarked = isMarked)
//                    val log = CallLogData()
//                    log.id = id
//                    log.number = number
//                    log.type =callType
//                    log.duration = duration
//                    log.name = name
//                    log.date = dateString
//                    log.dateInMilliseconds = dateInMilliseconds.toString()
                    setRelativeTime(dateInMilliseconds, log)

                    GlobalScope.launch {
//                        async { setInfoFromServer(log) }.await()
                    }.join()
                    if(!deletedIds.contains(id)){
                        listOfCallLogs.add(log)

                    }

                }while (cursor.moveToNext())

            }
        }catch (e: java.lang.Exception){
            Log.d(TAG, "fetchFirst10: exception $e")
        }finally {
            cursor?.close()
        }
            listOfCallLogs.add(item1)
            listOfCallLogs.add(item2)
            listOfCallLogs.add(item3)
        return listOfCallLogs
    }

    suspend fun clearCallersInfoFromServer() {
        dao.deleteAll()
    }

    suspend fun insertIntoCallLogDb(logsFromContentProvider: MutableList<CallLogTable>) {

        callLogDAO?.insert(logsFromContentProvider)

    }

    fun getAllCallLogLivedata(): LiveData<List<CallLogAndInfoFromServer>>? {
        return callLogDAO?.getAllLiveData()
    }
    suspend fun getAllCallLog(): MutableList<CallLogAndInfoFromServer>? {
        return callLogDAO?.getAllCallLog()
    }

    /**
     * delete call logs from call_logs table that are not in content provider
     */
    suspend fun deleteCallLogs(logsFromContentProvider: MutableList<CallLogTable>) {

        var idsFromContentPovider : MutableList<Long> = mutableListOf()
        idsFromContentPovider.addAll(logsFromContentProvider.map { it.id})

        var idsFromCallLogTable : MutableList<Long> = mutableListOf()

        callLogDAO?.getAll().apply {
            if(this!=null){
                idsFromCallLogTable.addAll(this.map {it.id})
                val idsToBeRemoved = idsFromCallLogTable - idsFromContentPovider
                for(id in idsToBeRemoved){
                    callLogDAO?.delete(id)

                }
            }

        }

    }

    suspend fun updateCallLogWithServerInfo(list: List<CallersInfoFromServer>) {
        var inforfoundFrom = SENDER_INFO_SEARCHING
        for (item in list){
//            callLogDAO?.find(item.contactAddress).apply {

               var name:String? =  getNameForAddressFromContentProvider(item.contactAddress)
                if(name!=null){
                    inforfoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                }else{
                    name = item.title
                    inforfoundFrom = SENDER_INFO_FROM_DB
                }
                callLogDAO?.update(item.contactAddress, name, inforfoundFrom)
//            }
        }
    }


     fun getNameForAddressFromContentProvider(contactAddress: String): String? {
        var name:String? = null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactAddress));

        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )
        if(cursor2!=null && cursor2.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
            name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
        }
        return name
    }




    companion object{
        const val TAG = "__CallContainerRepository"
        var markedIds:MutableSet<Long> = mutableSetOf()
        var deletedIds:MutableSet<Long> = mutableSetOf() // to keep track of item that are deleted from livedata

        fun addAllMarkedItemToDeletedIds(markedIds: MutableSet<Long>) {
            deletedIds.addAll(markedIds)
        }
        fun clearMarkedItems(){
            markedIds.clear()
        }

        /**
         * This should be only called after all item  are complete deleted from repository
         */
        fun clearDeleteditems(){
            deletedIds.clear()

        }
    }

}