package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSContract
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSLiveData(
    private val context: Context,
    private val repository: SMSLocalRepository?,
    private val lifecycleScope: LifecycleCoroutineScope
):
    ContentProviderLiveData<MutableList<SmsThreadTable>>(
        context,
        URI,
        lifecycleScope
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
     private suspend fun getMessages(context: Context): ArrayList<SmsThreadTable> = withContext(Dispatchers.IO){

//        SMSViewModel.isLoading.postValue(true)


         return@withContext repository!!.fetchSMSForLivedata(null, false)



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
   suspend fun update(address:String) = withContext(Dispatchers.IO){
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