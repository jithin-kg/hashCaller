package com.hashcaller.app.view.ui.contacts.individualContacts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.hashcaller.app.local.db.blocklist.mutedCallers.MutedCallers
import com.hashcaller.app.local.db.contactInformation.ContactTable
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockRepository
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_C_PROVIDER
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB_GOOGLE
import com.hashcaller.app.view.ui.contacts.startSpamReportWorker
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.SPAM_THREASHOLD
import com.hashcaller.app.view.ui.sms.individual.util.EXACT_NUMBER
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
import com.hashcaller.app.view.ui.sms.individual.util.ON_COMPLETED
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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
    private val spamNetworkRepository: SpamNetworkRepository,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository

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

    fun reportSpam(phoneNum: String,
                   spammerType: Int,
                   applicationContext: Context?
    ):LiveData<Int> = liveData {

        if (phoneNum.isNotEmpty()) {
            val formattedNum = formatPhoneNumber(phoneNum)
            val listOfNums = listOf<String>(formattedNum)
            viewModelScope.launch {
                supervisorScope {
                    val as1 = async { repository?.marAsReportedByUser(listOfNums) }

                    val as2 = async {
                        addAddressToPatternsTable(listOfNums)

                    }
//                    val as4 = async { repository?.markAsSpamInSMS(contactAddress) }
                    val as3 = async { startSpamReportWorker(listOfNums, applicationContext, spammerType) }
                    try {
                        as1.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
                    try {
                        as2.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
                    try {
                        as3.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
//                    try{
//                        as4.await()
//                    }catch (e:Exception){
//                        Log.d(TAG, "blockThisAddress: $e")
//                    }

                    generalBlockRepository.marAsReportedByUserInCall(formattedNum)
                    generalBlockRepository.marAsReportedByUserInSMS(formattedNum)
                }


//            }.join()
            }.join()

            emit(ON_COMPLETED)

        }

    }

    private suspend fun startSpamReportWorker(
        markedItems: List<String>,
        applicationContext: Context?,
        spammerType: Int
    ) {
        var commanSeperatedNumbers = ""
        for((count, num) in markedItems.withIndex()){
            if(count == markedItems.size -1){
                commanSeperatedNumbers += "$num"
            }else {
                commanSeperatedNumbers += "$num,"
            }
        }
        val list = commanSeperatedNumbers.split(",")
        applicationContext?.startSpamReportWorker(commanSeperatedNumbers, spammerType)
    }

    private suspend fun addAddressToPatternsTable(markedItems: List<String>) {
        for (num in markedItems){
            blockListPatternRepository.insertPattern(
                num,
                EXACT_NUMBER )
        }
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
                val defDbTask = async { repository.getIndividualContactFromDb(phoneNum) }

                try {
                    var imgFromCprovider: String? = defCproviderTask.await()
                    var inforFromDb = defDbTask.await()

                    if (imgFromCprovider != null) {
                        imageUri = imgFromCprovider
                        thumbnailImageData.imageFoundFrom = IMAGE_FOUND_FROM_C_PROVIDER
                        thumbnailImageData.imageStr = imageUri
                    } else if (inforFromDb != null) {
                        if(inforFromDb.thumbnailImg.isNotEmpty()){
                            imageUri =inforFromDb.thumbnailImg
                                thumbnailImageData.imageFoundFrom = IMAGE_FOUND_FROM_DB
                            thumbnailImageData.imageStr = imageUri
                        }else if(inforFromDb.avatarGoogle.isNotEmpty()){
                            imageUri =inforFromDb.avatarGoogle
                            thumbnailImageData.imageFoundFrom = IMAGE_FOUND_FROM_DB_GOOGLE
                            thumbnailImageData.avatarGoogle = imageUri
                        }
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
             contactForview.hUid = infoInDb?.hUid?:""
            if(infoInCprovider?.firstName.isNullOrEmpty()){
                contactForview.isInfoFoundInServer = infoInDb?.isUserInfoFoundInServer?: INFO_NOT_FOUND_IN_SERVER
            }else {
                contactForview.isInInContacts = true
            }
            if(contactForview.firstName.isNullOrEmpty()){
                contactForview.firstName = formatPhoneNumber( phoneNum)
            }
            contactForview.isVerifiedUser = infoInDb?.isVerifiedUser?:false
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