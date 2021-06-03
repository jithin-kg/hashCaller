package com.nibble.hashcaller.view.ui.search

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.Exception

class AllSearchViewmodel(
    private val allSearchRepository: AllSearchRepository,
) :ViewModel() {

    var contactsListOfLivedata:MutableLiveData<List<Contact>> = MutableLiveData()
    var smsListOfLivedata:MutableLiveData<List<SMS>> = MutableLiveData()
    private  var defContacts:Deferred<MutableList<Contact>>? = null
    private  var defSMS:Deferred<MutableList<SMS>>? = null
    fun onQueryTextChanged(searchTerm:String) = viewModelScope.launch {
        defContacts?.cancel()
        defSMS?.cancel()

        emptyAllLists()

        defContacts = async { allSearchRepository.searchInContacts(searchTerm)}
        defSMS = async {  allSearchRepository.searchInSMS(searchTerm) }
        try {
            contactsListOfLivedata.value =  defContacts?.await()
        }catch (e:Exception){
            Log.d(TAG, "onQueryTextChanged: $e")
        }

        try {
            smsListOfLivedata.value = defSMS?.await()
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

    fun getDefaultCountry(countryCodeHelper: CountrycodeHelper):LiveData<String> = liveData {
        emit(countryCodeHelper.getCountryISO())
    }

    companion object {
        const val TAG = "__AllSearchViewmodel"
    }




}