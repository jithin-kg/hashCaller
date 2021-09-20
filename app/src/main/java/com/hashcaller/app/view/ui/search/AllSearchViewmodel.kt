package com.hashcaller.app.view.ui.search

import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.sms.util.SMS
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.Exception

class AllSearchViewmodel(
    private val allSearchRepository: AllSearchRepository,
    private val libPhoneCodeHelper: LibPhoneCodeHelper,
) :ViewModel() {

    var contactsListOfLivedata:MutableLiveData<List<Contact>> = MutableLiveData()
    var contactsSearchListLivedata:MutableLiveData<List<Contact>> = MutableLiveData()


    var smsListOfLivedata:MutableLiveData<List<SMS>> = MutableLiveData()
    private  var defContacts:Deferred<MutableList<Contact>>? = null
    private  var defSMS:Deferred<MutableList<SMS>>? = null

    /**
     * @param isFullResultNeeded if set to true get all results else get limited amount of result (3)
     */
    fun onQueryTextChanged(searchTerm: String, isFullResultNeeded: Boolean = false) = viewModelScope.launch {

//        libPhoneCodeHelper.getCountryIso( libPhoneCodeHelper.getES164Formatednumber(searchTerm, countryIso = "IN"))
        defContacts?.cancel()
        defSMS?.cancel()

        emptyAllLists()

        defContacts = async { allSearchRepository.searchInContacts(formatPhoneNumber(searchTerm), isFullResultNeeded)}


//        defSMS = async {  allSearchRepository.searchInSMS(searchTerm) }
        try {
            val contactsInCprovider = defContacts?.await()
            if (contactsInCprovider != null) {
                for (ct in contactsInCprovider){
                   ct.thumbnailInCprovider = allSearchRepository.getClearImageFromCprovider(ct.phoneNumber)?:""
                   val infoInDb = allSearchRepository.getMoreInfoServerForContact(ct.phoneNumber)
                   ct.isVerifiedUser = infoInDb?.isVerifiedUser?:false
                }
            }
            contactsSearchListLivedata.value =  contactsInCprovider
        }catch (e:Exception){
            Log.d(TAG, "onQueryTextChanged: $e")
        }

//        try {
////            smsListOfLivedata.value = defSMS?.await()
//        }catch (e:Exception){
//            Log.d(TAG, "onQueryTextChanged: $e")
//        }

    }

    private fun searchInSMS(searchTerm: String)  {

    }


    /**
     * To initialise all contacts, sms, and call logs lists
     * with all info available in cprovider such as name,photothumbnail etc.
     *  Called from all search activity
     */
    fun initAllLists() = viewModelScope.launch {
        allSearchRepository.setListOfContacts()
        allSearchRepository.setListOfSMS()
    }

    /**
     * Called from SearchSMSActivity
     */
    fun initContactsList()= viewModelScope.launch {
       val defLimtedList =  async { allSearchRepository.getListOfLimitedContacts() }
       val fullList =  async { allSearchRepository.setListOfContacts() }
        try {
            contactsListOfLivedata.value = defLimtedList.await()
            fullList.await()
            contactsListOfLivedata.value = allSearchRepository.getAllContacts()

        }catch (e:Exception){
            Log.d(TAG, "initContactsList: $e")
        }
    }

    fun emptyAllLists() = viewModelScope.launch{
        contactsSearchListLivedata.value = emptyList()
        smsListOfLivedata.value = emptyList()

    }



    fun getDefaultCountry(countryCodeHelper: CountrycodeHelper):LiveData<String> = liveData {
        emit(countryCodeHelper.getCountryISO())
    }

    fun setFullContactsList() = viewModelScope.launch {
        contactsListOfLivedata.value = allSearchRepository.getAllContacts()
    }


//    fun cancelPrevJob(searchJob: Job?) :LiveData<Int> = liveData{
//        try {
//           searchJob?.cancel()
//        }catch (e:Exception){
//            Log.d(TAG, "cancelPrevJob: $e")
//        }
////        allSearchRepository.doSomeDelay()
//        emit(OPERATION_COMPLETED)
//    }

    companion object {
        const val TAG = "__AllSearchViewmodel"
    }




}