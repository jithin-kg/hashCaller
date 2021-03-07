package com.nibble.hashcaller.view.ui.sms.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualContactLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.isNumericOnlyString

import com.nibble.hashcaller.view.ui.contacts.utils.pageOb.page
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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
    val smssendersInfoDAO: SMSSendersInfoFromServerDAO?
){
    private var smsListHashMap:HashMap<String?, String?> = HashMap<String?, String?>()

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
        return fetch(null, isrequestingFromSmsSpamList)
    }

    //this function fetches sms while searching
    suspend fun getSms(searchQuery: String?): MutableList<SMS> {

        return fetch(searchQuery, false)
    }

    fun update(addressString: String){
        val cValues = ContentValues().apply {
            put("read", 1)

        }
        context.contentResolver.update(URI,cValues, "address='$addressString'",null)

    }
    @SuppressLint("LongLogTag")
    private suspend fun fetch(searchQuery: String?, requestinfromSpamlistFragment: Boolean?): MutableList<SMS> {
        var data = ArrayList<SMS>()

        val timeTook = measureTimeMillis {
            try {


                var deleteViewAdded = false
                val listOfMessages = mutableListOf<SMS>()
                var selectionArgs: Array<String>? = null
                var selection: String? = null
                var count = 0
                var map: HashMap<String?, String?> = HashMap()
                smsListHashMap = map

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

//        SELECT _id, DISTINCT thread_id, address, type, body, read, date FROM sms WHERE (thread_id IS NOT NULL) GROUP BY (thread_id ) ORDER BY date DESC
                val cursor = context.contentResolver.query(
                    SMSContract.ALL_SMS_URI,
                    projection,
                    "address IS NOT NULL) GROUP BY (address",
                    selectionArgs,
                    "_id DESC limit 12 offset $page"
                )
                Log.d(TAG, "fetch: page is   $page")
//https://stackoverflow.com/questions/2315203/android-distinct-and-groupby-in-contentresolver
                if (cursor != null && cursor.moveToFirst()) {
                    val spammersList = spamListDAO?.getAll()

//            if (spammersList != null) {
//                for (spamer in spammersList){
//                    this.smsListHashMap.put(spamer.contactAddress, spamer.id.toString())
//                }
//            }
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

                            val msg =
                                cursor.getString(cursor.getColumnIndexOrThrow("body"))
//                    val fromAddress =cursor.getColumnIndexOrThrow("from_address")
                            //todo if the same number in server have lesser spam count than this value update server count
                            //else vice versa

//                    val spamReport = cursor.getColumnIndexOrThrow("spam_report") //her I get spam count
//                    val res1= cursor.getColumnName(spamReport)
//                    val res2 = cursor.getLong(spamReport)
//                   val count2 =  cursor.getLong(spamReport)

//                    val protocol = cursor.getColumnIndexOrThrow("protocol")
//                    val read = cursor.getColumnIndexOrThrow("read")
//                    val resstatus = cursor.getInt(read)
//                    val data_sent = cursor.getColumnIndexOrThrow("date_sent")
//                    val status = cursor.getColumnIndexOrThrow("status")
//                    val service_center = cursor.getColumnIndexOrThrow("service_center")
//                    val servicecenterString = cursor.getString(service_center)
//
//                    val error_code = cursor.getColumnIndexOrThrow("error_code")
//                    val seen = cursor.getColumnIndexOrThrow("seen")
//                    val seenString = cursor.getString(seen)


                            var spannableStringBuilder: SpannableStringBuilder?

                            if (searchQuery != null) {
                                val lowercaseMsg = msg.toLowerCase()
                                val lowerSearchQuery = searchQuery.toLowerCase()

                                if (lowercaseMsg.contains(lowerSearchQuery) && searchQuery.isNotEmpty()) {
                                    val startPos =
                                        lowercaseMsg.indexOf(lowerSearchQuery)
                                    val endPos = startPos + lowerSearchQuery.length
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

                            objSMS.addressString = num.replace("+", "")


//                    val count =setSMSReadStatus(objSMS, objSMS.addressString!!)
//                    objSMS.unReadSMSCount = count!!


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

                            if (requestinfromSpamlistFragment!!) {
                                //if we are requesting from fragment SMSIdentifiedAsSpamFragment
                                if (smsListHashMap.containsKey(objSMS.addressString)) {
                                    if (!deleteViewAdded) {
                                        val delViewObj = SMS()
                                        delViewObj.deleteViewPresent = true
                                        listOfMessages.add(delViewObj)
                                        deleteViewAdded = true
                                    }
                                    listOfMessages.add(objSMS)
                                }
                            } else {
                                //we are requesting from SMSListFragment
                                if (!smsListHashMap.containsKey(objSMS.addressString)) {
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
                GlobalScope.launch {
                   val r1 =  async {  setSMSReadStatus(data) }
//        setSpamDetails(data)
                    val r2 = async {  setNameIfExistInContactContentProvider(data) }
                    r1.await()
                    r2.await()
                }.join()

            } catch (e: java.lang.Exception) {
                Log.d(TAG, "fetch: exception $e")
            }


        }
        Log.d(TAG, "fetch: size of list is ${data.size}")
        if(data.size >= 1)
        Log.d(TAG, "fetch: first item msg is  ${data[0].msg}")
        return data
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
            var formattedNum = formatPhoneNumber(sms.addressString!!)

            if(isNumericOnlyString(formattedNum)){
                val name =   getConactInfoForNumber(formattedNum)
                if (name != null){
                    sms.name = name
                }else{

                    getDetailsFromDB(formattedNum, sms)
                }
            }else{
                getDetailsFromDB(formattedNum, sms)
            }

        }

    }

    /**
     * function to get information from local db sms_senders_info_from_db
     * @param formattedNum , phone number
     */
    private fun getDetailsFromDB(
        num: String,
        sms: SMS
    ) {
        val res = smssendersInfoDAO!!.find(num!!)
        if(res!=null){
            sms.name = res?.name
            sms.spamCount  = res.spamReportCount
            sms.spammerType = res.spammerType
        }
    }

    private fun setContactName(sms: SMS) {
        var c: Contact = Contact(1L, "", "sample phone num", "", "")
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

                        c = Contact(id, name, photoURI, "photoThumnail", photoURI)

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
    private suspend fun setSMSReadStatus(
        smsList: ArrayList<SMS> ) {

        Log.d("__time", "setSMSReadStatus: called")

        for (sms in smsList) {
            if (sms.readState == 0)

                setCount(sms)

        }

    }

    private suspend fun setCount(sms: SMS) {
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

    fun fetchIndividualSMS(contact: String?): List<SMS> {
        var count = 0
        var prevDate = ""
        var selectionArgs: Array<String>? = null
        selectionArgs = arrayOf(contact!!)
        var smslist = mutableListOf<SMS>()

        val cursor = context.contentResolver.query(
            SMSContract.ALL_SMS_URI,
            null,
            SMSContract.SMS_SELECTION,
            selectionArgs,
            SMSContract.SORT_ASC
        )
        if(cursor != null && cursor.moveToFirst()) {
            do {
                count++
                val smsWithCurrentDate = SMS()
                smsWithCurrentDate.type = -1 // cause this is just date, ie does not belong to any
                //sms type so I can filer this from adapter
                val t = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date(t))


                if(currentDate != prevDate){
                    //for the first time add the date
                    //and if sms from different dates are in inbox then show date accoringly
                    prevDate = currentDate
                    smsWithCurrentDate.currentDate = prevDate
                    smslist.add(smsWithCurrentDate)


                }
                val sms = SMS()
                sms.time = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("date"))
                sms.msgString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("body"))
                sms.id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow("_id"))
                sms.threadID = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))

                sms.addressString = cursor!!.getString(cursor!!.getColumnIndexOrThrow("address"))
                sms.msgType = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                sms.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))

                smslist.add(sms)
                try {

                } catch (e: java.lang.Exception) {
                    Log.d(TAG, "fetchIndividualSMS: $e")
                }
            } while (cursor.moveToNext())

        }

        cursor?.close()
        Log.d(TAG, "fetchIndividualSMS: sizeL${smslist.size}, count:$count")
//        update(contact)
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
    fun getInfoFromLocalDb(smslist: List<SMS>) {
        for(sms in smslist){
            Log.d(TAG, "getInfoFromLocalDb: ")
            //replace all special character and search in db
            var num = sms.addressString
            var res:SMSSendersInfoFromServer? = null
            num = formatPhoneNumber(num!!)
            res = smssendersInfoDAO!!.find(num!!)
            if(sms.name.isNullOrEmpty()){
                sms.name = res?.name
                Log.d(TAG, "getInfoFromLocalDb:  empty ")

            }else{
                Log.d(TAG, "getInfoFromLocalDb: not empty ")
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
        smssendersInfoDAO!!.find(num!!)
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

//
                            listOfMessages.add(objSMS)

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

}

