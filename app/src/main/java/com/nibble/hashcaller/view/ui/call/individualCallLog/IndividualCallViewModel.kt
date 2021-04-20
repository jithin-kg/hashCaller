package com.nibble.hashcaller.view.ui.call.individualCallLog

import androidx.lifecycle.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class IndividualCallViewModel(
    private val repository: IndividualCallhistoryRepository,
    val callLogLiveData: IndividualCallLivedata
) : ViewModel() {
    



    fun getCalllog(num: String?) {
//        repository.getCalllogByNumber(num)
    }

    companion object
    {
        private const val TAG ="__IndividualCallViewModel"
    }





}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}