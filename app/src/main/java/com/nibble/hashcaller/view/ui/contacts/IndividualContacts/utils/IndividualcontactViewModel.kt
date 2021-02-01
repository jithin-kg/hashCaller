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
            var  num:String = phoneNumber

//            num =  num.replace(Regex("[^A-Za-z0-9]"), "")
            Log.d(TAG, "getContactsFromDb: num is $num")

            val c = repository.getIndividualContact(num)
            Log.d(TAG, "size is $c ")
            if(c!=null ){
                mt.value = c
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

    fun getMoreInfoforNumber(phoneNum: String?) {
        this.repository.getMoreInfoFOrNumber(phoneNum)
    }
//   val contact =
//       IndividualContactRepository(
//           application.applicationContext,
//           phoneNum
//       )

}