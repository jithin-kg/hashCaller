package com.nibble.hashcaller.view.ui.sms.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.local.db.sms.mute.MutedSenders
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb.page
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.system.measureTimeMillis


/**
 * type 2 sent message
 * and type 1 recieved message
 * Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT
 * Message type: drafts.
Constant Value: 3 (0x00000003)

public static final int MESSAGE_TYPE_FAILED
Message type: failed outgoing message.
Constant Value: 5 (0x00000005)

MESSAGE_TYPE_INBOX
public static final int MESSAGE_TYPE_INBOX
Message type: inbox.
Constant Value: 1 (0x00000001)

MESSAGE_TYPE_OUTBOX
public static final int MESSAGE_TYPE_OUTBOX
Message type: outbox.
Constant Value: 4 (0x00000004)

MESSAGE_TYPE_QUEUED, when there is network issue
public static final int MESSAGE_TYPE_QUEUED
Message type: queued to send later.
Constant Value: 6 (0x00000006)

MESSAGE_TYPE_SENT
public static final int MESSAGE_TYPE_SENT
Message type: sent messages.
Constant Value: 2 (0x00000002)

PERSON
public static final String PERSON
The ID of the sender of the conversation, if present.

Type: INTEGER (reference to item in content://contacts/people)

Constant Value: "person"

READ
public static final String READ
Has the message been read?
Type: INTEGER (boolean)
Constant Value: "read"

SEEN
Added in API level 19

public static final String SEEN
Has the message been seen by the user? The "seen" flag determines whether we need to show a notification.
Type: INTEGER (boolean)
Constant Value: "seen"
 */
//

class SMSLocalRepository(
    private val context: Context,
    private val spamListDAO: SpamListDAO?,
    val smssendersInfoDAO: SMSSendersInfoFromServerDAO?,
    private val mutedSendersDAO: IMutedSendersDAO?
){
    private var smsListHashMap:HashMap<String?, String?> = HashMap<String?, String?>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    var markedThreadIds:MutableSet<Long> = mutableSetOf()

    companion object{
        private val URI: Uri = SMSContract.INBOX_SMS_URI

        private const val TAG = "__SMSLocalRepository"

    }


    fun getUnreadMsgCount(): Int? {
        var cursor:Cursor? = null
        var cnt:Int? = 0
        var address = "44"
        try {
            cursor = context.contentResolver.query(
                URI,
                null,
                "read = 0",
                null,
                null

            )
            cnt = cursor?.count

//            Log.d(TAG, "getUnreadMsgCount: count $cnt")

        }catch (e:Exception){

        }finally {
            cursor?.close()
        }

        return cnt
    }

    //gets sms for SMSLiveData to show all sms
    suspend fun fetchSMS(searchText:String?, isrequestingFromSmsSpamList:Boolean = false): MutableList<SMS> {
         fetchWithFullData(null, isrequestingFromSmsSpamList).apply {
            return this
        }
    }

    //this function fetches sms while searching
    suspend fun getSms(searchQuery: String?): MutableList<SMS> {

         fetchWithRawData(searchQuery, false).apply {
             return this
         }
    }

    @SuppressLint("LongLogTag")
    fun markSMSAsRead(addressString: String?){
        try {
            /**
             * This commented out code marks all sms as read
             */

            if(addressString==null){
                //mark all sms as read
                val values = ContentValues()
                values.put(Telephony.Sms.READ, 1)
                val selectionArgs:Array<String> = arrayOf("address='$addressString'")
                context.contentResolver.update(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    values, Telephony.Sms.READ + "=0 ",
                    null
                )
            }else{
                //mark the sms send by a contactaddress/sender as read
                Log.d(TAG+"markAsRead", "markSMSAsRead: address is $addressString")
                val values = ContentValues()
                values.put(Telephony.Sms.READ, 1)
                val selectionArgs:Array<String> = arrayOf("address='$addressString'")
//                val updatedRowcount = context.contentResolver.update(
//                    Telephony.Sms.Inbox.CONTENT_URI,
//                    values, Telephony.Sms.ADDRESS + "=?",
//                    selectionArgs
//                )

//                Log.d(TAG+"markAsRead", "markSMSAsRead: updatedRowCount $updatedRowcount")

//            Telephony.Sms.READ

                val cValues = ContentValues().apply {
                    put("read", 1)
                }

        context.contentResolver.update(URI,cValues, "address='$addressString'",null)
            }

        }catch (e:Exception){
            Log.d(TAG+"markAsRead", "update: exception $e")
        }


//
//        Log.d(TAG, "update: sms read count")


    }

    /**
     * To get first 10 sms
     */

    suspend fun fetchFirstPageOfSMS(): MutableList<SMS>  {
        var item1 = SMS()
        item1.isDummy = true

        var item2 = SMS()
        item2.isDummy = true

        var item3 = SMS()
        item3.isDummy = true

        var item4 = SMS()
        item4.isDummy = true

        var listOfSMS: MutableList<SMS> = mutableListOf()

        var selectionArgs: Array<String>? = null
        var selection: String? = null
        val projection = arrayOf(
            "DISTINCT address",
            "thread_id",
            "_id",
            "type",
            "body",
            "read",
            "date"
        )
        var cursor:Cursor? = null
        try {
            cursor =  context.contentResolver.query(
                SMSContract.ALL_SMS_URI,
                projection,
                null,
                selectionArgs,
                "_id  DESC LIMIT 10"
            )

            if (cursor != null && cursor.moveToFirst()) {
                //                    val spammersList = spamListDAO?.getAll()
                do {


                    //TODO check if phone number exists in contact, if then add the contact information too
                    val objSMS = SMS()
                    objSMS.id =
                        cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                    objSMS.threadID =
                        cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
//                            Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
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

                    setSpannableStringBuilder(objSMS, null, msg, num) //calling
                    // spannable string builder function to setup spannable string builder
                    objSMS.addressString = formatPhoneNumber(num)
                    objSMS.nameForDisplay = objSMS.addressString!!
                    if(markedThreadIds.contains(objSMS.threadID)){
                        objSMS.isMarked = true
                    }
                    objSMS.readState =
                        cursor.getInt(cursor.getColumnIndex("read"))
                    val dateMilli =
                        cursor.getLong(cursor.getColumnIndexOrThrow("date"))
//                    if(prevAddress != objSMS.addressString){
//                        prevAddress = objSMS.addressString!!
//                    }else{
//                        //equal
//                        continue
//                    }
                    objSMS.time = dateMilli
//                    setRelativeTime(objSMS, dateMilli)

                    if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                            .contains("1")
                    ) {
                        objSMS.folderName = "inbox"
                        Log.d(TAG, "fetch: inbox")
                    } else {
                        objSMS.folderName = "sent"
                        Log.d(TAG, "fetch: sent")

                    }

                    getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
                        if(this!=null){
                            objSMS.name = this?.name
                            if(!this.name.isNullOrEmpty()){
                                objSMS.nameForDisplay = this.name
                            }
                            objSMS.spamCount  = this.spamReportCount
                            objSMS.spammerType = this.spammerType
                            objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
                        }
                    }

                    if(!objSMS.msgString.isNullOrEmpty()){
//                        setSMSHashMap(objSMS)

                    }
//                    setSMSReadStatus(objSMS)
//                    setNameIfExistInContactContentProvider(objSMS)
                    listOfSMS.add(objSMS)


                } while (cursor.moveToNext())
            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "fetchFlowSMS:exception $e")
        }finally {
            cursor?.close()
        }
        listOfSMS.add(item1)
        listOfSMS.add(item2)
        listOfSMS.add(item3)
        listOfSMS.add(item4)

        return listOfSMS
        }

    @SuppressLint("LongLogTag")
    private suspend fun fetchWithFullData(searchQuery: String?, requestinfromSpamlistFragment: Boolean?): MutableList<SMS> {
        var data = ArrayList<SMS>()
        Log.d(TAG, "fetch: called")
        var prevAddress = ""
        var prevTime = 0L
//       val r1= GlobalScope.async {
        val cursor = createCursor(searchQuery)
        try {


            var deleteViewAdded = false
            val listOfMessages = mutableListOf<SMS>()
            var setOfAddress:MutableSet<String> = mutableSetOf()
            var count = 0
            var map: HashMap<String?, String?> = HashMap()
            smsListHashMap = map

            //        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC

//                Log.d(TAG, "fetch: page is   $page")

            //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
            if (cursor != null && cursor.moveToFirst()) {
                //                    val spammersList = spamListDAO?.getAll()
                do {

                    try {
                        //TODO check if phone number exists in contact, if then add the contact information too
                        val objSMS = SMS()
                        objSMS.id =
                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadID =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
//                            Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
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

                        setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                        // spannable string builder function to setup spannable string builder
                        objSMS.addressString = formatPhoneNumber(num)
                        objSMS.nameForDisplay = objSMS.addressString!!

                        objSMS.readState =
                            cursor.getInt(cursor.getColumnIndex("read"))

                        val dateMilli =
                            cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                        if(prevAddress != objSMS.addressString){
                            prevAddress = objSMS.addressString!!
                        }else{
                            //equal
                            continue
                        }
                        objSMS.time = dateMilli
                        setRelativeTime(objSMS, dateMilli)

                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                            Log.d(TAG, "fetch: inbox")
                        } else {
                            objSMS.folderName = "sent"
                            Log.d(TAG, "fetch: sent")

                        }

//                          val r =  async {  getDetailsFromDB(replaceSpecialChars(objSMS.addressString!!), objSMS) }.await()
//                                if(r!=null){
//                                    objSMS.name = r?.name
//                                    objSMS.spamCount  = r.spamReportCount
//                                    objSMS.spammerType = r.spammerType
//                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                                }
//
                            getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
                                if(this!=null){
                                    objSMS.name = this?.name
                                    if(!this.name.isNullOrEmpty()){
                                        objSMS.nameForDisplay = this.name
                                    }
                                    objSMS.spamCount  = this.spamReportCount
                                    objSMS.spammerType = this.spammerType
                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
                                }
                            }
                        Log.d(TAG, "fetch: message is   ${objSMS.msgString}")

                        if(!objSMS.msgString.isNullOrEmpty()){
//                                setSMSHashMap(objSMS)

                        }
                            setOfAddress.add(objSMS.nameForDisplay)
                                if(markedThreadIds.contains(objSMS.threadID)){
                                    objSMS.isMarked = true
                                }

                        listOfMessages.add(objSMS)

                    } catch (e: Exception) {
                        Log.d(TAG, "getMessages: exception $e")
                    }

                } while (cursor.moveToNext())
                //                            })
                //                        }


            }

            data.addAll(listOfMessages)


                setNameIfExistInContactContentProvider(data)
//                removeDeletedMSSFRomhashMap(setOfAddress)



        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetch: exception $e")
        }finally {
            cursor?.close()
        }
//        }
//        r1.await()

        return data
    }
    @SuppressLint("LongLogTag")
    private suspend fun fetchWithRawData(searchQuery: String?, requestinfromSpamlistFragment: Boolean?): MutableList<SMS> {
        var data = ArrayList<SMS>()
        val listOfMessages = mutableListOf<SMS>()

        Log.d(TAG, "fetch: called")
        var prevAddress = ""
        var prevTime = 0L
//       val r1= GlobalScope.async {
            val cursor = createCursor(searchQuery)
            try {


                var deleteViewAdded = false
                var setOfAddress:MutableSet<String> = mutableSetOf()
                var count = 0
                var map: HashMap<String?, String?> = HashMap()
                smsListHashMap = map

                //        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC

//                Log.d(TAG, "fetch: page is   $page")

                //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
                if (cursor != null && cursor.moveToFirst()) {
                    //                    val spammersList = spamListDAO?.getAll()
                    do {

                        try {
                            //TODO check if phone number exists in contact, if then add the contact information too
                            val objSMS = SMS()
                            objSMS.id =
                                cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            objSMS.threadID =
                                cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
//                            Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
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

                            setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                            // spannable string builder function to setup spannable string builder
                            objSMS.addressString = formatPhoneNumber(num)
                            objSMS.nameForDisplay = objSMS.addressString!!

                            objSMS.readState =
                                cursor.getInt(cursor.getColumnIndex("read"))

                            val dateMilli =
                                cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                            if(prevAddress != objSMS.addressString){
                                prevAddress = objSMS.addressString!!
                            }else{
                                //equal
                                continue
                            }
                            objSMS.time = dateMilli
                            setRelativeTime(objSMS, dateMilli)

                            if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                    .contains("1")
                            ) {
                                objSMS.folderName = "inbox"
                                Log.d(TAG, "fetch: inbox")
                            } else {
                                objSMS.folderName = "sent"
                                Log.d(TAG, "fetch: sent")

                            }

//                          val r =  async {  getDetailsFromDB(replaceSpecialChars(objSMS.addressString!!), objSMS) }.await()
//                                if(r!=null){
//                                    objSMS.name = r?.name
//                                    objSMS.spamCount  = r.spamReportCount
//                                    objSMS.spammerType = r.spammerType
//                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                                }
//
//                            getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
//                                if(this!=null){
//                                    objSMS.name = this?.name
//                                    if(!this.name.isNullOrEmpty()){
//                                        objSMS.nameForDisplay = this.name
//                                    }
//                                    objSMS.spamCount  = this.spamReportCount
//                                    objSMS.spammerType = this.spammerType
//                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                                }
//                            }
                            Log.d(TAG, "fetch: message is   ${objSMS.msgString}")

                            if(!objSMS.msgString.isNullOrEmpty()){
//                                setSMSHashMap(objSMS)

                            }
//                            setOfAddress.add(objSMS.nameForDisplay)
//                                if(markedThreadIds.contains(objSMS.threadID)){
//                                    objSMS.isMarked = true
//                                }

                                listOfMessages.add(objSMS)

                        } catch (e: Exception) {
                            Log.d(TAG, "getMessages: exception $e")
                        }

                    } while (cursor.moveToNext())
                    //                            })
                    //                        }


                }

//                data.addAll(listOfMessages)
//                setSMSReadStatus(data)

//                setNameIfExistInContactContentProvider(data)
//                removeDeletedMSSFRomhashMap(setOfAddress)



            } catch (e: java.lang.Exception) {
                Log.d(TAG, "fetch: exception $e")
            }finally {
                cursor?.close()
            }
//        }
//        r1.await()

        return listOfMessages
    }

    private fun removeDeletedMSSFRomhashMap(setOfAdderss: MutableSet<String>) {

//        for (address in mapofAddressAndSMS.keys){
//            if(!setOfAdderss.contains(address)){
//                //if not present in setOfAdderss, then we have to remove them mapofAddressAndSMS
//                mapofAddressAndSMS.remove(address)
//            }
//        }
    }

    @SuppressLint("LongLogTag")
    private fun setSMSHashMap(objSMS: SMS) {
//        if(!objSMS.addressString.isNullOrEmpty()){
//            Log.d(TAG, "setSMSHashMap: ")
//
//            val mr = mapofAddressAndSMS[objSMS.addressString!!]
//            if(mr==null){
//                //if not pressent in map
//                mapofAddressAndSMS[objSMS.addressString!!] = objSMS
//            }else{
//                val timFromMap = mr.time!!.toLong()
//                val timeFromCProvider = objSMS.time!!.toLong()
//                if( timFromMap < timeFromCProvider){
//                    //new message is objsms.time
//                    Log.d(TAG+"setSMSHashMaptS", " lesser map: $timFromMap cp: $timeFromCProvider")
//                    mapofAddressAndSMS.put(objSMS.addressString!!, objSMS)
//                }else if(mr.senderInfoFoundFrom!= objSMS.senderInfoFoundFrom){
//                    mapofAddressAndSMS[objSMS.addressString!!] = objSMS
//                }
//            }
//        }

    }

    private fun setSpannableStringBuilder(
        objSMS: SMS,
        searchQuery: String?,
        mssg: String,
        num: String
    ) {
        var msg = mssg
        var spannableStringBuilder: SpannableStringBuilder?

        if (searchQuery != null) {
            val lowercaseMsg = msg.toLowerCase()
            val lowerSearchQuery = searchQuery.toLowerCase()

            if (lowercaseMsg.contains(lowerSearchQuery) && searchQuery.isNotEmpty()) {
                //search query pressent in sms body
                var startPos =
                    lowercaseMsg.indexOf(lowerSearchQuery) //getting the index of search query in msg body
                var endPos = 0
                if(startPos > 50){
                    msg = "... " + msg.substring(startPos)
                    startPos = 4
                }
                endPos = startPos + lowerSearchQuery.length
                val yellow =
                    BackgroundColorSpan(Color.YELLOW)
                spannableStringBuilder =
                    SpannableStringBuilder(msg)
                spannableStringBuilder.setSpan(
                    yellow,
                    startPos,
                    endPos,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )

                objSMS.msg = spannableStringBuilder
                objSMS.address = SpannableStringBuilder(num)
            } else if (num.contains(searchQuery) && searchQuery.isNotEmpty()) {
                val startPos = num.indexOf(searchQuery)
                val endPos = startPos + searchQuery.length
                val yellow = BackgroundColorSpan(Color.YELLOW)
                spannableStringBuilder = SpannableStringBuilder(num)
                spannableStringBuilder.setSpan(
                    yellow,
                    startPos,
                    endPos,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                objSMS.address = spannableStringBuilder
                objSMS.msg = SpannableStringBuilder(msg)
            } else {
                spannableStringBuilder =
                    SpannableStringBuilder(msg)
                objSMS.msg = spannableStringBuilder
                objSMS.address = SpannableStringBuilder(num)
            }
        } else {
            spannableStringBuilder =
                SpannableStringBuilder(msg)
            objSMS.msg = spannableStringBuilder
            objSMS.address = SpannableStringBuilder(num)
        }

    }

    private fun setRelativeTime(objSMS: SMS, dateMilli: Long) {
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

    private fun setHourAndMinute(objSMS: SMS, dateMilli: Long): String {
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

    /**
     * function to create cursor
     * if search query is null create cursor with paging
     * else create normal cursor with selection arguments
     */
    private fun createCursor(searchQuery: String?): Cursor? {

        var selectionArgs: Array<String>? = null
        var selection: String? = null


        if (searchQuery != null) {
            selection = SMSContract.SMS_SELECTION_SEARCH
            selectionArgs = arrayOf("%$searchQuery%", "%$searchQuery%")
        }
        val projection = arrayOf(
            "thread_id",
            "_id",
            "address",
            "type",
            "body",
            "read",
            "date"
        )
        if(searchQuery == null){
            //from list sms fragment
            val cursor =  context.contentResolver.query(
                SMSContract.ALL_SMS_URI,
                projection,
                "address IS NOT NULL) GROUP BY (address",
                selectionArgs,
                "_id DESC"
            )

            return cursor
        }else{
            //from search sms activity
            selection = SMSContract.SMS_SELECTION_SEARCH
            selectionArgs = arrayOf("%$searchQuery%", "%$searchQuery%")
            val cursor =  context.contentResolver.query(
                SMSContract.ALL_SMS_URI,
                projection,
                selection,
                selectionArgs,
                SMSContract.SORT_DESC
            )
            return cursor
        }


    }

    /**
     * to add data in sms_senders_info from server
     */
    private fun setAdditionalData(data: ArrayList<SMS>) {
//           for (sms in data){
//              val res =  smssendersInfoDAO!!.find(sms.addressString!!)
//               if(res !=null){
//
//                   sms.spamCount =res.spamReportCount
//                   sms.spammerType = res.spammerType
//                   if(sms)
//               }
//           }
    }

    private fun setSpamDetails(data: ArrayList<SMS>) {


    }

    private fun setNameIfExistInContactContentProvider(data: ArrayList<SMS>) {
        for (sms in data){
            if(sms.addressString != null){
                var formattedNum = formatPhoneNumber(sms.addressString!!)

//                if(isNumericOnlyString(formattedNum)){
                    val name =   getConactInfoForNumber(formattedNum)
                    if (name != null){
                        sms.name = name
                        sms.nameForDisplay = name
                        sms.senderInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                    }
//                }

            }

        }

    }

    /**
     * function to get information from local db sms_senders_info_from_db
     * @param formattedNum , phone number
     */
    private suspend fun getDetailsFromDB(
        num: String,
        sms: SMS
    ): SMSSendersInfoFromServer?  {
        var r: SMSSendersInfoFromServer? = null

        coroutineScope{
            r = async {  smssendersInfoDAO!!.find(formatPhoneNumber(num)) }.await()
        }.apply {
            return r
        }
//        GlobalScope.launch {
//            r = async {  smssendersInfoDAO!!.find(formatPhoneNumber(num)) }.await()
//        }.join()
//        return r

    }

    private fun setContactName(sms: SMS) {
        var c: Contact = Contact(
            1L,
            "",
            "sample phone num",
            "",
            ""
        )
        var cursor:Cursor? = null
        try {
            val phoneNumber = sms.addressString
            var phoneNumField = ContactsContract.CommonDataKinds.Phone.NUMBER
            cursor = context.contentResolver.query(
                IndividualContactLiveData.URI,
                null,
                "'$phoneNumField' ='%${phoneNumber}%'",
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    //                val id = cursor.getLong(0)
                    //                val name = cursor.getString(1)
                    val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
//                    Log.d(TAG, "id is $id ")
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val photoURI =  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                    val times_used = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))


                    val phoneNo =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

//                    Log.d(TAG, "phone num is $phoneNo")
//                    Log.d(TAG, "name is  $name")
                    if(name!=null){

                        c = Contact(
                            id,
                            name,
                            photoURI,
                            "photoThumnail",
                            photoURI
                        )

                    }



                }while (cursor.moveToNext())
                sms.name = c.name
                sms.photoURI = c.photoURI

            }

        }catch (e:Exception){
            Log.d(TAG, "setContactName: exception $e")
        }finally {
            cursor?.close()


        }
    }


    /**
     * @param contact adderss
     * cross check in local database to check if the number is reported as spammer by user
     *
     */
    //TODO get the updated spammer list
    private suspend fun isThisAddressSpam(addressString: String?): Boolean {

        val info = addressString?.let { spamListDAO?.get(it) }

        if(info == null)
            return false

        return true

    }

    /**
     * Function to check whether the current message is opened/readed by the user
     */
    private fun setSMSReadStatus(
        smsList: ArrayList<SMS> ) {


        Log.d("__time", "setSMSReadStatus: called")

        for (sms in smsList) {
            if (sms.readState == 0)

                setCount(sms)

        }

    }

    private  fun setCount(sms: SMS) {
        val addressString = sms.addressString
        var cnt:Int? = 0

        val cursorSMSCount = context.contentResolver.query(
            URI,
            emptyArray<String>(),
            "read = 0 and address='$addressString'",
            null,
            null

        )
        try {
            cnt = cursorSMSCount?.count
            sms.unReadSMSCount = cnt!!
        }catch (e:Exception){
            Log.d(TAG, "setSMSReadStatus: exception $e ")
        }finally {
            cursorSMSCount?.close()
        }
    }


    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }

    @SuppressLint("LongLogTag")
    fun fetchIndividualSMS(contact: String?): List<SMS> {
        Log.d(TAG+"individual", "contact is $contact ")
        var count = 0
        var smsRef : SMS = SMS()
        var prevDate = ""
        var selectionArgs: Array<String>? = null
        var counter = 0 //counter to decide where to scroll, for Search Activity
        selectionArgs = arrayOf("$contact")
        var smslist = mutableListOf<SMS>()
        var cursor:Cursor? = null

        try {
            cursor = context.contentResolver.query(
                SMSContract.ALL_SMS_URI,
                null,
                SMSContract.SMS_SELECTION,
                selectionArgs,
                SMSContract.SORT_ASC
            )
//            cursor = context.contentResolver.query(
//                SMSContract.ALL_SMS_URI,
//                null,
//                "address  ()",
//                selectionArgs,
//                SMSContract.SORT_ASC
//            )
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    count++
                    val smsWithCurrentDate = SMS()
                    smsWithCurrentDate.type = -1 // cause this is just date, ie does not belong to any
                    //sms type so I can filer this from adapter
                    val t = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                    var currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date(t))

                    val days = getDaysDifference(t)
                    if(days == 0L){
                        currentDate = "Today"
                    }else if(days == 1L){
                        currentDate = "Yesterday"
                    }

                    if(currentDate != prevDate){
                        //for the first time add the date
                        //and if sms from different dates are in inbox then show date accoringly
                        prevDate = currentDate
                        smsWithCurrentDate.currentDate = prevDate
                        smslist.add(smsWithCurrentDate)
                    }
                    val sms = SMS()
                    sms.time = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                    sms.timeString = setHourAndMinute(sms, sms.time!!)
                    sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))
                    val mgsStr = sms.msgString
                    sms.id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("_id"))
                    sms.threadID = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))

                    sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
                    sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))


                    if(IndividualSMSActivity.chatId.isNotEmpty()){
                        if(sms.id.toString() == IndividualSMSActivity.chatId){
                            IndividualSMSActivity.chatScrollToPosition = counter
                            val startPos = sms.msgString!!.indexOf(IndividualSMSActivity.queryText!!)
                            val endPos = startPos + IndividualSMSActivity.queryText!!.length

                            val yellow = BackgroundColorSpan(Color.YELLOW)
                            val spannableStringBuilder =
                                SpannableStringBuilder(mgsStr)

                            spannableStringBuilder.setSpan(
                                yellow,
                                startPos,
                                endPos,
                                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                            sms.msg = spannableStringBuilder
                        }else{
                            val spannableStringBuilder =
                                SpannableStringBuilder(mgsStr)
                            sms.msg = spannableStringBuilder
                        }
                    }else{
                        val spannableStringBuilder =
                            SpannableStringBuilder(mgsStr)
                        sms.msg = spannableStringBuilder

                    }
                    smslist.add(sms)
                    counter++

                } while (cursor.moveToNext())

            }
        }catch (e:java.lang.Exception){
            Log.d(TAG+"individual", "fetchIndividualSMS: exception $e")
        }finally {
            cursor!!?.close()

        }
        Log.d(TAG+"individual", "fetchIndividualSMS: sizeL${smslist.size}, count:$count")

        return smslist
    }

    /**
     * Add to messages of type sd/dsds... to TextBasedSMSColums table
     */
    fun addMessageToOutBox(msg: String, contactAddress: String): String {
//        Log.d(TAG, "addMessageToOutBox: ")
        var time = System.currentTimeMillis().toString()
//        Log.d(TAG, "addMessageToOutBox: time is $time")
        val values = ContentValues().apply {
            put("body", "this the the  message while addinig to outbox")
            put("address", contactAddress)
            put("date", time)
            put("type", "4")
            put("thread_id", "0")
            put("date_sent", time)
        }


        var id:Int? = -1
        try {
            //the response will be  like content://sms/outbox/894
            val res = context.contentResolver.insert(SMSContract.ALL_SMS_URI, values)

            id = extractIdFromUri(res.toString())
//            saveSMSIdToDatabase(id)


//            Log.d(TAG, "addMessageToOutBox: $res")
        }catch (e:java.lang.Exception){
            Log.d(TAG, "addMessageToOutBox: exception $e")
        }
        Log.d(TAG, "id:$id")
        return time

    }

    fun moveFromoutBoxToSent(time: String?, address: String){
//        Log.d(TAG, "moveFromoutBoxToSent:  id $time")
//        Log.d(TAG, "moveFromoutBoxToSent:  address $address")
        val values = ContentValues().apply {
            put("type", "2")
        }
//        values.put("address", address)
//        values.put("body", "msg")
//        values.put("date", System.currentTimeMillis().toString())
//        values.put("type", "2")
//        values.put("thread_id", "0")
//        val idStr = time

//        val res =context.contentResolver.update(SMSContract.ALL_SMS_URI,cValues, "_id='$id'",null)
        val res =  context.contentResolver.update(SMSContract.ALL_SMS_URI, values,
            "date_sent = '$time' AND address = '$address'",null)
//        val res =context.contentResolver.delete(SMSContract.SMS_OUTBOX_URI, "_id='$id'",null)
//        Log.d(TAG, "moveFromoutBoxToSent: $res")
    }

    private fun saveSMSIdToDatabase(id: Int) {
//        GlobalScope.
//        smsDAO.insert(SMSDraft(id))

    }

    private fun extractIdFromUri(res: String): Int {
        val p: Pattern = Pattern.compile("\\d+")
        val m: Matcher = p.matcher(res)
        var id = ""
        while(m.find()) {
            id = m.group()
        }
//        Log.d(TAG, "extractIdFromUri: $id")
        return id.toInt()

    }

    suspend fun deleteAllSpamSMS() {
        val spamerslist = spamListDAO?.getAll()
        if (spamerslist != null) {
            for (spamer in spamerslist){

                val thread = Uri.parse("content://sms")
                val deleted: Int = context.contentResolver.delete(
                    thread,
                    "thread_id=?",
                    arrayOf<String>(
                        java.lang.String.valueOf(spamer.threadId)
                    )
                )
//                Log.d(TAG, "deleteAllSpamSMS: deleted: $deleted")
            }
        }


    }

    /**
     * function to get contact info for numbers
     * @param pno phone number
     */
    fun getConactInfoForNumber( pno: String): String? {
        var cursor:Cursor? = null
//        Log.d(TAG, "getConactInfoForNumber: pno $pno")
//        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        var name:String? = null
        val phoneNum = pno.replace("+", "").trim()
//        Log.d(TAG, "getConactInfoForNumber: phoneNum $phoneNum")
//        try {
//
//             cursor = context.contentResolver.query(
//                 ContactsContract.Data.CONTENT_URI,
//                null,
//                ContactsContract.CommonDataKinds.Phone.NUMBER +" LIKE ? ",
//                arrayOf("%$phoneNum%"),
//                null
//            )
////            cursor = context.contentResolver.query(
////                ContactLiveData.URI,
////                projection,
////                ContactsContract.CommonDataKinds.Phone.NUMBER +" LIKE ? ",
////                arrayOf("+919495617494"),
////                null
////            )
//
//            if(cursor!=null && cursor.moveToFirst()){
//                Log.d(TAG, "getConactInfoForNumber: contact exist")
//                do{
//                     name = cursor.getString(0)
//
//                }while (cursor.moveToNext())
//            }else{
//                Log.d(TAG, "getConactInfoForNumber: no such number")
//            }
//        }catch (e:Exception){
//            Log.d(TAG, "getConactInfoForNumber: exception $e")
//        }finally {
//            cursor?.close()
//        }
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pno));
//        cursor = context.contentResolver.query(
//                 ContactsContract.Data.CONTENT_URI,
//                null,
//                ContactsContract.CommonDataKinds.Phone.NUMBER +" LIKE ? ",
//                arrayOf("%$phoneNum%"),
//                null
//            )
        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )
        try{
            if(cursor2!=null && cursor2.moveToFirst()){
//                    Log.d(TAG, "getConactInfoForNumber: data exist")
                name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
            }else{
//                    Log.d(TAG, "getConactInfoForNumber: no date")
            }

        }catch (e:Exception){
            Log.d(TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }

//
        return name
    }

    /**
     * get info of sms senders from contentprovider contacts
     * @param smslist list of sms from conentprovider
     * Here the smslist is passed as refernce so I dont need to return value
     * so whenever there is a change it will reflect in original list
     */
    fun getInfoFromContacts(smslist: List<SMS>): List<SMS> {

        val regex = "[0-9]+"
        val pattern = Pattern.compile(regex)
        for(sms in smslist){
            val formattedNumber = formatPhoneNumber(sms.addressString!!)
            val m = pattern.matcher(formattedNumber)
            if(m.matches()){
                //if the address is not name ("jio-4g, ideacareetc")
                //ie the address is number 34834,555,802383213
                val name = getConactInfoForNumber(sms.addressString!!)
                Log.d(TAG, "getInfoFromContacts: name is $name")

                sms.name = name
            }

        }

        return smslist

    }

    /**
     * function to get information of each sms sender from localDB
     * these information in local db(SMSSendersInfoFromServer) is saved via SmsHashedNumUploadWorker
     *
     */
    suspend fun getInfoFromLocalDb(smslist: List<SMS>) {
        for(sms in smslist){
            Log.d(TAG, "getInfoFromLocalDb: ")
            //replace all special character and search in db
            var num = sms.addressString
            var res:SMSSendersInfoFromServer? = null
            num = formatPhoneNumber(num!!)
            res = smssendersInfoDAO!!.find(formatPhoneNumber(num)!!).apply {
                if(sms.name.isNullOrEmpty()){
                    sms.name = res?.name
                    Log.d(TAG, "getInfoFromLocalDb:  empty ")

                }else{
                    Log.d(TAG, "getInfoFromLocalDb: not empty ")
                }
            }


//            try {
//                GlobalScope.launch(Dispatchers.Main) {
//                   val res = withContext(Dispatchers.IO)  {
////                        res = getSenderInfo(num).await()
//                        smssendersInfoDAO!!.find(num!!)
//
//                    }
//                    sms.name = "Moriarity"
//                }
//
//
//            }catch (e:java.lang.Exception){
//                Log.d(TAG, "getInfoFromLocalDb: exception $e")
//            }finally {
//
//                if(res !=null){
//                    Log.d(TAG, "getInfoFromLocalDb: not  empty $res")
//                    if(sms.name.isNullOrEmpty()){
//                        Log.d(TAG, "getInfoFromLocalDb: name is  null result name is ${res!!.name}")
//                        sms.name = "Adam"
//                    }else{
//                        Log.d(TAG, "getInfoFromLocalDb: name is not null ${sms.name} ")
//                    }
//                    sms.spamCount = res!!.spamReportCount
//                    sms.spammerType = res!!.spammerType!!
//
//                }else{
//                    Log.d(TAG, "getInfoFromLocalDb:  empty $res")
//                }
//            }


        }
    }

    private fun getSenderInfo(num: String) = GlobalScope.async{
        smssendersInfoDAO!!.find(formatPhoneNumber(num))
    }

    fun getSmsSenderInforFromDB(): LiveData<List<SMSSendersInfoFromServer>> {
        return smssendersInfoDAO!!.getAllLiveData()
    }

    /**
     * function to return sms and address in content provider for SmsHashedNumUploadWorker
     */
    @SuppressLint("LongLogTag")
    suspend fun fetchSmsForWorker(): MutableList<SMS> {
        var data = ArrayList<SMS>()
        var hashSetOfAddress:HashSet<String> = hashSetOf()
        try {
            val listOfMessages = mutableListOf<SMS>()
            var selectionArgs: Array<String>? = null



            val projection = arrayOf(
                "thread_id",
                "_id",
                "address",
                "type",
                "body",
                "read",
                "date"


            )

//        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC
            val cursor = context.contentResolver.query(
                SMSContract.ALL_SMS_URI,
                projection,
                "address IS NOT NULL) GROUP BY (address",
                selectionArgs,
                "_id DESC"
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    try {
                        //TODO check if phone number exists in contact, if then add the contact information too
                        val objSMS = SMS()
                        objSMS.id =
                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadID =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                        Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
                        var num =
                            cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        num = num.replace("+", "")

                        objSMS.type =
                            cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                        val msg =
                            cursor.getString(cursor.getColumnIndexOrThrow("body"))

                        objSMS.addressString = num.replace("+", "")

                        objSMS.readState =
                            cursor.getInt(cursor.getColumnIndex("read"))
                        val dateMilli =
                            cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                        objSMS.time = dateMilli

                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                        } else {
                            objSMS.folderName = "sent"
                        }
                        setSpannableStringBuilder(objSMS, null, msg, num)
                        if(!hashSetOfAddress.contains(objSMS.addressString)){
                            hashSetOfAddress.add(objSMS.addressString!!)
                            listOfMessages.add(objSMS)
                        }

                    } catch (e: Exception) {
                        Log.d(TAG, "getMessages: $e")
                    }

                } while (cursor.moveToNext())

            }
            data.addAll(listOfMessages)

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetchSmsForWorker: exception $e")
        }



        Log.d(TAG, "fetchSmsForWorker: size of list is ${data.size}")
        if(data.size >= 1)
            Log.d(TAG, "fetchSmsForWorker: first item msg is  ${data[0].msg}")
        return data
    }


    /**
     * function for fetching sms for smspam viewmodel
     */
    @SuppressLint("LongLogTag")
    private suspend fun fetchForVidemodel(searchQuery: String?, requestinfromSpamlistFragment: Boolean?): MutableList<SMS> {
        var data = ArrayList<SMS>()

        val timeTook = measureTimeMillis {
            try {


                var deleteViewAdded = false
                val listOfMessages = mutableListOf<SMS>()
                var count = 0
                var map: HashMap<String?, String?> = HashMap()
                smsListHashMap = map

                //        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC
                val cursor = createCursor(searchQuery)
                Log.d(TAG, "fetch: page is   $page")

                //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
                if (cursor != null && cursor.moveToFirst()) {
                    //                    val spammersList = spamListDAO?.getAll()
                    do {

                        try {
                            //TODO check if phone number exists in contact, if then add the contact information too
                            val objSMS = SMS()
                            objSMS.id =
                                cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            objSMS.threadID =
                                cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                            Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
                            var num =
                                cursor.getString(cursor.getColumnIndexOrThrow("address"))
                            num = num.replace("+", "")
                            //                    objSMS.address = num

                            objSMS.type =
                                cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                            var msg =
                                cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            //

                            setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                            // spannable string builder function to setup spannable string builder
                            objSMS.addressString = num.replace("+", "")

                            objSMS.readState =
                                cursor.getInt(cursor.getColumnIndex("read"))
                            val dateMilli =
                                cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            objSMS.time = dateMilli
                            setRelativeTime(objSMS, dateMilli)

                            if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                    .contains("1")
                            ) {
                                objSMS.folderName = "inbox"
                            } else {
                                objSMS.folderName = "sent"
                            }
                            getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
                                if(this!=null){
                                    objSMS.name = this?.name
                                    objSMS.spamCount  = this.spamReportCount
                                    objSMS.spammerType = this.spammerType
                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
                                }

                                if (requestinfromSpamlistFragment!!) {
                                    //if we are requesting from fragment SMSIdentifiedAsSpamFragment
                                    //                                if (smsListHashMap.containsKey(objSMS.addressString)) {
                                    if (!deleteViewAdded) {
                                        val delViewObj = SMS()
                                        delViewObj.deleteViewPresent = true
                                        listOfMessages.add(delViewObj)
                                        deleteViewAdded = true
                                    }
                                    if(objSMS.spamCount>0)
                                        listOfMessages.add(objSMS)
                                    //                                }
                                } else {
                                    //we are requesting from SMSListFragment


                                    if(objSMS.spamCount<1)
                                        listOfMessages.add(objSMS)

                                }
                            }


                            //                this.smsListHashMap.put(objSMS.addressString!!,count.toString())
                            count = listOfMessages.size - 1
                        } catch (e: Exception) {
                            Log.d(TAG, "getMessages: $e")
                        }

                    } while (cursor.moveToNext())
                    //                            })
                    //                        }


                }

                //        data = sortAndSet(listOfMessages)
                data.addAll(listOfMessages)
                //        setAdditionalData(data)

                scope.launch {
//                    val r1 =  async {  setSMSReadStatus(data) }
                    //        setSpamDetails(data)
                    val r2 = async {  setNameIfExistInContactContentProvider(data) }
//                    r1.await()
                    r2.await()
                }.join()



            } catch (e: java.lang.Exception) {
                Log.d(TAG, "fetch: exception $e")
            }


        }


        return data
    }



    suspend fun getSMSForViewModel(searchQuery: String?, requestinfromSpamlistFragment: Boolean?,
                                   isFullSmsNeeded :Boolean = false): MutableList<SMS> {
        var data = ArrayList<SMS>()
        Log.d(TAG, "getSMSForSpammList: ")
        scope.launch {
            try {


                var deleteViewAdded = false
                val listOfMessages = mutableListOf<SMS>()
                var count = 0
                var map: HashMap<String?, String?> = HashMap()
                smsListHashMap = map

                //        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC
//                val cursor = createCursor(searchQuery)

                var selectionArgs: Array<String>? = null
                var selection: String? = null
                val projection = arrayOf(
                    "thread_id",
                    "_id",
                    "address",
                    "type",
                    "body",
                    "read",
                    "date"
                )
                val cursor = if(isFullSmsNeeded){
                    //requesting from smsspam viewmodel after smssenderinfofromserver changed
                    context.contentResolver.query(
                        SMSContract.ALL_SMS_URI,
                        projection,
                        "address IS NOT NULL) GROUP BY (address",
                        selectionArgs,
                        "_id DESC"
                    )
                }else{
                    context.contentResolver.query(
                        SMSContract.ALL_SMS_URI,
                        projection,
                        "address IS NOT NULL) GROUP BY (address",
                        selectionArgs,
                        "_id DESC "
                    )
                }

//                Log.d(TAG, "fetch: page is   $pageSpa")

                //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
                if (cursor != null && cursor.moveToFirst()) {
                    //                    val spammersList = spamListDAO?.getAll()
                    do {

                        try {
                            //TODO check if phone number exists in contact, if then add the contact information too
                            val objSMS = SMS()
                            objSMS.id =
                                cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                            objSMS.threadID =
                                cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                            Log.d(TAG, "getSMSForSpammList: threadid ${objSMS.threadID}")
                            var num =
                                cursor.getString(cursor.getColumnIndexOrThrow("address"))
                            num = num.replace("+", "")
                            //                    objSMS.address = num

                            objSMS.type =
                                cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                            var msg =
                                cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            objSMS.msgString = msg

                            //

                            setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                            // spannable string builder function to setup spannable string builder
                            objSMS.addressString = num.replace("+", "")
                            objSMS.addressString = formatPhoneNumber(num)
                            objSMS.nameForDisplay = objSMS.addressString!!

                            objSMS.readState =
                                cursor.getInt(cursor.getColumnIndex("read"))
                            val dateMilli =
                                cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            objSMS.time = dateMilli
                            setRelativeTime(objSMS, dateMilli)

                            if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                    .contains("1")
                            ) {
                                objSMS.folderName = "inbox"
                            } else {
                                objSMS.folderName = "sent"
                            }
                          val r = async { getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS)  }.await()
                                if(r!=null){
                                    objSMS.name = r?.name
                                    objSMS.nameForDisplay = r.name
                                    objSMS.spamCount  = r.spamReportCount
                                    objSMS.spammerType = r.spammerType
                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB

                            }
//                            setSMSHashMap(objSMS)
                            listOfMessages.add(objSMS)
                        } catch (e: Exception) {
                            Log.d(TAG, "getSMSForSpammList: $e")
                        }

                    } while (cursor.moveToNext())
                    //                            })
                    //                        }


                }

                //        data = sortAndSet(listOfMessages)
                data.addAll(listOfMessages)
                //        setAdditionalData(data)
                scope.launch {
//                    val r1 =  async {  setSMSReadStatus(data) }
                    //        setSpamDetails(data)
                    val r2 = async {  setNameIfExistInContactContentProvider(data) }
//                    r1.await()
                    r2.await()
                }.join()

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "getSMSForSpammList: exception $e")

            }
        }.join()
        Log.d(TAG, "getSMSForSpammList: size is  ${data.size}")




        return data
    }

    /**
     * Adding a new sms sender info who is a spammer
     */
    suspend fun save(contactAddress: String, i: Int, s: String, s1: String) {
        var name = ""
        var spamCount = 0L
        smssendersInfoDAO!!.find(formatPhoneNumber(contactAddress)).apply {
            if(this!=null){
                name = this.name
                spamCount = this.spamReportCount

            }

            spamCount+=1
            val info = SMSSendersInfoFromServer(contactAddress, 0,name, Date(), spamCount)
            val list = listOf<SMSSendersInfoFromServer>(info)

            smssendersInfoDAO!!.insert(list)
            pageOb.page = 0
        }

    }
    suspend fun report(callerInfo: ReportedUserDTo) : Response<NetWorkResponse>? {
        var retrofitService:ISpamService? = null

        retrofitService = RetrofitClient.createaService(ISpamService::class.java)
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val tokenManager = TokenManager(sp)
        val token = tokenManager.getToken()
        return retrofitService?.report(callerInfo, token)
    }


    @SuppressLint("LongLogTag")
    suspend fun deleteSmsThread(): Int {
        smsDeletingStarted = true
        var numRowsDeleted = 0
        var copy:MutableList<Long> = mutableListOf()
        copy.addAll(markedThreadIds)
        try{
            for(id in copy) {
                Log.d(TAG, "deleteSmsThread: threadid $id")
                var uri = Telephony.Sms.CONTENT_URI
                val selection = "${Telephony.Sms.THREAD_ID} = ?"
                val selectionArgs = arrayOf(id.toString())
                try {
                    delay(800L).apply {
                    }

                } finally { // this is to slow down deleting by 800mlseconds to see user deleting happening
                    numRowsDeleted = context.contentResolver.delete(uri, selection, selectionArgs)
                    Log.d(TAG, "deleteSmsThread: number  of  rows deleted $numRowsDeleted")


                }
            }
        }catch (e: Exception) {
            Log.d(SMScontainerRepository.TAG, "deleteSmsThread: exception $e")
        }finally {
            deleteList()
        }

        return numRowsDeleted
    }


    private fun deleteList() {
        markedThreadIds.clear()
    }

    /***
     * function to add contact address to muted_senders table,
     * no notification for incoming sms from muted senders
     */
    suspend fun muteSenders() {
        var addressList: MutableList<MutedSenders> = mutableListOf()
        for (address in MarkedItemsHandler.markedContactAddress){
            val mutedSender = MutedSenders(formatPhoneNumber(address))
            addressList.add(mutedSender)
        }
        mutedSendersDAO!!.insert(addressList)
    }

    suspend fun deleteAllSMmsendersINo() {
        smssendersInfoDAO!!.deleteAll()
    }

    fun searchSmsForIndividualSMS(text: String, contactAddress: String?): List<SMS> {
        var selectionArgs: Array<String>? = null
        var selection: String? = null



            selection = SMSContract.SMS_SELECTION_SEARCH
            selectionArgs = arrayOf("%$contactAddress%", "%$text%")

        val projection = arrayOf(
            "thread_id",
            "_id",
            "address",
            "type",
            "body",
            "read",
            "date"
        )

        selection = SMSContract.SMS_SELECTION_SEARCH_INDIVIDUAL
        val cursor =  context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            projection,
            selection,
            selectionArgs,
            SMSContract.SORT_DESC
        )
        var smslist = mutableListOf<SMS>()

        try {
            if(cursor != null && cursor.moveToFirst()) {
                do {

                    val smsWithCurrentDate = SMS()
                    smsWithCurrentDate.type = -1 // cause this is just date, ie does not belong to any
                    //sms type so I can filer this from adapter
                    val t = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                    var currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date(t))

                    val days = getDaysDifference(t)
                    if(days == 0L){
                        currentDate = "Today"
                    }else if(days == 1L){
                        currentDate = "Yesterday"
                    }

                    val sms = SMS()
                    sms.time = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                    sms.timeString = setHourAndMinute(sms, sms.time!!)
                    sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))
                    val mgsStr = sms.msgString
                    sms.id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("_id"))
                    sms.threadID = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))

                    sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
                    sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))


                    if(IndividualSMSActivity.chatId.isNotEmpty()){
                        if(sms.id.toString() == IndividualSMSActivity.chatId){
                            val startPos = sms.msgString!!.indexOf(IndividualSMSActivity.queryText!!)
                            val endPos = startPos + IndividualSMSActivity.queryText!!.length

                            val yellow = BackgroundColorSpan(Color.YELLOW)
                            val spannableStringBuilder =
                                SpannableStringBuilder(mgsStr)

                            spannableStringBuilder.setSpan(
                                yellow,
                                startPos,
                                endPos,
                                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                            sms.msg = spannableStringBuilder
                        }else{
                            val spannableStringBuilder =
                                SpannableStringBuilder(mgsStr)
                            sms.msg = spannableStringBuilder
                        }
                    }else{
                        val spannableStringBuilder =
                            SpannableStringBuilder(mgsStr)
                        sms.msg = spannableStringBuilder

                    }
                    smslist.add(sms)


                } while (cursor.moveToNext())

            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "getSmsForIndividualSMS: exception $e")
        }

        return  smslist
    }

    fun fetchFlowSMS(): Flow<SMS> = flow {
        var selectionArgs: Array<String>? = null
        var selection: String? = null
        val projection = arrayOf(
            "thread_id",
            "_id",
            "address",
            "type",
            "body",
            "read",
            "date"
        )

        val cursor =  context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            projection,
            null,
            selectionArgs,
            "_id DESC"
        )


        if (cursor != null && cursor.moveToFirst()) {
            //                    val spammersList = spamListDAO?.getAll()
            do {

                try {
                    //TODO check if phone number exists in contact, if then add the contact information too
                    val objSMS = SMS()
                    objSMS.id =
                        cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                    objSMS.threadID =
                        cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
//                            Log.d(TAG, "fetch: threadid ${objSMS.threadID}")
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

//                    setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                    // spannable string builder function to setup spannable string builder
                    objSMS.addressString = num.replace("+", "")
                    objSMS.addressString = formatPhoneNumber(num)
                    objSMS.nameForDisplay = objSMS.addressString!!

                    objSMS.readState =
                        cursor.getInt(cursor.getColumnIndex("read"))
                    val dateMilli =
                        cursor.getLong(cursor.getColumnIndexOrThrow("date"))
//                    if(prevAddress != objSMS.addressString){
//                        prevAddress = objSMS.addressString!!
//                    }else{
//                        //equal
//                        continue
//                    }
                    objSMS.time = dateMilli
                    setRelativeTime(objSMS, dateMilli)

                    if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                            .contains("1")
                    ) {
                        objSMS.folderName = "inbox"
                        Log.d(TAG, "fetch: inbox")
                    } else {
                        objSMS.folderName = "sent"
                        Log.d(TAG, "fetch: sent")

                    }

                    emit(objSMS)
                } catch (e: Exception) {
                    Log.d(TAG, "getMessages: exception $e")
                }

            } while (cursor.moveToNext())



        }
    }

    /**
     * function to get name for a contact address from DB in individual sms activity
     */
    suspend fun getContactInfoFRomDB(pno: String): String? {
//        val numWithoutSpecialChars = replaceSpecialChars(pno)
        var result : SMSSendersInfoFromServer? = null
        var name :String ? = null
//        if(isNumericOnlyString(numWithoutSpecialChars)){
//            var formatednum = formatPhoneNumber(numWithoutSpecialChars)
//              smssendersInfoDAO!!.find(formatednum).apply {
//                  result =this
//            }
//        }else{
//            //number of type containing sring like jio
//              smssendersInfoDAO!!.find(numWithoutSpecialChars).apply {
//                  result = this
//            }
//
//        }
        smssendersInfoDAO!!.find(formatPhoneNumber(pno)).apply {
            result =this
            if(result!=null){
                name = result!!.name
            }
            return name
        }

    }

    suspend fun saveSpamReportedByUser(contactAddress: String, threadID: Long, spammerType: Int?, spammerCategory: Int) {
        val formatedAddress = formatPhoneNumber(contactAddress)
        smssendersInfoDAO!!.find(formatedAddress).apply {
            if(this!=null){
                //already infor exists
                    val spamcount = this.spamReportCount +1
                smssendersInfoDAO!!.updateSpamCount(spamcount, true, this.contactAddress)
            }else{
                smssendersInfoDAO.insert(listOf(SMSSendersInfoFromServer(
                    formatedAddress,
                    spammerType!!,
                    " ",Date(), 0, true )))
            }
        }
    }


}

