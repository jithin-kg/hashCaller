package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Jithin KG on 22,July,2020
 */
class SearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository
): ViewModel() {
    var searchRes = MutableLiveData<Response<SearchResponse>>()
    // The internal MutableLiveData String that stores the most recent response
//    private val _response = MutableLiveData<String>()
//    var mt:
    // The external immutable LiveData for the response String
//    val response: LiveData<String>

//        get() = _response


//    init {
//       syncContactsWithLocalDb()
//
//    }



    fun search(phoneNumber:String) = liveData(Dispatchers.IO) {
                emit(Resource.loading(data=null))
        var res: Response<SerachRes>? = null
             try {
                   res = searchNetworkRepository.search(phoneNumber)

                 Log.d(TAG, "search: $res")
                 emit(Resource.success(data = res?.body()));

             }catch (e:Exception){
                 Log.d(TAG, "response: $res");
                 Log.d(TAG, "execption : $e");
                     emit(Resource.error(null, message ="Error Occurred!" ))
             }


    }





    /**
     * If there is no contacts is local sqlite database then we insert all contacts
     * by adding all contacts to a list
     */
//    private suspend fun insertContactstoLocalDb(contentProviderContacts: List<ContactUploadDTO>?) {
//        val contactsListToSave:MutableList<ContactTable> = mutableListOf()
////        for(item in contentProviderContacts){
////            Log.d(TAG, "getPreparedContacts: ${i}")
////            val contact = ContactTable(item.phoneNumber, item.name)
////           contactLocalSyncRepository.insertContacts(contact)
////        }
//
//        contentProviderContacts?.forEachIndexed { i, item->
//            run {
//                val contact = ContactTable(null, item.phoneNumber, item.name)
//
//                val response = contactLocalSyncRepository.insertContacts(contact)
//            }
//        }
//
//    }



companion object{
    private const val TAG ="__SearchViewModel"
}
}
