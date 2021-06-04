package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.contacts.getAllSMSCursor
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSSpamLiveData(private val context: Context, lifecycleScope: LifecycleCoroutineScope):
    ContentProviderLiveData<MutableList<SMS>>(
        context,
        URI,
        lifecycleScope
    )  {
    private lateinit var spamListDAO:SpamListDAO
    private lateinit var smssendersInfoDAO:CallersInfoFromServerDAO
    private lateinit var mutedSendersDAO: IMutedSendersDAO
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)
    val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }
    private val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }

    companion object{
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =
            SMSContract.INBOX_SMS_URI
        private const val TAG = "__MessagesLiveData"
    }
     private suspend fun getMessages(context: Context): MutableList<SMS> {

        SMSSpamViewModel.isLoading.postValue(true)
          spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
         smssendersInfoDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
         mutedSendersDAO = HashCallerDatabase.getDatabaseInstance(context).mutedSendersDAO()
         val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }
        val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO,
                DataStoreRepository(context.tokeDataStore),
                TokenHelper( FirebaseAuth.getInstance().currentUser),
                callLogDAO,
                SmsRepositoryHelper(context),
                LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
                CountrycodeHelper(context).getCountryISO()

            )
//        val res =  repository.getSMSForViewModel(null, true)
            val res:MutableList<SMS> = mutableListOf()
        //IMPORTANT from backgroudn thread we need to call postValue to set livedata
        SMSSpamViewModel.isLoading.postValue(false)
        return res

//        val listOfMessages = mutableListOf<SMS>()
//        var data = ArrayList<SMS>()
//
//        val projection = arrayOf(
//            CallLog.Calls.NUMBER,
//            CallLog.Calls.TYPE,
//            CallLog.Calls.DURATION,
//            CallLog.Calls.CACHED_NAME,
//            CallLog.Calls._ID,
//            CallLog.Calls.DATE
//
//
//        )
//        var selectionArgs: Array<String>? = null
//
//        val cursor = context.contentResolver.query(
//            URI,
//            null,
//            null,
//            null,
//            SMSContract.SORT_DESC
//        )
//        if(cursor != null && cursor.moveToFirst()){
//            do{
//
//                try{
//                    //TODO check if phone number exists in contact, if then add the contact information too
//                    val objSMS = SMS()
//                    objSMS.id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
//                    val num = cursor.getString(cursor.getColumnIndexOrThrow("address"))
//                    objSMS.address = num
//                    objSMS.msg = cursor.getString(cursor.getColumnIndexOrThrow("body"))
//                    objSMS.readState = cursor.getString(cursor.getColumnIndex("read"))
//                    val dateMilli = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
//
//
//
//
//
//                    objSMS.time = dateMilli
//
//
//                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
//                        objSMS.folderName = "inbox"
//                    } else {
//                        objSMS.folderName = "sent"
//                    }
//
//                    listOfMessages.add(objSMS)
//                }catch (e:Exception){
//                    Log.d(TAG, "getMessages: $e")
//                }
//
//            }while (cursor.moveToNext())
//
//             data = sortAndSet(listOfMessages)
//            cursor.close()
//        }

//        return data

    }

    private fun convertEpochToStd(dateMilliString: Long): Int {
        val fmt = SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")

   val date =  SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date(dateMilliString * 1000))
        Log.d(TAG, "date  $date")
        val hour  = SimpleDateFormat("HH:mm:ss").format(Date(dateMilliString * 1000))
        Log.d(TAG, "hour: $hour")


        return 34

    }

    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }
    suspend fun update(address:String) = withContext(Dispatchers.IO){
        val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO,
                DataStoreRepository(context.tokeDataStore),
                TokenHelper( FirebaseAuth.getInstance().currentUser),
                callLogDAO,
                SmsRepositoryHelper(context),
                LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
                CountrycodeHelper(context).getCountryISO()
            )
        repository.markSMSAsRead(address)
    }

    override suspend fun getContentProviderValue(searchText:String?) = getMessages(context)
//    fun getSms(){
//
//    }
}