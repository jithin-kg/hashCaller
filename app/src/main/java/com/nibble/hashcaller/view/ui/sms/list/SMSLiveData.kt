package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSLiveData(private val context: Context,private val repository: SMSLocalRepository?):
    ContentProviderLiveData<MutableList<SmsThreadTable>>(
        context,
        URI
    ) {

//    private lateinit var spamListDAO:SpamListDAO
//    private lateinit var smssendersInfoDAO : SMSSendersInfoFromServerDAO
//    private lateinit var mutedSendersDAO:IMutedSendersDAO
//    private lateinit var smsThreadsDAO:ISMSThreadsDAO
    var isLoading:MutableLiveData<Boolean> = MutableLiveData(true)

    companion object {
        //        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =
            SMSContract.INBOX_SMS_URI
        private const val TAG = "__SMSLiveDataFlow"
    }
     private suspend fun getMessages(context: Context): ArrayList<SmsThreadTable> {
         pageOb.page = 0 //set page size to 0 when there is a change in sms

//        SMSViewModel.isLoading.postValue(true)


          repository!!.fetchSMSForLivedata(null, false).apply {
//              Log.d(TAG, "getMessages: spamcount ${this[0].spamCount}")
              return this
              
          }



        //IMPORTANT from backgroudn thread we need to call postValue to set livedata


    }

    private suspend fun getSMS(repository: SMSLocalRepository):Flow<SMS> = flow{
                 repository.fetchFlowSMS().collect {
                    emit(it)
        }
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
//        val repository =
//            SMSLocalRepository(
//                context,
//                spamListDAO,
//                smssendersInfoDAO,
//                mutedSendersDAO,
//                smsThreadsDAO
//            )

        repository?.markSMSAsRead(address)
    }

    override suspend fun getContentProviderValue(searchText:String?) = getMessages(context)
//    fun getSms(){
//
//    }
}