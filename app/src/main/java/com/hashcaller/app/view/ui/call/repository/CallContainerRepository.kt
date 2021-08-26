package com.hashcaller.app.view.ui.call.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.hashcaller.app.local.db.blocklist.mutedCallers.MutedCallers
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.CallFragment.Companion.pageCall
import com.hashcaller.app.view.ui.call.db.*
import com.hashcaller.app.view.ui.call.dialer.util.CallLogData
import com.hashcaller.app.view.ui.call.dialer.util.CallLogLiveData
import com.hashcaller.app.view.ui.call.utils.UnknownCallersInfoResponse
import com.hashcaller.app.view.ui.contacts.getAvailableSIMCardLabels
import com.hashcaller.app.view.ui.contacts.getSimIndexForSubscriptionId
import com.hashcaller.app.view.ui.contacts.utils.TYPE_SPAM
import com.hashcaller.app.view.ui.hashworker.HashWorker
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.ui.sms.db.NameAndThumbnail
import com.hashcaller.app.view.ui.sms.individual.util.getRandomColor
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import com.hashcaller.app.work.removeAllNonNumbericChars
import kotlinx.coroutines.*
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class CallContainerRepository(
    val context: Context,
    val callerInfoFromServerDAO: CallersInfoFromServerDAO,
    val mutedCallersDAO: IMutedCallersDAO?,
    private val callLogDAO: ICallLogDAO?,
    private val dataStoreRepository: DataStoreRepository,
    private val tokenHelper: TokenHelper?,
    private val smsThreadsDAO: ISMSThreadsDAO?,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryISO: String

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
        val list =  callerInfoFromServerDAO.getAll()
        return list
    }

    @SuppressLint("LongLogTag")
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownCallersInfoResponse>?  = withContext(Dispatchers.IO){
        retrofitService = RetrofitClient.createaService(ICallService::class.java)
      val token = tokenHelper?.getToken()

       var response:Response<UnknownCallersInfoResponse>? = null
        token?.let {
             response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        }

        return@withContext response
    }

    suspend fun getCallerInfoForAddressFromDB(number: String): CallersInfoFromServer?   = withContext(Dispatchers.IO){
        val numWithoutSpecialChars = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(number), countryISO)
//        var numberForQuery =numWithoutSpecialChars
//        if(isNumericOnlyString(numWithoutSpecialChars)){
//            numberForQuery = formatPhoneNumber(numWithoutSpecialChars)
//        }
        var result: CallersInfoFromServer? = null
        CoroutineScope(Dispatchers.IO).launch {

            result= async { callerInfoFromServerDAO.find(numWithoutSpecialChars) }.await()


        }.join()

        return@withContext result

    }

    /**
     * function to delete call logs in db by id, ie marked items
     */
    suspend fun deleteCallLogsFromDBByid(address: String) = withContext(Dispatchers.IO) {
//            callLogDAO?.delete(id)
            callLogDAO?.markAsDeleted(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(address), countryISO), true)
        delay(400L)
    }

    /**
     * delete call logs in content provider
     * @param nonFormatedNum number received from calllog table as it is
     */
    @SuppressLint("LongLogTag")
    suspend fun deleteLog(nonFormatedNum: String) = withContext(Dispatchers.IO) {

//        val queryString = "NUMBER=$number"
//        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, queryString, null);


//        smsDeletingStarted = true
//        var numRowsDeleted = 0

        try {
                var uri = CallLog.Calls.CONTENT_URI
                val selection = "${CallLog.Calls.NUMBER} = ?"
//                callLogDAO?.delete(id)
                val selectionArgs = arrayOf(nonFormatedNum)
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
    suspend fun muteContactAddress(address: String): String = withContext(Dispatchers.IO) {
        mutedCallersDAO!!.insert(listOf(MutedCallers(address)))
        return@withContext address


    }

    @SuppressLint("LongLogTag")
    suspend fun unmuteByAddress(contactAdders: String): String = withContext(Dispatchers.IO){
        mutedCallersDAO!!.delete(contactAdders)
        return@withContext contactAdders
    }

    /**
     * Function to check whether a number is muted or not,
     * if contactaddress contains in db then it is  muted
     */
    suspend fun isMmuted(address: String): Boolean = withContext(Dispatchers.IO){
        var isMutedNum = false
        val res = mutedCallersDAO!!.find(address)
            if(res!=null){
                isMutedNum = true
            }
        return@withContext isMutedNum

    }

    suspend fun markCallerAsSpamer(formatPhoneNumber: String, spammerType: Int, s: String, s1: String) {
//        callerInfoFromServerDAO.find(formatPhoneNumber).apply {
//            if(this !=null){
//                //number exist in db
//                callerInfoFromServerDAO.update(this.spamReportCount+1, this.contactAddress, true)
//            }else{
//
//                val callerInfoTobeSavedInDatabase = CallersInfoFromServer(
//                    contactAddress= formatPhoneNumber(formatPhoneNumber),
//                    spammerType= spammerType,
//                    firstName="",
//                    informationReceivedDate =Date(),
//                    spamReportCount = 1L)
//                callerInfoFromServerDAO.insert(listOf(callerInfoTobeSavedInDatabase))
//            }
//        }
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
                        dateString,dateInMilliseconds = dateInMilliseconds.toString(), isMarked = isMarked
                    )
//                    val log = CallLogData()
//                        log.id = id
//                        log.number = number
//                        log.type =callType
//                        log.duration = duration
//                        log.name = name
//                        log.date = dateString
//                        log.dateInMilliseconds = dateInMilliseconds.toString()


                            setRelativeTime(dateInMilliseconds, log)

                    CoroutineScope(Dispatchers.IO).launch {
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

        return callerInfoFromServerDAO.getAllLiveData()
    }



    @SuppressLint("LongLogTag")
    suspend fun getRawCallLogs(): MutableList<CallLogTable>  = withContext(Dispatchers.IO){
        val listOfCallLogs = mutableListOf<CallLogTable>()
        val setOfAddres:HashSet<String> = hashSetOf()
        var simIds = mutableListOf<String>()
        simIds.addAll(context.getSimIndexForSubscriptionId())

        val projection = arrayOf(
            CallLog.Calls.NUMBER,  //0
            CallLog.Calls.TYPE,    //1
            CallLog.Calls.DURATION,  //2
            CallLog.Calls.CACHED_NAME, //3
            CallLog.Calls._ID,         //4
            CallLog.Calls.DATE,        //5
            "subscription_id"
        )
        var cursor:Cursor? = null
        val numberToSimIDMap = HashMap<String, Int>()
        context.getAvailableSIMCardLabels().forEach {
            numberToSimIDMap[it.phoneNumber] = it.id
        }
        try {

            cursor = context.contentResolver.query(
                CallLogLiveData.URI,
                projection,
                null,
                null,
                "${CallLog.Calls._ID} DESC"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{
                    var i = 0
//                    while(i<cursor.columnCount){
//                        Log.d(TAG+"colum", " ${cursor.getColumnName(i)} :  ${cursor.getString(i)}")
//
//                        i++
//                    }
                    var number = cursor.getString(0)
                    val formatedNum = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(number), countryISO)
                    if(!setOfAddres.contains(formatedNum)){
                        setOfAddres.add(formatedNum)
                    }else{
                        continue
                    }
                    val type: Int = cursor.getInt(1)
                    val duration: String = cursor.getString(2)
                    val name:String? = cursor.getString(3)
                    val id = cursor.getLong(4)
                    var dateInMilliseconds = cursor.getLong(5)
                    var subId = cursor.getString(6)
                    var simID =  simIds.indexOf(removeAllNonNumbericChars(subId))

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

//                    dateInMilliseconds += name + id + Math.random().toString();
                    var isMarked = false
                    if(markedIds.contains(id)){
                        isMarked = true
                    }
                    val color = getRandomColor()
                    val log = CallLogTable(id = id, name = name,
                        number = number, type = type, duration = duration,
                        dateInMilliseconds = dateInMilliseconds,
                        simId = simID, color =color, numberFormated = formatedNum)
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

            Log.d(TAG, "getRawCallLogs: exception $e")
        }finally {
            cursor?.close()
        }

        return@withContext listOfCallLogs
    }

    private suspend fun setInfoFromServer(log: CallLogTable) {
//        callerInfoFromServerDAO.find(formatPhoneNumber(log.number)).apply {
//            if(this !=null){
//                if(log.name.isNullOrEmpty()){
//                    log.name = this.firstName
////                    log.callerInfoFoundFrom = SENDER_INFO_FROM_DB
//                }else{
//                    //if there is name already in the log then it would be got from content provider
////                    log.callerInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
//                }
////                log.spamReportCount = this.spamReportCount
//            }
//        }
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

                    CoroutineScope(Dispatchers.IO).launch {
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
        callerInfoFromServerDAO.deleteAll()
        callLogDAO?.deleteAll()

    }

    suspend fun insertIntoCallLogDb(logsFromContentProvider: MutableList<CallLogTable>) = withContext(Dispatchers.IO) {
        callLogDAO?.insert(logsFromContentProvider)

    }

    fun getAllCallLogLivedata(): LiveData<MutableList<CallLogTable>>?  {
        return callLogDAO?.getAllLiveData()
    }
//    suspend fun getAllCallLog(): MutableList<CallLogAndInfoFromServer>? {
////        return callLogDAO?.getAllCallLog()
//    }

    /**
     * delete call logs from call_logs table that are not in content provider
     */
    suspend fun deleteCallLogs(logsFromContentProvider: MutableList<CallLogTable>) = withContext(Dispatchers.IO) {

        var idsFromContentPovider : MutableList<String> = mutableListOf()
        idsFromContentPovider.addAll(logsFromContentProvider.map { it.numberFormated})

        var contactAddressFromCallLogTable : MutableList<String> = mutableListOf()

        callLogDAO?.getAllForDeleting().apply {
            if(this!=null){
                contactAddressFromCallLogTable.addAll(this.map {it.numberFormated!!})
                val idsToBeRemoved = contactAddressFromCallLogTable - idsFromContentPovider
                for(id in idsToBeRemoved){
                    callLogDAO?.deleteBycontactAddress(id)

                }
            }

        }

    }

    suspend fun updateCallLogWithServerInfo(serverInfo: CallersInfoFromServer) = withContext(Dispatchers.IO) {
        callLogDAO?.updateWitServerInfo(serverInfo.contactAddress, nameFromServer = serverInfo.firstName,spamCount =  serverInfo.spamReportCount)
    }


    suspend fun getNameForAddressFromContentProvider(contactAddress: String): NameAndThumbnail?  = withContext(Dispatchers.IO){
         var name:String?
         var thumbnail:String?
         var nameAndThumbnail: NameAndThumbnail? = null
//        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactAddress));
//
//        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )
//        if(cursor2!=null && cursor2.moveToFirst()){
////                    Log.d(TAG, "getConactInfoForNumber: data exist")
//            name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
//            thumbnail = cursor2.getString(cursor2.getColumnIndexOrThrow( ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
//            nameAndThumbnail = NameAndThumbnail(name?:"", thumbnail?:"")
//        }
//        var photoUri:String? = null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactAddress));
        val cursor = context.contentResolver.query(uri, null,  null, null, null )
        if(cursor!=null && cursor.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
            thumbnail = cursor.getString(cursor.getColumnIndexOrThrow( ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))

            name = cursor.getString(cursor.getColumnIndexOrThrow( ContactsContract.Contacts.DISPLAY_NAME))
            nameAndThumbnail = NameAndThumbnail(name?:"", thumbnail?:"")
        }
        return@withContext nameAndThumbnail
    }

    /**
     * searach in calllog db
     */
    suspend fun findFromCallLogTable(contactAddress: String): CallLogTable?  = withContext(Dispatchers.IO){
        return@withContext callLogDAO?.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryISO))

    }
    /**
     * searach in calllog db
     */
    suspend fun findOneFromCallLogTable(contactAddress: String): CallLogTable?  = withContext(Dispatchers.IO){
        return@withContext callLogDAO?.findOne(formatPhoneNumber(contactAddress))
    }

    suspend fun getFirst10Logs(): MutableList<CallLogTable>? = withContext(Dispatchers.IO) {
        return@withContext callLogDAO?.getFirst10Logs()
    }

   suspend fun updateWithCproviderInfo(number: String, nameAndThumbnailFromCp: NameAndThumbnail)  = withContext(Dispatchers.IO){
        callLogDAO?.updateWitCproviderInfo(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(number), countryISO), nameAndThumbnailFromCp.name, nameAndThumbnailFromCp.thumbnailUri )
    }

    suspend fun marAsReportedByUser(contactAddressList: List<String>) {
        for(num in contactAddressList){
            val formatedAdders = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(num), countryISO)
            callLogDAO?.markAsReportedByUser(formatedAdders, 1)
        }
//       val log =  callLogDAO?.findOne(formatedAdders)
//        if(log!=null){
//            var spamCount = log.spamCount
//            spamCount += 1

//        }
    }

    suspend fun updateCallLogWithSpamerDetails(serverInfo: CallersInfoFromServer) = withContext(Dispatchers.IO){
        callLogDAO?.updateSpammerWitServerInfo(serverInfo.contactAddress, serverInfo.firstName, serverInfo.spamReportCount, TYPE_SPAM)

    }

    suspend fun updateIdWithContentProviderInfo(item: CallLogTable) = withContext(Dispatchers.IO) {
        callLogDAO?.updateIdAndRelatedInfos(item.numberFormated,
            item.id,
            item.duration,
            item.dateInMilliseconds,
            item.thumbnailFromCp,
            item.simId
            )
    }

    suspend fun deleteCallLogFromDb(item: String) {
        callLogDAO?.deleteBycontactAddress(formatPhoneNumber(item))
    }

    suspend fun updateCallLogWithImgFromServer(item: CallersInfoFromServer) {
        callLogDAO?.updateWithServerImage(item.thumbnailImg, libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(item.contactAddress), countryISO))
    }

    suspend fun markAsSpamInSMS(contactAddress: String)  = withContext(Dispatchers.IO) {
        val formatedAddress = formatPhoneNumber(contactAddress)
        val res  = smsThreadsDAO?.find(formatedAddress)
        if(res!=null){
            var spamCount = res.spamCount
            spamCount+=1
            smsThreadsDAO?.updateSpamCount(formatedAddress, spamCount = spamCount )


        }

    }

    suspend fun startHashWork(applicationContext: Context?) = withContext(Dispatchers.IO) {

        applicationContext?.let {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(HashWorker::class.java)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(it).enqueue(oneTimeWorkRequest)
        }
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