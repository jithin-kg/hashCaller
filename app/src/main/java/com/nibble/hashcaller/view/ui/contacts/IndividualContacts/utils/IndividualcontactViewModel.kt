package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualContactLiveData
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualcontactViewModel(
    private val repository: IndividualContactRepository,
     val livedata: IndividualContactLiveData
)
    : ViewModel()  {
    var mt: MutableLiveData<ContactTable>
    var photoUri:MutableLiveData<String>
    init{

        var contactsFromLocalDb : LiveData<ContactTable>? = MutableLiveData<ContactTable>()
        photoUri = MutableLiveData<String>("")
        mt = MutableLiveData<ContactTable>(contactsFromLocalDb?.value)
//         mt = contactLocalSyncRepository.getContacts("")!!
    }

    companion object{
        private const val TAG = "__IndividualcontactViewModel"
    }
    @SuppressLint("LongLogTag")
    fun getContactsFromDb(phoneNumber: String)= viewModelScope.launch {

        if(!phoneNumber.trim().equals("")) {
            val c = repository.getIndividualContact(phoneNumber)
            Log.d(TAG, "size is $c ")
            if(c!=null && c.size>=1){
                mt.value = c[0]
            }

        }else{
            mt.value = ContactTable(0, "", "","", "",
                "",0)
        }



    }

    fun getPhoto(id: Long, phoneNum: String?) =viewModelScope.launch{
        val photo = repository.getPhoto(id, phoneNum)
        photoUri.value = photo
    }
//   val contact =
//       IndividualContactRepository(
//           application.applicationContext,
//           phoneNum
//       )

}