package com.nibble.hashcaller.view.ui.sms.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.sms.search.SmsSearchQueries
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.async
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
var searchResultLivedata:MutableLiveData<MutableList<SMS>> = MutableLiveData()


    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }






     fun search(searchQuery: String?) = viewModelScope.launch {
    //getsms previously called
           val  res =   async { repository!!.searchForSMS(searchQuery) }.await()
             if(res.size>0){
//                 emit(res)
                 searchResultLivedata.value = res
             }else {
                 //if there is no result then, the search query can be a name
                 //eg if samuel exists in contacts and user searched for `am`, then get all the contact address
                     //containing name `am`
                 searchQuery.let {

                    val res = async {  repository?.getInfoFromThreadDbForQuery(searchQuery) }.await()
                     //if we got name sam, jamy, samson, american, we have to search for sms of these
                     //names currespondin numbers
                     if(res!=null){
                         var listOfResults : MutableList<SMS> = mutableListOf()
                        for(item in res ){
                            val searchResult = async { repository?.getSMSForAddress(item.contactAddress) }.await()
                            searchResult.let {
                                if(it!=null){
                                   listOfResults.addAll(it)
                                }
                            }
                        }
                         searchResultLivedata.value = listOfResults
                     }

                 }
             }



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