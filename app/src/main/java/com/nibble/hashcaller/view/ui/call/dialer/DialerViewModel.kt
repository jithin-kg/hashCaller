package com.nibble.hashcaller.view.ui.call.dialer

import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.sms.individual.util.normalizeString
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
       val result =  repository?.getFirst10Logs()

    }

    fun searchContactsInDb(text: String) = viewModelScope.launch{

        var filtered = allContacts?.filter {
            val convertedName = PhoneNumberUtils.convertKeypadLettersToDigits(it.name.normalizeString())
            it.doesContainPhoneNumber(text) || (convertedName.contains(text, true))

        }?.sortedWith(compareBy{
            !it.doesContainPhoneNumber(text)

        })?.toMutableList()
        searchResultLivedata.value = filtered
        Log.d(TAG, "searchContactsInDb:${filtered?.size}")
//        var def:Job? = null
//        var def2 :Job? = null
//        cancelJob= true
//
////        def?.cancelChildren()
////        def2?.cancelChildren()
////
////        def?.cancel()
////        def2?.cancel()
////        val contactsContainingName :MutableList<Contact> = mutableListOf()
////        val contactsContainingNum =  contactSearchRepository?.getContactsLike(phoneNumber)
//        cancelJob= false
//        def2= async {  NumberToStringMapper.printStringForNumber(phoneNumber) }
//        var combinationOfLetters = def2.await()
////        hashsetOfSearchResult = Collections.synchronizedSet(hashsetOfSearchResult)
////        val threadPool = Executors.newCachedThreadPool().asCoroutineDispatcher()
//            combinationOfLetters.add(phoneNumber)
//         def =   async {
//              getList(combinationOfLetters)
//          }
//        searchResultLivedata.value = def.await()


//
//        for (item in combinationOfLetters){
////           contactSearchRepository?.getContactsLike(phoneNumber)?.let {
////               contactsContainingName.addAll(it)
////           }
//       }


        
        

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