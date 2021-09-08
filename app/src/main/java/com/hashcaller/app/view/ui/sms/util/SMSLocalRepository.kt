package com.hashcaller.app.view.ui.sms.util

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
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.blocklist.SpamListDAO
import com.hashcaller.app.local.db.sms.mute.IMutedSendersDAO
import com.hashcaller.app.local.db.sms.mute.MutedSenders
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.db.ICallLogDAO
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.sms.SMScontainerRepository
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.ui.sms.db.NameAndThumbnail
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable
import com.hashcaller.app.view.ui.sms.individual.IndividualSMSActivity
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
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
    val callerInfoDAO: CallersInfoFromServerDAO?,
    private val mutedSendersDAO: IMutedSendersDAO?,
    private val smsThreadsDAO: ISMSThreadsDAO?,
    private val dataStoreRepostiroy: DataStoreRepository,
    private val tokenHelper: TokenHelper?,
    private val callLogDAO: ICallLogDAO?,
    private val smsRepositoryHelper: SmsRepositoryHelper,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
    private val countryISO: String,



    ){
    private var smsListHashMap:HashMap<String?, String?> = HashMap<String?, String?>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    var markedThreadIds:MutableSet<Long> = mutableSetOf()

    companion object{
        private val URI: Uri = SMSContract.INBOX_SMS_URI

        private const val TAG = "__SMSLocalRepository"
//        var queryText:String? =null

    }


    suspend fun getUnreadMsgCount(): Int?   = withContext(Dispatchers.IO){
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


        }catch (e:Exception){

        }finally {
            cursor?.close()
        }

        return@withContext cnt
    }

    //gets sms for SMSLiveData to show all sms
    suspend fun fetchSMS(searchText:String?, isrequestingFromSmsSpamList:Boolean = false): ArrayList<SmsThreadTable>  = withContext(Dispatchers.IO) {
        return@withContext fetchSMSForLivedata(null, isrequestingFromSmsSpamList)
    }


    //this function fetches sms while searching
    suspend fun getSms(searchQuery: String?): MutableList<SMS>   = withContext(Dispatchers.IO){

        return@withContext smsRepositoryHelper.fetchWithRawData()
    }


    /**
     * function to mark the sms as read in smsthreads table
     */
    suspend fun marAsReadInDB(contactAddress: String)  = withContext(Dispatchers.IO) {
        smsThreadsDAO?.markAsRead(contactAddress, 1)
    }
    @SuppressLint("LongLogTag")
    suspend fun markSMSAsRead(addressString: String?)  = withContext(Dispatchers.IO){
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

//                    setSpannableStringBuilder(objSMS, null, msg, num) //calling
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
                    } else {
                        objSMS.folderName = "sent"

                    }

                    getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
                        if(this!=null){
                            objSMS.firstNameFromServer = this?.firstName
                            objSMS.lastNameFromServer = this.lastName
                            objSMS.spamCount  = this.spamReportCount
                            objSMS.spammerType = this.spammerType
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
     suspend fun fetchSMSForLivedata(searchQuery: String?, requestinfromSpamlistFragment: Boolean?): ArrayList<SmsThreadTable> = withContext(Dispatchers.IO) {
        var data = ArrayList<SmsThreadTable>()
        var prevAddress = ""
        var prevTime = 0L
//       val r1= GlobalScope.async {
        val cursor = createCursor(searchQuery)
        try {

            var deleteViewAdded = false
            val listOfMessages = mutableListOf<SmsThreadTable>()
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
                        val objSMS = SmsThreadTable()
//                        objSMS.id =
//                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadId =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                        var num =
                            cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        objSMS.numFormated = formatPhoneNumber(num)
                        objSMS.contactAddress = num
                        objSMS.type =
                            cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                        var msg =
                            cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        objSMS.body = msg

                        objSMS.readState =
                            cursor.getInt(cursor.getColumnIndex("read"))

                        val dateMilli =
                            cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                        objSMS.dateInMilliseconds = dateMilli
                        objSMS.spamCount = 0

                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                        } else {
                            objSMS.folderName = "sent"
                        }
                        setOfAddress.add(objSMS.contactAddress)
                        listOfMessages.add(objSMS)

                    } catch (e: Exception) {
                        Log.d(TAG, "getMessages: exception $e")
                    }

                } while (cursor.moveToNext())

            }

            data.addAll(listOfMessages)

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetch: exception $e")
        }finally {
            cursor?.close()
        }

        return@withContext data
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
    private fun setSpannableStrinForName(objSMS: SMS, searchQuery: String) {
        val lowerSearchQuery = searchQuery.toLowerCase()
        var startPos = 0
        var endPos = 0
        if(objSMS.firstName!=null){
            var lowerCaseName =objSMS.firstName!!.toLowerCase()

            var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(lowerCaseName)

            if(lowerCaseName.contains(searchQuery)){
                startPos = lowerCaseName.indexOf(searchQuery)
                objSMS.spanStartPosNameCp = startPos
                endPos = startPos + searchQuery.length
                objSMS.spanEndPosNameCp = endPos
//                val yellow =
//                    ForegroundColorSpan(Color.BLUE)
//                spannableStringBuilder.setSpan(
//                    yellow,
//                    startPos,
//                    endPos,
//                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
//                )

            }
//            objSMS.address = spannableStringBuilder!!
        }
    }


    private fun setSpannableStringBuilder(
        objSMS: SMS,
        searchQuery: String?,
        mssg: String,
        num: String
    ) {
        val lowercaseNum = num.toLowerCase()
        var msg = mssg
        var spannableStringBuilder: SpannableStringBuilder?

        if (searchQuery != null) {
            val lowercaseMsg = msg.toLowerCase()
            val lowerSearchQuery = searchQuery.toLowerCase()
            objSMS.address = SpannableStringBuilder(num)
            objSMS.msg = SpannableStringBuilder(msg)

            if (lowercaseMsg.contains(lowerSearchQuery) && searchQuery.isNotEmpty()) {
                //search query pressent in sms body
                var startPos =
                    lowercaseMsg.indexOf(lowerSearchQuery) //getting the index of search query in msg body
                var endPos = 0
                if(startPos > 50){
                    msg = "... " + msg.substring(startPos)
                    startPos = 4
                }
                objSMS.body = msg
                endPos = startPos + lowerSearchQuery.length
                objSMS.spanStartPosMsgPeek = startPos
                objSMS.spanEndPosMsgPeek = endPos
            }
            if (lowercaseNum.contains(searchQuery) && searchQuery.isNotEmpty()) {
                val startPos = lowercaseNum.indexOf(searchQuery)
                val endPos = startPos + searchQuery.length
                val yellow = BackgroundColorSpan(Color.YELLOW)
                objSMS.spanStartPos = startPos
                objSMS.spanEndPos = endPos

            }
        }
//        else {
//            spannableStringBuilder =
//                SpannableStringBuilder(msg)
//            objSMS.msg = spannableStringBuilder
//            objSMS.address = SpannableStringBuilder(num)
//        }

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
                    val name =   getNameForNumberFromCprovider(formattedNum)
                    if (name != null){
                        sms.firstName = name
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
    ): CallersInfoFromServer?   = withContext(Dispatchers.IO) {
        var r: CallersInfoFromServer? = null

        return@withContext callerInfoDAO!!.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(num), countryISO))
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
                sms.firstName = c.firstName
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

    private  fun setCount(sms: SMS)  {
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
    suspend fun fetchIndividualSMS(contact: String?): List<SMS>  = withContext(Dispatchers.IO) {
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

                    val days = smsRepositoryHelper.getDaysDifference(t)
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
                    sms.timeString = smsRepositoryHelper.setHourAndMinute(sms, sms.time!!)
                    sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body")).trim()
                    val mgsStr = sms.msgString
                    sms.id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("_id"))
                    sms.threadID = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))

                    sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
                    sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))



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

        return@withContext smslist
    }

    /**
     * Add to messages of type sd/dsds... to TextBasedSMSColums table
     */
    suspend fun addMessageToOutBox(msg: String, contactAddress: String): String  = withContext(Dispatchers.IO) {
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
        return@withContext time

    }

    suspend fun moveFromoutBoxToSent(time: String?, address: String)  = withContext(Dispatchers.IO){
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

    suspend fun deleteAllSpamSMS()  = withContext(Dispatchers.IO) {
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
    fun getNameForNumberFromCprovider(pno: String): String? {
        var cursor:Cursor? = null
        var name:String? = null
        val phoneNum = pno.replace("+", "").trim()
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pno));
        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )
        try{
            if(cursor2!=null && cursor2.moveToFirst()){
                name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
            }

        }catch (e:Exception){
            Log.d(TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }

//
        return name
    }

    fun getInfoFromCproviderForNum(numFormated: String): Contact? {
       var contact:Contact? = null
        val phoneNum = formatPhoneNumber(numFormated)
        var cursor2:Cursor? = null
        try{
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
             cursor2 = context.contentResolver.query(uri, null,  null, null, null )
            if(cursor2!=null && cursor2.moveToFirst()){
//               val  name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
               contact = Contact( 0, "", photoThumnailServer = "" , phoneNumber =numFormated )
            val name =
                    cursor2.getString(cursor2.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val photoURI =  cursor2.getString(cursor2.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                val times_used = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                contact?.firstName = name
                contact?.thumbnailInCprovider = photoURI
            }
        }catch (e:Exception){
            Log.d(TAG, "getConactInfoForNumber: exception $e")
        }finally {
            cursor2?.close()
        }
        return contact
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
                val name = getNameForNumberFromCprovider(sms.addressString!!)
                Log.d(TAG, "getInfoFromContacts: name is $name")

                sms.firstName = name
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
            var res:CallersInfoFromServer? = null
            num = formatPhoneNumber(num!!)
            res = callerInfoDAO!!.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(num)!!, countryISO)).apply {
                if(sms.firstName.isNullOrEmpty()){
                    sms.firstName = res?.firstName
                    Log.d(TAG, "getInfoFromLocalDb:  empty ")

                }else{
                    Log.d(TAG, "getInfoFromLocalDb: not empty ")
                }
            }


//            try {
//                CorutinScope.launch(Dispatchers.Main) {
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

    private fun getSenderInfo(num: String) = CoroutineScope(Dispatchers.IO).async{
        callerInfoDAO!!.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(num), countryISO))
    }

    fun getSmsSenderInforFromDB(): LiveData<List<CallersInfoFromServer>>{
        return callerInfoDAO!!.getAllLiveData()
    }

    /**
     * function to return sms and address in content provider for SmsHashedNumUploadWorker
     */
    @SuppressLint("LongLogTag")
    suspend fun fetchSmsForWorker(): MutableList<SMS>  = withContext(Dispatchers.IO) {
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
                        //setSpannableStringBuilder(objSMS, null, msg, num)
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


        return@withContext data
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
                            var num =
                                cursor.getString(cursor.getColumnIndexOrThrow("address"))
                            num = num.replace("+", "")
                            //                    objSMS.address = num

                            objSMS.type =
                                cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                            var msg =
                                cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            //

                            //setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                            // spannable string builder function to setup spannable string builder
                            objSMS.addressString = num.replace("+", "")

                            objSMS.readState =
                                cursor.getInt(cursor.getColumnIndex("read"))
                            val dateMilli =
                                cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            objSMS.time = dateMilli
                          smsRepositoryHelper.setRelativeTime(objSMS, dateMilli)

                            if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                    .contains("1")
                            ) {
                                objSMS.folderName = "inbox"
                            } else {
                                objSMS.folderName = "sent"
                            }
                            getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS).apply {
                                if(this!=null){
                                    objSMS.firstNameFromServer = this?.firstName
                                    objSMS.lastNameFromServer = this.lastName
                                    objSMS.spamCount  = this.spamReportCount
                                    objSMS.spammerType = this.spammerType
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
                            var num =
                                cursor.getString(cursor.getColumnIndexOrThrow("address"))
                            num = num.replace("+", "")
                            objSMS.type =
                                cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                            var msg =
                                cursor.getString(cursor.getColumnIndexOrThrow("body"))
                            objSMS.msgString = msg
                            objSMS.addressString = num.replace("+", "")
                            objSMS.addressString = formatPhoneNumber(num)
                            objSMS.nameForDisplay = objSMS.addressString!!

                            objSMS.readState =
                                cursor.getInt(cursor.getColumnIndex("read"))
                            val dateMilli =
                                cursor.getLong(cursor.getColumnIndexOrThrow("date"))

                            objSMS.time = dateMilli
                            smsRepositoryHelper.setRelativeTime(objSMS, dateMilli)

                            if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                    .contains("1")
                            ) {
                                objSMS.folderName = "inbox"
                            } else {
                                objSMS.folderName = "sent"
                            }
                          val r = async { getDetailsFromDB(formatPhoneNumber(objSMS.addressString!!), objSMS)  }.await()
                                if(r!=null){
                                    objSMS.firstNameFromServer = r?.firstName
                                    objSMS.lastNameFromServer = r?.lastName
                                    objSMS.spamCount  = r.spamReportCount
                                    objSMS.spammerType = r.spammerType

                            }
                            listOfMessages.add(objSMS)
                        } catch (e: Exception) {
                            Log.d(TAG, "getSMSForSpammList: $e")
                        }

                    } while (cursor.moveToNext())
                    //                            })
                    //                        }


                }

                data.addAll(listOfMessages)
                scope.launch {
                    val r2 = async {  setNameIfExistInContactContentProvider(data) }
//                    r1.await()
                    r2.await()
                }.join()

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "getSMSForSpammList: exception $e")

            }
        }.join()
        return data
    }

    /**
     * Adding a new sms sender info who is a spammer
     */
    suspend fun markAsSpam(contactAddress: String)   = withContext(Dispatchers.IO){
       val formatedAddress = formatPhoneNumber(contactAddress)
        val res  = smsThreadsDAO?.find(formatedAddress)
        if(res!=null){
            var spamCount = res.spamCount
            spamCount+=1
            smsThreadsDAO?.updateSpamCount(formatedAddress, spamCount = spamCount )


        }

    }



    @SuppressLint("LongLogTag")
    suspend fun deleteSmsThread(id:Long): Int  = withContext(Dispatchers.IO) {
        smsDeletingStarted = true
        var numRowsDeleted = 0
//        var copy:MutableList<Long> = mutableListOf()
//        copy.addAll(markedThreadIds)
        try{
//            for(id in copy) {
                var uri = Telephony.Sms.CONTENT_URI
                val selection = "${Telephony.Sms.THREAD_ID} = ?"
                val selectionArgs = arrayOf(id.toString())
                try {
                    delay(800L).apply {
                    }

                } finally { // this is to slow down deleting by 800mlseconds to see user deleting happening
                    numRowsDeleted = context.contentResolver.delete(uri, selection, selectionArgs)


                }
//            }
        }catch (e: Exception) {
            Log.d(SMScontainerRepository.TAG, "deleteSmsThread: exception $e")
        }finally {
            deleteList()
        }

        return@withContext numRowsDeleted
    }


    private fun deleteList() {
        markedThreadIds.clear()
    }

    /***
     * function to add contact address to muted_senders table,
     * no notification for incoming sms from muted senders
     */
    suspend fun muteSenders()   = withContext(Dispatchers.IO){
        var addressList: MutableList<MutedSenders> = mutableListOf()
//        for (address in MarkedItemsHandler.markedContactAddress){
//            val mutedSender = MutedSenders(formatPhoneNumber(address))
//            addressList.add(mutedSender)
//        }
        mutedSendersDAO!!.insert(addressList)
    }

    suspend fun deleteAllSMmsendersINo()  = withContext(Dispatchers.IO) {
        callerInfoDAO!!.deleteAll()
        smsThreadsDAO?.deleteAll()
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

                    val days = smsRepositoryHelper.getDaysDifference(t)
                    if(days == 0L){
                        currentDate = "Today"
                    }else if(days == 1L){
                        currentDate = "Yesterday"
                    }

                    val sms = SMS()
                    sms.time = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                    sms.timeString = smsRepositoryHelper.setHourAndMinute(sms, sms.time!!)
                    sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))
                    val mgsStr = sms.msgString
                    sms.id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("_id"))
                    sms.threadID = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))

                    sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
                    sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                    sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))


                    if(IndividualSMSActivity.chatId.isNotEmpty()){
                        if(sms.id.toString() == IndividualSMSActivity.chatId){
//                            val startPos = sms.msgString!!.indexOf(queryText!!)
//                            val endPos = startPos + queryText!!.length

                            val yellow = BackgroundColorSpan(Color.YELLOW)
                            val spannableStringBuilder =
                                SpannableStringBuilder(mgsStr)

                            spannableStringBuilder.setSpan(
                                yellow,
                                0,
                                0,
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
                    smsRepositoryHelper.setRelativeTime(objSMS, dateMilli)

                    if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                            .contains("1")
                    ) {
                        objSMS.folderName = "inbox"
                    } else {
                        objSMS.folderName = "sent"

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
    suspend fun getContactInfoFRomDB(pno: String): String?   = withContext(Dispatchers.IO){
//        val numWithoutSpecialChars = replaceSpecialChars(pno)
        var result : CallersInfoFromServer? = null
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
        result = callerInfoDAO!!.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(pno), countryISO))
            if(result!=null){
                name = result!!.firstName
            }
        return@withContext name
    }

    suspend fun saveSpamReportedByUser(contactAddress: String, threadID: Long, spammerType: Int?)  = withContext(Dispatchers.IO) {
        val formatedAddress = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryISO)
        callerInfoDAO!!.find(formatedAddress).apply {
            if(this!=null){
                //already infor exists
                    val spamcount = this.spamReportCount +1
                callerInfoDAO!!.update(spamcount, formatedAddress, true)
            }else{
//                callerInfoDAO.insert(listOf(CallersInfoFromServer(
//                    formatedAddress,
//                    spammerType!!,
//                    " ","Date()", Date(), 1000 )))
            }
        }
    }

    suspend fun insertIntoThreadsDb(smsChatsFromCprovider: MutableList<SmsThreadTable>)  = withContext(Dispatchers.IO){
        smsThreadsDAO?.insert(smsChatsFromCprovider)
    }

    fun getSMSThreadsLivedata(): LiveData<MutableList<SmsThreadTable>>?  {
        return smsThreadsDAO?.getAllLiveData()
    }

   suspend fun getNameForAddressFromContentProvider(contactAddress: String): NameAndThumbnail? = withContext(Dispatchers.IO) {
        var name:String?
       var thumbnail:String?
       var nameAndThumbnail: NameAndThumbnail? = null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactAddress));

        val cursor2 = context.contentResolver.query(uri, null,  null, null, null )
        if(cursor2!=null && cursor2.moveToFirst()){
            name = cursor2.getString(cursor2.getColumnIndexOrThrow("display_name"))
            thumbnail = cursor2.getString(cursor2.getColumnIndexOrThrow( ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
            nameAndThumbnail = NameAndThumbnail(name?:"", thumbnail?:"")
        }
        return@withContext nameAndThumbnail
    }

    suspend fun markAsDelete(threadId: Long)   = withContext(Dispatchers.IO){
        smsThreadsDAO?.markAsDeleted(threadId, isDeleted = true)
//        delay(400L)

    }

    /**
     * function to delete threads id from db that are not in content provider
     * @param smsFromContentProvider all sms chatthreads in content provider
     */
    suspend fun deleteFromDb(smsFromContentProvider: MutableList<SmsThreadTable>)  = withContext(Dispatchers.IO){
        var idsFromContentPovider : MutableList<Long> = mutableListOf()
        idsFromContentPovider.addAll(smsFromContentProvider.map { it.threadId})

        var idsFromChatThreadsTable : MutableList<Long> = mutableListOf()
        smsThreadsDAO?.getAll().apply {
            this?.map { it.threadId}?.let { idsFromChatThreadsTable.addAll(it) }

            val threadidsTobeRemoved = idsFromChatThreadsTable - idsFromContentPovider

            for(thredId in threadidsTobeRemoved){
                smsThreadsDAO?.delete(thredId)
            }
        }


    }

    suspend fun updateThreadsDBWithServerInfo(item: CallersInfoFromServer) = withContext(Dispatchers.IO) {
        smsThreadsDAO?.updateWithServerInfo(
            libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(item.contactAddress), countryISO),
            item.spamReportCount,
            item.firstName,
            item.lastName,
            item.thumbnailImg
            )
    }

    /**
     * function to upate thread's content such as body , read, type, date
     */
    suspend fun updateThreadContent(smsFromCprovider: MutableList<SmsThreadTable>)  = withContext(Dispatchers.IO){
        for(item in smsFromCprovider){
            smsThreadsDAO?.find(contactAddress = item.numFormated).apply {
                if(this!=null){
                    smsThreadsDAO?.updateBodyAndContents(item.numFormated,
                        item.body,  item.dateInMilliseconds )

                }
            }
        }
    }

    suspend fun getSenderInfoFromServerForAddres(contactAddress: String): CallersInfoFromServer? = withContext(Dispatchers.IO) {
        val num = formatPhoneNumber(contactAddress)
         callerInfoDAO?.find(libPhoneCodeHelper.getES164Formatednumber(num, countryISO)).apply {
             return@withContext this
        }
    }

    /**
     *  function to get smsthreads details from db
     */
    suspend fun getThreadInfo(contactAddress: String): SmsThreadTable?  = withContext(Dispatchers.IO) {
        val fnum = formatPhoneNumber(contactAddress)
        return@withContext smsThreadsDAO?.find(fnum)
    }

    /**
     * function to upate name, nameFrom server, spamcount
     */
    suspend fun updateThreadSpamCount(item: SmsThreadTable) = withContext(Dispatchers.IO) {
        smsThreadsDAO?.updateInfos(item.numFormated, item.spamCount, item.firstName, item.firstNameFromServer, item.thumbnailFromCp)
    }

    /**
     * called from smssearch activity
     */
    suspend fun searchForSMS(searchQuery: String?): MutableList<SMS> = withContext(Dispatchers.IO){
        var data = ArrayList<SMS>()
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


            //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
            if (cursor != null && cursor.moveToFirst()) {
                //                    val spammersList = spamListDAO?.getAll()
                do {

                    try {
                        //TODO check if phone number exists in contact, if then add the contact information too
                        val objSMS = SMS()
//                        objSMS.id =
//                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadID =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                        objSMS.id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.addressString =
                            cursor.getString(cursor.getColumnIndexOrThrow("address"))

                        objSMS.type =
                            cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                        var msg =
                            cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        objSMS.body = msg

//                        setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                        // spannable string builder function to setup spannable string builder
//                        objSMS.addressString = formatPhoneNumber(num)
//                        objSMS.nameForDisplay = objSMS.addressString!!

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


//                        setRelativeTime(objSMS, dateMilli)
                        objSMS.spamCount = 0
                        objSMS.firstName = ""
                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                        } else {
                            objSMS.folderName = "sent"
                        }

//                          val r =  async {  getDetailsFromDB(replaceSpecialChars(objSMS.addressString!!), objSMS) }.await()
//                                if(r!=null){
//                                    objSMS.name = r?.name
//                                    objSMS.spamCount  = r.spamReportCount
//                                    objSMS.spammerType = r.spammerType
//                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                                }
//
                            getDetailsFromDB(objSMS.addressString!!, objSMS).apply {
                                if(this!=null){
                                    objSMS.firstNameFromServer = this?.firstName
                                    objSMS.spamCount  = this.spamReportCount
                                    objSMS.spammerType = this.spammerType
                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
                                }
                            }

                        objSMS.firstName = getNameForNumberFromCprovider(objSMS.addressString!!)

                        setSpannableStringBuilder(objSMS, searchQuery, objSMS.body, objSMS.addressString!!)


//                        if(!objSMS.msgString.isNullOrEmpty()){
////                                setSMSHashMap(objSMS)
//
//                        }
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

            data.addAll(listOfMessages)


//                setNameIfExistInContactContentProvider(data)
//                removeDeletedMSSFRomhashMap(setOfAddress)



        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetch: exception $e")
        }finally {
            cursor?.close()
        }
//        }
//        r1.await()

        return@withContext data
    }

    suspend fun getInfoFromThreadDbForQuery(searchQuery: String?): List<SmsThreadTable>?   = withContext(Dispatchers.IO){
        return@withContext smsThreadsDAO?.findNameLike("%$searchQuery%")
    }

    /**
     * function called when user searched for name, so exact number is given and in the result
     * spannable string will be the name containing searchquery
     */
    suspend fun getSMSForAddress(numForSearching: String, searchQuery: String): ArrayList<SMS>  = withContext(Dispatchers.IO) {
        //todo if found result set spannable string to address and if body also contains set it also
        var data = ArrayList<SMS>()
        var prevAddress = ""
        var prevTime = 0L
//       val r1= GlobalScope.async {
        val cursor = createCursor(numForSearching)
        try {

            var deleteViewAdded = false
            val listOfMessages = mutableListOf<SMS>()
            var setOfAddress:MutableSet<String> = mutableSetOf()
            var count = 0
            var map: HashMap<String?, String?> = HashMap()
            smsListHashMap = map

            //        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC


            //https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
            if (cursor != null && cursor.moveToFirst()) {
                //                    val spammersList = spamListDAO?.getAll()
                do {

                    try {
                        //TODO check if phone number exists in contact, if then add the contact information too
                        val objSMS = SMS()
//                        objSMS.id =
//                            cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                        objSMS.threadID =
                            cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))
                        objSMS.id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))

                        objSMS.addressString =
                            cursor.getString(cursor.getColumnIndexOrThrow("address"))

                        objSMS.type =
                            cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                        var msg =
                            cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        objSMS.body = msg

//                        setSpannableStringBuilder(objSMS, searchQuery, msg, num) //calling
                        // spannable string builder function to setup spannable string builder
//                        objSMS.addressString = formatPhoneNumber(num)
//                        objSMS.nameForDisplay = objSMS.addressString!!

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


//                        setRelativeTime(objSMS, dateMilli)
                        objSMS.spamCount = 0
                        objSMS.firstName = ""
                        if (cursor.getString(cursor.getColumnIndexOrThrow("type"))
                                .contains("1")
                        ) {
                            objSMS.folderName = "inbox"
                        } else {
                            objSMS.folderName = "sent"

                        }

//                          val r =  async {  getDetailsFromDB(replaceSpecialChars(objSMS.addressString!!), objSMS) }.await()
//                                if(r!=null){
//                                    objSMS.name = r?.name
//                                    objSMS.spamCount  = r.spamReportCount
//                                    objSMS.spammerType = r.spammerType
//                                    objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                                }
//
                        getDetailsFromDB(objSMS.addressString!!, objSMS).apply {
                            if(this!=null){
                                objSMS.firstNameFromServer = this?.firstName
                                objSMS.spamCount  = this.spamReportCount
                                objSMS.spammerType = this.spammerType
                                objSMS.senderInfoFoundFrom = SENDER_INFO_FROM_DB
                            }
                        }
                        objSMS.firstName = getNameForNumberFromCprovider(objSMS.addressString!!)
                        setSpannableStringBuilder(objSMS, numForSearching, objSMS.body, objSMS.addressString!!)
                        setSpannableStrinForName(objSMS, searchQuery)

//                        if(!objSMS.msgString.isNullOrEmpty()){
////                                setSMSHashMap(objSMS)
//
//                        }
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

            data.addAll(listOfMessages)


//                setNameIfExistInContactContentProvider(data)
//                removeDeletedMSSFRomhashMap(setOfAddress)



        } catch (e: java.lang.Exception) {
            Log.d(TAG, "fetch: exception $e")
        }finally {
            cursor?.close()
        }
//        }
//        r1.await()

        return@withContext data
    }

    suspend fun findOneThreadById(id: Long): SmsThreadTable?  = withContext(Dispatchers.IO) {
        return@withContext smsThreadsDAO?.findOneById(id)
    }

    suspend fun getAllSmsThreads(): List<SmsThreadTable>? = withContext(Dispatchers.IO) {

        return@withContext smsThreadsDAO?.getAll()
    }

    suspend fun getServerInfoForNumber(numFormated: String): CallersInfoFromServer?  = withContext(Dispatchers.IO){
        return@withContext callerInfoDAO?.find(libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(numFormated), countryISO))
    }

    suspend fun updateChatThreadWithContentProviderInfo(infoFromCprovider: Contact) = withContext(Dispatchers.IO) {
        smsThreadsDAO!!.updateWithContentProviderInfo(infoFromCprovider.firstName, infoFromCprovider.thumbnailInCprovider, infoFromCprovider.phoneNumber)
    }

    suspend fun marAsReportedByUserInCall(contactAddress: String)  = withContext(Dispatchers.IO){
        val formatedAdders = libPhoneCodeHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryISO)
//        val log =  callLogDAO?.findOne(formatedAdders)
//        if(log!=null){
//            var spamCount = log.spamCount
//            spamCount += 1
            callLogDAO?.markAsReportedByUser(formatedAdders, 1)
//        }
    }


}

