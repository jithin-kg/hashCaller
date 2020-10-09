package com.nibble.hashcaller.view.ui.IncommingCall

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Response

/**
 * Created by Jithin KG on 22,July,2020
 */
class IncommingCallViewModel(
    private val spamNetworkRepository: SpamNetworkRepository
): ViewModel() {
//    var searchRes = MutableLiveData<Response<SearchResponse>>()
//
//    init{
//
//
////         mt = contactLocalSyncRepository.getContacts("")!!
//    }



    fun report(phoneNumber:String) = liveData(Dispatchers.IO) {
                emit(Resource.loading(data=null))
        var res: Response<NetWorkResponse>? = null
             try {

//                    mt.value  = cntctsFromDb?.value
//                 Log.d(TAG, "search: ${cntctsFromDb?.value?.size}")

                 
                   res = spamNetworkRepository.report(ReportedUserDTo(phoneNumber, "sample"))

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
