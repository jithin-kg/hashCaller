package com.nibble.hashcaller.view.ui.sms.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.sms.search.SmsSearchQueries
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSSearchViewModel(
    val repository: SMSLocalRepository?,
    val smsSearchRepository: SMSSearchRepository
): ViewModel() {
//    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive



    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }






     fun search(searchQuery: String?): LiveData<List<SMS>> = liveData<List<SMS>>{

     emit(repository!!.getSms(searchQuery))

    }
//    fun searchForIndividualSMS(text: String, contactAddress: String?): LiveData<List<SMS>> = liveData<List<SMS>> {
////        emit(repository!!.searchSmsForIndividualSMS(text, contactAddress))
//    }

    fun saveSearchQueryToDB(queryText: String) = viewModelScope.launch {
        smsSearchRepository.insertSearchQueryToDB(queryText)
    }

    fun getAllSearchHistory(): LiveData<List<SmsSearchQueries>> =
        liveData<List<SmsSearchQueries>> {
        emit(smsSearchRepository.getAllSearchHistory())

    }


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}