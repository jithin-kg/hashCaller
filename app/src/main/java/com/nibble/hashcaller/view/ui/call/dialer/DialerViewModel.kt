package com.nibble.hashcaller.view.ui.call.dialer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.stubs.Contact
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class DialerViewModel(
    private val repository: DialerRepository?,
    private val contactSearchRepository: ContactSearchRepository?
) : ViewModel() {
    
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
       val result =  repository?.getFirst10Logs()

    }

    fun searchContactsInDb(phoneNumber: String) = viewModelScope.launch{
//        val contactsContainingName :MutableList<Contact> = mutableListOf()
//        val contactsContainingNum =  contactSearchRepository?.getContactsLike(phoneNumber)
        var hashsetOfSearchResult : HashSet<Contact> = hashSetOf()
        val combinationOfLetters =  NumberToStringMapper.printStringForNumber(phoneNumber)
        combinationOfLetters.add(phoneNumber)
       val fullSearchResult: MutableList<Contact> = mutableListOf()

        for (str in combinationOfLetters){
            contactSearchRepository?.getContactsLike(str)?.let { fullSearchResult.addAll(it) }
        }
        hashsetOfSearchResult.addAll(fullSearchResult)
        searchResultLivedata.value = hashsetOfSearchResult.toList()

//
//        for (item in combinationOfLetters){
////           contactSearchRepository?.getContactsLike(phoneNumber)?.let {
////               contactsContainingName.addAll(it)
////           }
//       }


        
        

    }


    companion object{
        private const val TAG ="__DialerViewModel"
//        var strCombinationForNum :Flow<String> = 
    }
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}