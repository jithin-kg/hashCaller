package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.security.MessageDigest
import kotlin.experimental.and


/**
 * Created by Jithin KG on 22,July,2020
 */
class SearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository,
    private  val contactLocalSyncRepository: ContactLocalSyncRepository
): ViewModel() {
    var searchRes = MutableLiveData<Response<SearchResponse>>()

    var mt:MutableLiveData<List<ContactTable>>
    init{
         var contactsFromLocalDb : LiveData<List<ContactTable>>? = contactLocalSyncRepository.contactsFomLocalDB
         mt = MutableLiveData<List<ContactTable>>(contactsFromLocalDb?.value)
//         mt = contactLocalSyncRepository.getContacts("")!!
    }


    fun getContactsFromDb(phoneNumber: String)= viewModelScope.launch {

        if(!phoneNumber.trim().equals("")) {
            val c = contactLocalSyncRepository.getContacts(phoneNumber)
            Log.d(TAG, "size is ${c?.size} ")
            mt.value = c
        }else{
            mt.value = emptyList()
        }



    }

//    fun search(phoneNumber:String) = liveData(Dispatchers.IO) {
    fun search(phoneNumber:String) = viewModelScope.launch {
//                emit(Resource.loading(data=null))
        var res: Response<SerachRes>? = null
             try {

//                    mt.value  = cntctsFromDb?.value
//                 Log.d(TAG, "search: ${cntctsFromDb?.value?.size}")
                val hashedPhone = hashPhoneNum(phoneNumber)

//                 res = searchNetworkRepository.search(phoneNumber)

                 Log.d(TAG, "search: $res")
//                 emit(Resource.success(data = res?.body()));

             }catch (e:Exception){
                 Log.d(TAG, "response: $res");
                 Log.d(TAG, "execption : $e");
//                     emit(Resource.error(null, message ="Error Occurred!" ))
             }


    }

    private fun hashPhoneNum(phoneNumber: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        md.update(phoneNumber.toByteArray());
        val bytes = md.digest()
        val sb = StringBuilder()
        for (element in bytes) {
            sb.append(
                ((element and 0xff.toByte()) + 0x100).toString(16)
                    .substring(1)
            )
        }
        val hashedPhone = sb.toString()
        Log.d(TAG, "search: hashed phone is  $hashedPhone")
        return hashedPhone
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
