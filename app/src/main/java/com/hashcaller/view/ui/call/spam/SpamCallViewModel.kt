package com.hashcaller.view.ui.call.spam

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.view.ui.call.db.CallLogTable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created by Jithin KG on 22,July,2020
 */
class SpamCallViewModel(private val repository: SpamCallRepository) : ViewModel() {


    @SuppressLint("LongLogTag")
    fun delete(logAt: CallLogTable?) = viewModelScope.launch {
        logAt?.id?.let {

          val def1=  async { repository.markAsDeleted(logAt.numberFormated) }
           val def2 =  async { repository.deleteCallLogsFromCp(logAt.number) }
           try {
               def1.await()
               def2.await()
           }catch (e:Exception){
               Log.d(TAG, "delete: exception $e")
           }
        }

    }

    var spamCalllivedata: LiveData<MutableList<CallLogTable>> = repository.getSpamCallLogLivedata()
    val markeditemsHelper = MarkeditemsHelper()

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