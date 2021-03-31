package com.nibble.hashcaller.view.ui.sms.list

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.contacts.utils.isNumericOnlyString
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.util.*
import com.nibble.hashcaller.work.formatPhoneNumber
import com.nibble.hashcaller.work.replaceSpecialChars
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SMSHelperFlow(private val context: Context) {
    val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
    suspend fun fetchFlowSMS(): MutableList<SMS>  {
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
            "thread_id",
            "_id",
            "address",
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
                "_id DESC LIMIT 10"
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
                    objSMS.addressString = num.replace("+", "")
                    objSMS.addressString = replaceSpecialChars(num)
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

                    getDetailsFromDB(replaceSpecialChars(objSMS.addressString!!), objSMS).apply {
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
                    setSMSReadStatus(objSMS)
                    setNameIfExistInContactContentProvider(objSMS)
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

    /**
     * function to get information from local db sms_senders_info_from_db
     * @param formattedNum , phone number
     */
    private suspend fun getDetailsFromDB(
        num: String,
        sms: SMS
    ): SMSSendersInfoFromServer?  {
        var r: SMSSendersInfoFromServer? = null
//        val frmtedNum = replaceSpecialChars(num)
        GlobalScope.launch {
            r = async {  smssendersInfoDAO!!.find(num) }.await()
        }.join()
        return r

    }

    private fun setNameIfExistInContactContentProvider(sms: SMS) {

            if(sms.addressString != null){
                var formattedNum = formatPhoneNumber(sms.addressString!!)
                if(isNumericOnlyString(formattedNum)){
                    val name =   getConactInfoForNumber(formattedNum)
                    if (name != null){
                        sms.name = name
                        sms.nameForDisplay = name
                        sms.senderInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                    }
                }

            }



    }


    /**
     * function to get contact info for numbers
     * @param pno phone number
     */
    fun getConactInfoForNumber( pno: String): String? {
        var cursor: Cursor? = null
        var name:String? = null
        val phoneNum = pno.replace("+", "").trim()
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(pno));

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
     * Function to check whether the current message is opened/readed by the user
     */
    private fun setSMSReadStatus(
        sms: SMS ) {


        Log.d("__time", "setSMSReadStatus: called")


            if (sms.readState == 0)
                setCount(sms)

    }
    private  fun setCount(sms: SMS) {
        val addressString = sms.addressString
        var cnt:Int? = 0

        val cursorSMSCount = context.contentResolver.query(
            SMSContract.INBOX_SMS_URI,
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

    @SuppressLint("LongLogTag")
    fun setSMSHashMap(objSMS: SMS) {
//        if(!objSMS.addressString.isNullOrEmpty()){
//            Log.d(TAG, "setSMSHashMap: ")
//
////            val mr = SMSContainerFragment.mapofAddressAndSMS[objSMS.addressString!!]
//            if(mr==null){
//                SMSContainerFragment.mapofAddressAndSMS[objSMS.addressString!!] = objSMS
//            }else{
//                val timFromMap = mr.time!!.toLong()
//                val timeFromCProvider = objSMS.time!!.toLong()
//                if( timFromMap < timeFromCProvider){
//                    //new message is objsms.time
//                    Log.d(TAG +"setSMSHashMaptS", " lesser map: $timFromMap cp: $timeFromCProvider")
//                    SMSContainerFragment.mapofAddressAndSMS.put(objSMS.addressString!!, objSMS)
//                }else{
//                    Log.d(TAG, "setSMSHashMap: greater")
//                }
//            }
//        }

    }

    companion object{
        const val TAG = "__SMSHelperFlow"
    }
}