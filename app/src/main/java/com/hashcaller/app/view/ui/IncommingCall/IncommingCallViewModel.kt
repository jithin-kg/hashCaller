package com.hashcaller.app.view.ui.IncommingCall

import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.Secrets
import com.hashcaller.app.network.contact.NetWorkResponse
import com.hashcaller.app.network.spam.ReportedUserDTo
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers

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
