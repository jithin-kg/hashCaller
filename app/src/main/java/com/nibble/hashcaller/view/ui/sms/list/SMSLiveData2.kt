package com.nibble.hashcaller.view.ui.sms.list

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
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.*
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSLiveData2(private val context: Context, private val scope: LifecycleCoroutineScope):
    ContentProviderLiveData<MutableList<SmsThreadTable>>(
        context,
        URI,
        scope
    )  {

    private lateinit var spamListDAO:SpamListDAO
    private lateinit var smssendersInfoDAO : CallersInfoFromServerDAO
    private lateinit var mutedSendersDAO:IMutedSendersDAO
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)
    private val callLogDAO = context?.let{HashCallerDatabase.getDatabaseInstance(it).callLogDAO()}



    companion object {
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =
            SMSContract.INBOX_SMS_URI
        private const val TAG = "__MessagesLiveData"
    }
     private suspend fun getMessages(context: Context): ArrayList<SmsThreadTable> {

         pageOb.page = 0 //set page size to 0 when there is a change in sms

        SMSViewModel.isLoading.postValue(true)
          spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
         smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
          mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
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
                SmsRepositoryHelper(context.getAllSMSCursor()),
                LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
                CountrycodeHelper(context).getCountryISO()

            )
         repository.fetchSMSForLivedata(null, false).apply {
            return this
        }

        //IMPORTANT from backgroudn thread we need to call postValue to set livedata


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
    suspend fun update(address:String){
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
                SmsRepositoryHelper(context.getAllSMSCursor()),
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