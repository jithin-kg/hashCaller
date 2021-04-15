package com.nibble.hashcaller.view.ui.call.search

import androidx.lifecycle.*
import com.nibble.hashcaller.view.ui.call.db.CallLogTable

/**
 * Created by Jithin KG on 22,July,2020
 */
class CallLogSearchViewModel(private val repository: CallLogSearchRepository) : ViewModel() {
    
    
    fun search(text: String): LiveData<MutableList<CallLogTable>> = liveData {
        emit(repository.search(text))
        
    }

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