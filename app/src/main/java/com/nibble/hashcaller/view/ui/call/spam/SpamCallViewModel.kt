package com.nibble.hashcaller.view.ui.call.spam

import android.provider.CallLog
import androidx.lifecycle.*
import com.nibble.hashcaller.view.ui.call.db.CallLogTable

/**
 * Created by Jithin KG on 22,July,2020
 */
class SpamCallViewModel(private val repository: SpamCallRepository) : ViewModel() {

    var livedata: LiveData<MutableList<CallLogTable>> = repository.getCallLogLivedata()
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