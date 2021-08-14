package com.hashcaller.view.ui.search

import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.Secrets
import com.hashcaller.network.HttpStatusCodes.Companion.NO_CONTENT
import com.hashcaller.network.HttpStatusCodes.Companion.STATUS_OK
import com.hashcaller.network.search.model.Cntct
import com.hashcaller.network.search.model.SerachRes
import com.hashcaller.repository.search.SearchNetworkRepository
import com.hashcaller.stubs.Contact
import com.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.view.ui.contacts.utils.DATE_THREASHOLD
import com.hashcaller.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import com.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.view.utils.LibPhoneCodeHelper
import com.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    private var defServerSearch: Deferred<Response<SerachRes>?>? = null
    private var defServerinfoAvialableInDb: Deferred<CallersInfoFromServer?>? = null

    fun searchInServer(
        phoneNumber: String,
        packageName: String,
        countryCode: String,
        countryIso: String
    ) =  viewModelScope.launch {
//        searchNetworkRepository.makeDelay()
//        showDummySearchResult()
        var formatedNum = formatPhoneNumber(phoneNumber)
        formatedNum =  libPhoneCodeHelper.getES164Formatednumber(formatedNum, countryIso)

        defServerSearch?.cancel()
        defServerinfoAvialableInDb?.cancel()
        defServerSearch = null
        defServerinfoAvialableInDb = null
        defServerinfoAvialableInDb = async { getServerinfoAvailableInDb(formatedNum, countryIso) }


       val hashed =  Secrets().managecipher(packageName,formatedNum)
       var infoAvialbleInDb:CallersInfoFromServer? = null
       try {
           infoAvialbleInDb =  defServerinfoAvialableInDb?.await()
       } catch (e:Exception){
           Log.d(TAG, "searchInServer: $e")
       }
        var isPerformServerSearchInServer = shouldPerformServerSearch(infoAvialbleInDb)
       if(isPerformServerSearchInServer){
           defServerSearch = async { searchNetworkRepository.manualSearch(hashed, countryCode, countryIso) }
       }else {
           val searchResult = Contact(-1,
               firstName = infoAvialbleInDb?.firstName?:"",
               phoneNumber= phoneNumber,
               photoThumnailServer = infoAvialbleInDb?.thumbnailImg,
               photoURI = "",
               country = "",
               location = infoAvialbleInDb?.city?:"",
               spamCount =  infoAvialbleInDb?.spamReportCount?:0L,
               isInfoFoundInServer= infoAvialbleInDb?.isUserInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER
           )
           if(searchResult.isInfoFoundInServer !=INFO_NOT_FOUND_IN_SERVER ){
               serverSearchResultLiveData.value = listOf(searchResult)
           }else {
               serverSearchResultLiveData.value = emptyList()
           }

       }

        try {
           val response = defServerSearch?.await()
           val result = response?.body()?.cntcts
//           if(result!= null){
                result?.let {
                    val searchResult = getPreparedContact(it, formatedNum)
                    serverSearchResultLiveData.value = listOf(searchResult)

                }

//           }
            if(response?.code() == NO_CONTENT){
                saveServerIntoDb(getPreparedContact(null, formatedNum))
                serverSearchResultLiveData.value = emptyList()
            }else if(response?.code() == STATUS_OK){
                saveServerIntoDb(getPreparedContact(result, formatedNum))
            }
       }catch (e:Exception){
           Log.d(TAG, "searchInServer: $e")
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
            phoneNumber= formatedNum,
            photoThumnailServer = contact?.thumbnailImg?:"",
            photoURI = "",
            country = "",
            location = contact?.location?:"",
            spamCount =  contact?.spammCount?:0L,
            isInfoFoundInServer= contact?.isInfoFoundInDb?:INFO_NOT_FOUND_IN_SERVER
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
        searchNetworkRepository.saveServerInfoIntoDB(contact)
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