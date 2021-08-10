package com.hashcaller.view.ui.call.dialer

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashcaller.stubs.Contact
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by Jithin KG on 22,July,2020
 */
class DialerViewModel(
    private val repository: DialerRepository?,
    private val contactSearchRepository: ContactSearchRepository?
) : ViewModel() {
    init {
        getAllCallLogs()
    }

    private fun getAllCallLogs() = viewModelScope.launch {
        allContacts = contactSearchRepository?.getAllContacts()
    }

    private var allContacts:MutableList<Contact>? = mutableListOf()
    private var phoneNumber: MutableLiveData<String>? = null
    var searchResultLivedata : MutableLiveData<List<Contact>> = MutableLiveData()


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (phoneNumber == null) {
            phoneNumber = MutableLiveData<String>()
            return phoneNumber
        }
        return phoneNumber
    }



    fun getFirst10Logs() = viewModelScope.launch  {
       withContext(Dispatchers.Default){
           val result =  repository?.getFirst10Logs()
           val contacts: MutableList<Contact> = mutableListOf()

           if (result != null) {
               for (item in result){
                   val contact = item.id?.let { item.name?.let { it1 ->
                       Contact(it,
                           it1, item.number, item.thumbnailFromCp)
                   } }

                   contact?.let { contacts.add(it) }

               }
           }
           withContext(Dispatchers.Main){
               searchResultLivedata.value = contacts
           }
       }

    }

    fun searchContactsInDb(text: String) = viewModelScope.launch {

        searchResultLivedata.value = repository?.getFilteredlist(text, allContacts)
    }

    private suspend fun getList(combinationOfLetters: MutableList<String>): List<Contact> = withContext(Dispatchers.Default) {
        var fullSearchResult: MutableList<Contact> = mutableListOf()
        var hashsetOfSearchResult : HashSet<Contact> = hashSetOf()
        val iterator = combinationOfLetters.iterator()
        try {

            while (iterator.hasNext()){
                async { contactSearchRepository?.getContactsLike(iterator.next())?.let { fullSearchResult.addAll(it) } }.await()
//              val res =   contactSearchRepository?.getContactsLike(iterator.next())
//                if(!res.isNullOrEmpty()){
//                    fullSearchResult.addAll(res)
////                    break
//                }
//                contactSearchRepository?.getContactsLike(iterator.next())?.let { fullSearchResult.addAll(it) }
//                yield()


             }
        }catch (e:Exception){
            Log.d(TAG, "getList:exception $e")
        }
//        for (str in combinationOfLetters){
//            contactSearchRepository?.getContactsLike(str)?.let { fullSearchResult.addAll(it) }
//        }

        hashsetOfSearchResult.addAll(fullSearchResult)
        return@withContext hashsetOfSearchResult.toList()
    }


    companion object{
        private const val TAG ="__DialerViewModel"
        var cancelJob = false
//        var strCombinationForNum :Flow<String> = 
    }
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}