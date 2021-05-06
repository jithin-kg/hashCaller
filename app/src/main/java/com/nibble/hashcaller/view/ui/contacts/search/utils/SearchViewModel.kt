package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.util.Base64
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.SearchResponse
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.utils.hashPhoneNum
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Response
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


/**
 * Created by Jithin KG on 22,July,2020
 */

class SearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository,
    private  val contactLocalSyncRepository: ContactLocalSyncRepository
): ViewModel() {
    var searchRes = MutableLiveData<Response<SearchResponse>>()
    var hashedPhoneNum:MutableLiveData<String> = MutableLiveData()

    var searchResultsLivedata:MutableLiveData<List<Contact>> = MutableLiveData()

    init{
         var contactsFromLocalDb : LiveData<List<ContactTable>>? = contactLocalSyncRepository.contactsFomLocalDB
//         mt = MutableLiveData<List<ContactTable>>(contactsFromLocalDb?.value)
//         mt = contactLocalSyncRepository.getContacts("")!!
    }


    fun searchContactsInDb(phoneNumber: String) = viewModelScope.launch{

        if(!phoneNumber.trim().equals(""))  {
            searchResultsLivedata.value = contactLocalSyncRepository.getContactsLike(phoneNumber) as List<Contact>
        }
//        else{
//            contactsLocalSearchLiveDAta.value = emptyList()
//        }



    }

//    fun search(phoneNumber:String) = liveData(Dispatchers.IO) {
    fun search(phoneNumber: String, key: String?, packageName: String) = viewModelScope.launch {
//                emit(Resource.loading(data=null))
        var res: Response<SerachRes>? = null
             try {

//                    mt.value  = cntctsFromDb?.value
//                 Log.d(TAG, "search: ${cntctsFromDb?.value?.size}")
                 Log.d(TAG, "search: hash is $phoneNumber")
                val hashedPhone = hashPhoneNum(phoneNumber)
//                 val encPhone = encryptPhoneNum("hi", key)
//                 Log.d(TAG, "search: enc hi is $encPhone")
//                 val encPhone = encryptPhoneNum(hashedPhone, key)
                 val num = formatPhoneNumber(phoneNumber)
                 hashedPhoneNum.value = Secrets().managecipher(packageName, num)//encoding the number with my algorithm

                 res = searchNetworkRepository.search(hashedPhoneNum.value!!)
                 var result = res?.body()?.cntcts

//                 searchResultLiveData.value = result

//                 res = searchNetworkRepository.search("Iyfgga1yHP90u/mBwFS5XK2QNq3KRsr+ZYYGH6Lav5X4IS8FvzMlC/WKvxQ0+o1q7XXgcNZ8Olg6P5JEKXVAGnVlexUDbUsjCuwZgRbACGJ8jIueYOUcgc7w9N+K1+Sc6I7ZAe6vRLknjpLuLIy9DOMJ/wirO1s5tv+l/fgDbEJp7Jl1rOodiFZU1ysBl/2cel7+9Xozb1+ZJhkQk/hlKdX49MvXwVDmbO+2uGYIEIe7V6uNouPlpE7VAKg/VP29uySsxDNJFR8ABEvMJhEkqkQJTCmM4Jk0sQmgmV1e+44ugyIZPEZMMQPGT/M+D4w2JtAg21zHpEUmXtkGZ7Lyuxp55fUWk8ISAZ/wPm9BO9hYCC4mEGF3vWtxEJNoWKMLw6vxeGbOnaNVakZs0bze9OzTsJsz3Vxgv1arutT9gDl2FfB5IfTHoC85V9+J4AUGnn4v1Oel7NxdPici0DhBAawkb41FV+fW6LuUgtm1TpdxyKuxZOfObQS23XtmmArd9B44TjMNl0S7G3RsfG3BYCSFG+bxSEgPgWLboXjkwhFqLK0MH7oD2oLvrP2eZYWK+lJSt0g926zT94yC5Y5jQCatsN//ZKiOunxNPI2Gr6Nj2fVdEsGRIjAYBqiPxk/GmshlN3RLnFtlu+PQY+PDqdhxpA5ZbOsBgjbrtFyJ2o4=")

                 Log.d(TAG, "search: $res")
//                 emit(Resource.success(data = res?.body()));

             }catch (e:Exception){
                 Log.d(TAG, "response: $res");
                 Log.d(TAG, "execption : $e");
//                     emit(Resource.error(null, message ="Error Occurred!" ))
             }


    }

    private fun encryptPhoneNum(phoneNumber: String, key: String?): String {
        var encoded = ""
        var encrypted: ByteArray? = null
        try {
            val publicBytes: ByteArray = Base64.decode(key, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(publicBytes)
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING") //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey)
            encrypted = cipher.doFinal(phoneNumber.toByteArray())
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "encryptPhoneNum: error $e")
            e.printStackTrace()
        }
        Log.d(TAG, "encryptPhoneNum: $encoded")
        return encoded;
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
