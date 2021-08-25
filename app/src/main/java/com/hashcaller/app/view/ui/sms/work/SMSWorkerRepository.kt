package com.hashcaller.app.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.hashcaller.app.view.ui.sms.util.SMS
import com.hashcaller.app.view.ui.sms.util.SMSContract
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to fetch all sms for uploading to server
 *
 */
class SMSWorkerRepository(
    private val context: Context,
    private val libCountryHelper: LibPhoneCodeHelper,
    private val countryCodeHelper: CountrycodeHelper
) {
    private val countryIso = countryCodeHelper.getCountryISO()

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

                        num = formatPhoneNumber(num)
                        num = libCountryHelper.getES164Formatednumber(num, countryIso)

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
    companion object {
        private const val TAG = "__SMSWorkerRepository"
    }
}