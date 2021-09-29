package com.hashcaller.app.view.ui.search

import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.Secrets
import com.hashcaller.app.network.HttpStatusCodes.Companion.NO_CONTENT
import com.hashcaller.app.network.HttpStatusCodes.Companion.STATUS_OK
import com.hashcaller.app.network.search.model.Cntct
import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.contacts.utils.DATE_THREASHOLD
import com.hashcaller.app.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import com.hashcaller.app.view.ui.contacts.utils.isNumericOnlyString
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.*
import retrofit2.Response
import java.lang.Exception

/**
 * ViewModel to only perform  search in server
 */
class ServerSearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository,
    private val libPhoneCodeHelper: LibPhoneCodeHelper
) : ViewModel() {
    val serverSearchResultLiveData: MutableLiveData<List<Contact>?> = MutableLiveData(null)
    private var mapOfhashedNumsSearched: HashMap<String, String> = hashMapOf() // <key-> hashednum , value-> formatedNum>
    fun searchInServer(
        phoneNumber: String,
        packageName: String,
        countryCode: String,
        countryIso: String
    ) =  viewModelScope.launch {
//        searchNetworkRepository.makeDelay()
//        showDummySearchResult()
        Log.d(TAG, "searchInServer: called")
        serverSearchResultLiveData.value = emptyList()
        delay(300L)
        var formatedNum = formatPhoneNumber(phoneNumber)
        Log.d(TAG, "searchInServer:formatedNum $formatedNum")
        if(isNumericOnlyString(formatedNum)){
            formatedNum =  libPhoneCodeHelper.getES164Formatednumber(formatedNum, countryIso)
        }

        Log.d(TAG, "searchInServer:formatedNum $formatedNum")
//        defServerSearch?.cancel()
//        defServerinfoAvialableInDb?.cancel()
        val hashed =  Secrets().managecipher(packageName,formatedNum)
        mapOfhashedNumsSearched[hashed] = formatedNum
        val infoAvialbleInDb = getServerinfoAvailableInDb(formatedNum, countryIso)

        var isPerformServerSearchInServer = shouldPerformServerSearch(infoAvialbleInDb)
        Log.d(TAG, "searchInServer: $isPerformServerSearchInServer")
       if(isPerformServerSearchInServer){
           val response  = searchNetworkRepository.manualSearch(hashed, countryCode, countryIso)
           response?.body()?.let {
               val reshash = it.cntcts?.clientHashedNum?:""
                   if(response?.code() == NO_CONTENT){
//                        saveServerIntoDb(getPreparedContact(null, formatedNum))
                       val numInMap = mapOfhashedNumsSearched[response?.body()?.cntcts?.clientHashedNum]
                       if(numInMap!=null){
                           searchNetworkRepository.saveServerInfoIntoDB(getPreparedContact(null, numInMap))
                       }
                       serverSearchResultLiveData.value = emptyList()
                   }else if(response?.code() == STATUS_OK){
                       Log.d(TAG, "searchInServer: status ok ${response?.body()?.cntcts?.clientHashedNum}")
//                        saveServerIntoDb(getPreparedContact(result, formatedNum))
                       //get phone number from map by matching hash value
                       val numInMap = mapOfhashedNumsSearched[response?.body()?.cntcts?.clientHashedNum]
                       if(!numInMap.isNullOrEmpty()){
                           searchNetworkRepository.saveServerInfoIntoDB(getPreparedContact(response?.body()?.cntcts, numInMap))
                           val searchResult = getPreparedContact(response?.body()?.cntcts, numInMap)
                           serverSearchResultLiveData.value = listOf(searchResult)
                       }

                   }

           }

       }else {
           //no need to perform searching in server, information avaialbe in local db
           Log.d(TAG, "searchInServer: no need to search in server $infoAvialbleInDb")
           val searchResult = Contact(-1,
               firstName = infoAvialbleInDb?.firstName?:"",
                lastName= infoAvialbleInDb?.lastName?:"",
               phoneNumber= phoneNumber,
               photoThumnailServer = infoAvialbleInDb?.thumbnailImg,
               country = infoAvialbleInDb?.country?:"",
               location = infoAvialbleInDb?.city?:"",
               spamCount =  infoAvialbleInDb?.spamReportCount?:0L,
               isInfoFoundInServer= infoAvialbleInDb?.isUserInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER,
               isVerifiedUser = infoAvialbleInDb?.isVerifiedUser?:false,
               nameInPhoneBook = infoAvialbleInDb?.nameInPhoneBook?:"",
               avatarGoogle = infoAvialbleInDb?.avatarGoogle?:"",
               photoURI = infoAvialbleInDb?.thumbnailImg?:"",
               hUid = infoAvialbleInDb?.hUid?:""
           )
           if(searchResult.isInfoFoundInServer !=INFO_NOT_FOUND_IN_SERVER ){
               Log.d(TAG, "searchInServer: updating livedata if$searchResult")
               serverSearchResultLiveData.value = listOf(searchResult)
           }else {
               Log.d(TAG, "searchInServer: updating livedata setting empty list")
               serverSearchResultLiveData.value = emptyList()
           }

       }
    }


    private fun showDummySearchResult(){
        val dummyContact = Contact(1,
            firstName = "Tina Moss",
            phoneNumber = "+12065550134",
            photoThumnailServer = "content://com.android.contacts/contacts/5352/photo"
        )
        serverSearchResultLiveData.value = listOf(dummyContact)
    }
    private fun getPreparedContact(contact: Cntct?, formatedNum: String): Contact {
        return Contact(-1,
            firstName = contact?.firstName?:"",
            lastName =contact?.lastName?:"",
            phoneNumber= formatedNum,
            photoThumnailServer = contact?.thumbnailImg?:"",
            photoURI = "",
            country = "",
            location = contact?.location?:"",
            spamCount =  contact?.spammCount?:0L,
            isInfoFoundInServer= contact?.isInfoFoundInDb?:INFO_NOT_FOUND_IN_SERVER,
            nameInPhoneBook = contact?.nameInPhoneBook?:"",
            hUid = contact?.hUid?:"",
            email = contact?.email?:"",
            avatarGoogle = contact?.avatarGoogle?:"",
            bio = contact?.bio?:"",
            isVerifiedUser = contact?.isVerifiedUser?:false

        )

    }

    private fun shouldPerformServerSearch(infoAvialbleInDb: CallersInfoFromServer?): Boolean {
        var shouldPerformServerSearch = false
        if(infoAvialbleInDb == null){
            shouldPerformServerSearch = true
        }else if( isCurrentDateAndPrevDateisGreaterThanLimit(infoAvialbleInDb.informationReceivedDate, DATE_THREASHOLD)){
            shouldPerformServerSearch = true
        }
        return  shouldPerformServerSearch
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun saveServerIntoDb(contact: Contact)= viewModelScope.launch {
//        searchNetworkRepository.saveServerInfoIntoDB(contact)
    }

    suspend fun getServerinfoAvailableInDb(formatedNum: String, countryIso: String): CallersInfoFromServer? {
        return searchNetworkRepository.findOneInDb(formatedNum, countryIso)


    }

    fun clearResult()= viewModelScope.launch {
        serverSearchResultLiveData.value = emptyList()
    }

    companion object {
        const val TAG = "__ServerSearchViewModel"
    }
}