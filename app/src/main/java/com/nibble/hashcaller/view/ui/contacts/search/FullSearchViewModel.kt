package com.nibble.hashcaller.view.ui.contacts.search

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
class FullSearchViewModel(
    private val searchNetworkRepository: SearchNetworkRepository,
    private  val contactLocalSyncRepository: ContactLocalSyncRepository
): ViewModel() {

    init{
    }


    fun searchContactsInDb(phoneNumber: String):LiveData<List<Contact>> = liveData{

        if(!phoneNumber.trim().equals(""))  {
            val c = contactLocalSyncRepository.getContactsLike(phoneNumber) as List<Contact>
//            Log.d(TAG, "size is ${c?.size} ")
//            contactsLocalSearchLiveDAta.value = c
            emit(c)
        }
//        else{
//            contactsLocalSearchLiveDAta.value = emptyList()
//        }



    }



companion object{
    private const val TAG ="__SearchViewModel"
}
}
