package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
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
    private lateinit var smssendersInfoDAO : SMSSendersInfoFromServerDAO
    private lateinit var mutedSendersDAO:IMutedSendersDAO
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)

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
         smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
         val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
          mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
         val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

         val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO
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
    fun update(address:String){
        val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO
            )
        repository.markSMSAsRead(address)
    }

    override suspend fun getContentProviderValue(searchText:String?) = getMessages(context)
//    fun getSms(){
//
//    }
}