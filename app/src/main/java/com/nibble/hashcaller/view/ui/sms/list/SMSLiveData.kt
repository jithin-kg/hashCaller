package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSLiveData(private val context: Context):
    ContentProviderLiveData<MutableList<SMS>>(context,
        URI
    )  {

    private lateinit var spamListDAO:SpamListDAO
    private lateinit var smssendersInfoDAO : SMSSendersInfoFromServerDAO

    var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)

    companion object {
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =
            SMSContract.INBOX_SMS_URI
        private const val TAG = "__MessagesLiveData"
    }
     private suspend fun getMessages(context: Context): MutableList<SMS> {

        SMSViewModel.isLoading.postValue(true)
          spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
         smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
         val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }

         val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO
            )
        val res =  repository.fetchSMS(null)

        //IMPORTANT from backgroudn thread we need to call postValue to set livedata
        return res


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
        val repository =
            SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO
            )
        repository.update(address)
    }

    override suspend fun getContentProviderValue(searchText:String?) = getMessages(context)
//    fun getSms(){
//
//    }
}