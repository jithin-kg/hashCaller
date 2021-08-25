package com.hashcaller.app.view.ui.sms.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.hashcaller.app.local.db.sms.search.SmsSearchQueries
import com.hashcaller.app.view.ui.sms.util.SMS
import com.hashcaller.app.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSSearchViewModel(
    val repository: SMSLocalRepository?,
    val smsSearchRepository: SMSSearchRepository
): ViewModel() {
//    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var searchResultLivedata:MutableLiveData<SearchResultAndQueryTerm> = MutableLiveData()
    var resultGotForQuery = ""

    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }






     fun search(searchQuery: String?) = viewModelScope.launch {
    //getsms previously called

         searchQuery.let {
             if (searchQuery != null) {
                 val  def1=   async { repository!!.searchForSMS(searchQuery) }
//                 emit(res)
                 val def2 = async { getSMSWithNameContainingSearchQuery(searchQuery) }
                 var list:MutableList<SMS> = mutableListOf()
                 list.addAll(def1.await().union(def2.await()))
                 searchResultLivedata.value = SearchResultAndQueryTerm(list, searchQuery)


             }

         }







    }

    /**
     * function to get contact number like %searchquery name % and get the
     * number of that name and query for sms
     * eg @param searchQuery am -> get all contact address with name containing %am%
     * query all sms for that address and show with search result
     */
    private suspend fun getSMSWithNameContainingSearchQuery(searchQuery: String?) : MutableList<SMS>{
        var listOfResults : MutableList<SMS> = mutableListOf()
        viewModelScope.launch {
            //if there is no result then, the search query can be a name
            //eg if samuel exists in contacts and user searched for `am`, then get all the contact address
            //containing name `am`


            val res = async {  repository?.getInfoFromThreadDbForQuery(searchQuery) }.await()
            //if we got name sam, jamy, samson, american, we have to search for sms of these
            //names currespondin numbers
            if(res!=null){
                for(item in res ){
                    val searchResult = async { repository?.getSMSForAddress(item.contactAddress, searchQuery!! ) }.await()
                    searchResult.let {
                        if(it!=null){

                            listOfResults.addAll(it)
                        }
                    }
                }
            }
        }.join()

        return listOfResults


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