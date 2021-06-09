package com.nibble.hashcaller.view.ui.contacts.individualContacts.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.nibble.hashcaller.view.ui.contacts.individualContacts.ThumbnailImageData
import com.nibble.hashcaller.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_C_PROVIDER
import com.nibble.hashcaller.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualcontactViewModel(
    private val repository: IndividualContactRepository,
    val livedataCntct: IndividualContactLiveData,
    private val mutedContactsDAO: IMutedCallersDAO,
    private val callersInfoFromServer: CallersInfoFromServerDAO,
    private val spamNetworkRepository: SpamNetworkRepository
)
    : ViewModel()  {

    var contactId = 0L
    val mutedContacts = mutedContactsDAO.get().asLiveData()
//    val callersinfoLivedata = callersInfoFromServer.getFlow().asLiveData()

    var mt: MutableLiveData<CallersInfoFromServer>
    var contactForViewLivedata: MutableLiveData<CntctitemForView> = MutableLiveData()
    var photoUri:MutableLiveData<String>
    init{


        var contactsFromLocalDb : LiveData<CallersInfoFromServer>? = MutableLiveData<CallersInfoFromServer>()
        photoUri = MutableLiveData<String>("")
        mt = MutableLiveData<CallersInfoFromServer>(contactsFromLocalDb?.value)
//         mt = contactLocalSyncRepository.getContacts("")!!
    }
    var infoFromServer:LiveData<ContactTable?>? = repository.getInfoFromServerForContact()
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

            val c = repository.getIndividualContactFromDb(num)
            Log.d(TAG, "size is $c ")
            if(c!=null ){
                mt.value = c
            }

        }else{
//            mt.value = ContactTable(0, "", "","", "",
//                "",0)
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
        val res = repository.getContactDetailForNumberFromCp(phoneNum!!)
        if (res != null) {
            contactId = res.id
            emit(res)
        }


    }

    fun isThisAddressMuted(phoneNum: String, list: List<MutedCallers>) : LiveData<Boolean> = liveData{
        var isMuted = false
        val res =  list.find { it.address == formatPhoneNumber(phoneNum) }
        if(res!=null){
            isMuted = true
        }
        emit(isMuted)
    }

    fun muteThisAddress(phoneNum: String): LiveData<Int> = liveData{
        val formatedNum = formatPhoneNumber(phoneNum)
        mutedContactsDAO.find(formatedNum).apply {
            if(this == null){
                //this number is not yet muted, so add new record
                mutedContactsDAO.insert(listOf(MutedCallers(formatPhoneNumber(phoneNum)))).apply {
                    emit(OPERATION_COMPLETED)
                }
            }else{
                //this number is already muted, so unmute
                mutedContactsDAO.delete(formatedNum).apply {

                }
            }
        }


    }

    fun unMuteByAddress(phoneNum: String) = viewModelScope.launch{
        mutedContactsDAO.delete(formatPhoneNumber(phoneNum))
    }

    /**
     *  function to check whether a given number is blocked by the user
     */
    fun isThisAddressBlockedByUser(phoneNum: String, isBlockTopSpammersEnabled: Boolean): LiveData<Boolean> = liveData  {
        var isBlocked = false
        val res = repository.getCallLogInfoForNum(phoneNum)
        if(res!=null){
            if(res.isReportedByUser){
                isBlocked = true
            }

            if(res.spamCount > SPAM_THREASHOLD  && isBlockTopSpammersEnabled){
                isBlocked = true
            }
        }
        emit(isBlocked)
//        val res = callersInfoFromServer.find(formatPhoneNumber(phoneNum))
//        if(res !=null){
//            if(res.isBlockedByUser){
//                isBlocked = true
//            }
//        }
//        emit(isBlocked)
    }

    fun blockOrUnblockByAdderss(phoneNum: String, spammerType: Int):LiveData<Int> = liveData {


        val formatedPhoneNumber = formatPhoneNumber(phoneNum)



//        callersInfoFromServer.find(formatedPhoneNumber).apply {
//            if(this !=null){
//                //number exist in db
//                    if(this.isBlockedByUser){
//                        //we need to unblock , no need of changing spam count
//                            val spamcount = this.spamReportCount -1
//                        callersInfoFromServer.unBlock(false, this.contactAddress, spamcount).apply {
//                            emit(OPERATION_UNBLOCKED)
//                        }
//
//                    }else{
//                        callersInfoFromServer.update(this.spamReportCount+1, this.contactAddress,true).apply {
//                            emit(OPERATION_BLOCKED)
//
//                        }
//                        //TODO USER WORKER FOR REPORTING SPAM HERE
//                        //report to server
//                        spamNetworkRepository.report(
//                            ReportedUserDTo(phoneNum, "IN",
//                            spammerType.toString()))
//                    }
//            }else{
//
//                val callerInfoTobeSavedInDatabase = CallersInfoFromServer(
//                    contactAddress= formatPhoneNumber(formatedPhoneNumber), spammerType=0,  firstName="",
//                    informationReceivedDate = Date(),spamReportCount =  1, isBlockedByUser = true)
//                callersInfoFromServer.insert(listOf(callerInfoTobeSavedInDatabase))
//                emit(OPERATION_BLOCKED)
//            }
//        }
    }

    fun unmute(phoneNum: String) = viewModelScope.launch {
        mutedContactsDAO!!.delete(formatPhoneNumber(phoneNum))
    }

    @SuppressLint("LongLogTag")
    fun getClearImage(phoneNum: String):LiveData<ThumbnailImageData> = liveData {
//        kotlinx.coroutines.delay(1500L)
        try {
            var imageUri = ""
            val thumbnailImageData= ThumbnailImageData()
            viewModelScope.launch {
                val defCproviderTask = async { repository.getClearImageFromCprovider(phoneNum) }
                val defDbTask = async { repository.getCallLogInfoForNum(phoneNum) }

                try {
                    var imgFromCprovider: String? = defCproviderTask.await()
                    var imgFromDb: String? = defDbTask.await()?.imageFromDb
                    if (imgFromCprovider != null) {
                        imageUri = imgFromCprovider
                        thumbnailImageData.imageFoundFrom = IMAGE_FOUND_FROM_C_PROVIDER
                        thumbnailImageData.imageStr = imageUri
                    } else if (!imgFromDb.isNullOrEmpty()) {
                        imageUri = imgFromDb
                        thumbnailImageData.imageFoundFrom = IMAGE_FOUND_FROM_DB
                        thumbnailImageData.imageStr = imageUri
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "getClearImage: exception $e")
                }
            }.join()
            emit(thumbnailImageData)
        }catch (e:Exception){
            Log.d(TAG, "getClearImage:execption $e")
        }




    }

    fun getInfoFromServer(phoneNum: String) = viewModelScope.launch{
//        var infoFromServer:ContactTable? = null
//            infoFromServer = repository?.getInfoFromServerForContact(phoneNum)
//          infoFromServer?.let { emit(it) }

    }

    fun getAgregatedContactInformation(phoneNum: String) = viewModelScope.launch {
        //get details from content provider
        var defCp:Deferred<Contact?> = async { repository.getContactDetailForNumberFromCp(phoneNum) }
        var contactForview:CntctitemForView = CntctitemForView(informationReceivedDate = Date())
        //get details from databse
        var defDb:Deferred<CallersInfoFromServer?> =  async { repository.getIndividualContactFromDb(phoneNum) }


        try {
            val infoInCprovider = defCp.await()
            val infoInDb = defDb.await()
             contactForview.firstName =  getStrinProp(infoInCprovider?.firstName, infoInDb?.firstName)
             contactForview.lastName = getStrinProp(infoInCprovider?.lastName, infoInDb?.lastName)
             contactForview.carrier = getStrinProp(infoInCprovider?.carrier, infoInDb?.carrier)
             contactForview.country = getStrinProp(infoInCprovider?.country, infoInDb?.country)
//             contactForview.lineType = getStrinProp(infoInCprovider?.lineType, infoInDb?.lineType)
             contactForview.location = getStrinProp(infoInCprovider?.location, infoInDb?.city)
            if(infoInCprovider?.firstName.isNullOrEmpty()){
                contactForview.isInfoFoundInServer = infoInDb?.isUserInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER
            }else {
                contactForview.isInInContacts = true
            }
            if(contactForview.firstName.isNullOrEmpty()){
                contactForview.firstName = formatPhoneNumber( phoneNum)
            }
            contactForview.spammCount = infoInDb?.spamReportCount?:0
            contactForViewLivedata.value  = contactForview
        }catch (e:Exception){
            Log.d(TAG, "getAgregatedContactInformation: exception $e")
        }
    }

    private fun getStrinProp(
        stringPropInCprovider: String?,
        stringPropInDB: String?
    ): String {
        if(stringPropInCprovider.isNullOrEmpty()){
            //return  info from db
            return stringPropInDB?:""
        }else {
           return  stringPropInCprovider?:""
        }
    }


//   val contact =
//       IndividualContactRepository(
//           application.applicationContext,
//           phoneNum
//       )

}