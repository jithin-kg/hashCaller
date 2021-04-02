package com.nibble.hashcaller.view.ui.contacts.individualContacts.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualcontactViewModel(
    private val repository: IndividualContactRepository,
    val livedataCntct: IndividualContactLiveData,
    private val mutedContactsDAO: IMutedCallersDAO,
    private val callersInfoFromServer: CallersInfoFromServerDAO
)
    : ViewModel()  {

    val mutedContacts = mutedContactsDAO.get().asLiveData()
    val callersinfoLivedata = callersInfoFromServer.getFlow().asLiveData()

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


        if(phoneNumber.trim() != "") {

            var  num:String = phoneNumber
            num = formatPhoneNumber(num)
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

    fun getContactFromContentProvider(phoneNum: String?): LiveData<Contact?> = liveData {
        emit(repository.getContactDetailForNumber(phoneNum!!))

    }

    fun isThisAddressMuted(phoneNum: String, list: List<MutedCallers>) : LiveData<Boolean> = liveData{
        var isMuted = false
        val res =  list.find { it.address == formatPhoneNumber(phoneNum) }
        if(res!=null){
            isMuted = true
        }
        emit(isMuted)
    }

    fun muteThisAddress(phoneNum: String) = viewModelScope.launch{
        mutedContactsDAO.insert(listOf(MutedCallers(formatPhoneNumber(phoneNum))))

    }

    fun unMuteByAddress(phoneNum: String) = viewModelScope.launch{
        mutedContactsDAO.delete(formatPhoneNumber(phoneNum))
    }

    /**
     *  function to check whether a given number is blocked by the user
     */
    fun isThisAddressBlockedByUser(phoneNum: String): LiveData<Boolean> = liveData  {
        var isBlocked = false
        val res = callersInfoFromServer.find(formatPhoneNumber(phoneNum))
        if(res !=null){
            if(res.isBlockedByUser){
                isBlocked = true
            }
        }
        emit(isBlocked)
    }

    fun blockOrUnblockByAdderss(phoneNum: String) = viewModelScope.launch {
        val formatedPhoneNumber = formatPhoneNumber(phoneNum)
        callersInfoFromServer.find(formatedPhoneNumber).apply {
            if(this !=null){
                //number exist in db
                    if(this.isBlockedByUser){
                        //we need to unblock , no need of changing spam count
                        callersInfoFromServer.unBlock(false, this.contactAddress)

                    }else{
                        callersInfoFromServer.update(this.spamReportCount+1, this.contactAddress,true)

                    }
            }else{

                val callerInfoTobeSavedInDatabase = CallersInfoFromServer(null,
                    formatPhoneNumber(formatedPhoneNumber), 0,  "",
                    Date(), 1, isBlockedByUser = true)
                callersInfoFromServer.insert(listOf(callerInfoTobeSavedInDatabase))
            }
        }
    }


//   val contact =
//       IndividualContactRepository(
//           application.applicationContext,
//           phoneNum
//       )

}