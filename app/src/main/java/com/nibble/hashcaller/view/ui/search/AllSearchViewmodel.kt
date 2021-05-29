package com.nibble.hashcaller.view.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.Exception

class AllSearchViewmodel(private val allSearchRepository: AllSearchRepository) :ViewModel() {

    var contactsListOfLivedata:MutableLiveData<List<Contact>> = MutableLiveData()
    var smsListOfLivedata:MutableLiveData<List<SMS>> = MutableLiveData()

    fun onQueryTextChanged(searchTerm:String) = viewModelScope.launch {
        val defContacts = async { allSearchRepository.searchInContacts(searchTerm)}
        val defSMS = async {  allSearchRepository.searchInSMS(searchTerm) }

        try {
            contactsListOfLivedata.value =  defContacts.await()
        }catch (e:Exception){
            Log.d(TAG, "onQueryTextChanged: $e")
        }

        try {
            smsListOfLivedata.value = defSMS.await()
        }catch (e:Exception){
            Log.d(TAG, "onQueryTextChanged: $e")
        }

    }

    private fun searchInSMS(searchTerm: String)  {

    }


    /**
     * To initialise all contacts, sms, and call logs lists
     * with all info available in cprovider such as name,photothumbnail etc.
     */
    fun initAllLists() = viewModelScope.launch {
        allSearchRepository.setListOfContacts()
        allSearchRepository.setListOfSMS()
    }

    fun emptyAllLists() = viewModelScope.launch{
        contactsListOfLivedata.value = emptyList()
        smsListOfLivedata.value = emptyList()
    }

    companion object {
        const val TAG = "__AllSearchViewmodel"
    }

}