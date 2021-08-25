package com.hashcaller.app.view.ui.sms.spam

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created by Jithin KG on 22,July,2020
 */
class SpamSMSViewModel(private val repository: SpamSMSRepository) : ViewModel() {


    @SuppressLint("LongLogTag")
    fun delete(smsThread: SmsThreadTable?): LiveData<Int> = liveData {

        viewModelScope.launch {
            smsThread?.threadId.let {

                val def1=  async { smsThread?.threadId?.let { it1 -> repository.markAsDeleted(it1) } }
                val def2 =  async { smsThread?.let { it1 -> repository.deleteSMSsFromCp(it1.threadId) } }

                try {
                    def1.await()
                    def2.await()
                }catch (e:Exception){
                    Log.d(TAG, "delete: exception $e")
                }
//

            }
        }.join()
        emit(OPERATION_COMPLETED)




    }

    var spamSMSLivedata: LiveData<MutableList<SmsThreadTable>> = repository.getSpamCallLogLivedata()

    companion object
    {
        private const val TAG ="__CallLogSearchViewModel"
    }





}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}