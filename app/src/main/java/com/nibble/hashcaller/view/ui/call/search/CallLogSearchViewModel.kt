package com.nibble.hashcaller.view.ui.call.search

import androidx.lifecycle.*
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class CallLogSearchViewModel(private val repository: CallLogSearchRepository) : ViewModel() {
    
    var callLogs:MutableLiveData<MutableList<CallLogTable>> = MutableLiveData()


    fun search(text: String) = viewModelScope.launch {
//        emit(repository.search(text))
        var res:MutableList<CallLogTable> = mutableListOf()
          val r =    async { repository.search(text) }.await()
           callLogs.value = r

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