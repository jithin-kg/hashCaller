package com.hashcaller.app.view.ui.blockConfig.blockList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

/**
 * Created by Jithin KG on 03,July,2020
 */
//todo extends from viewmodel
class BlockListViewModel(application: Application) : AndroidViewModel(application) {
    private var permsnBadgeDismissed = false

    private  val blockListPatternRepository: BlockListPatternRepository

    val allblockedList: LiveData<MutableList<BlockedListPattern>>?
    fun setDismissedState(isDismissed:Boolean){
        permsnBadgeDismissed = isDismissed
    }
    fun getDismissedState(): Boolean {
        return permsnBadgeDismissed
    }

    init {
        //todo move theese to injector util
        val blockedLIstDao = HashCallerDatabase.getDatabaseInstance(application).blocklistDAO()
        val mutedCallersDao = HashCallerDatabase.getDatabaseInstance(application).mutedCallersDAO()
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(application).getCountryISO()

        blockListPatternRepository = BlockListPatternRepository(
            blockedLIstDao,
            mutedCallersDao,
            libCountryHelper,
            countryCodeIso
        )

        allblockedList = blockListPatternRepository.allBlockedList
    }

    //creating a coroutine to call suspending function
    //view models have their on scope we are launching coroutine on the viewmodelScope
    fun insert(newPattern: String, patterntype: Int):  LiveData<Int> = liveData{
        blockListPatternRepository.insertPattern(newPattern, patterntype).apply {
            emit( OPERATION_COMPLETED)
        }

    }
    fun delete(blockedListPattern: String, type: Int) :LiveData<Int> = liveData{
        blockListPatternRepository.delete(blockedListPattern, type)
        emit(OPERATION_COMPLETED)

    }
//    fun getVal(){
//            CoroutineScope(IO).launch {
//                val result = async { blockListPatternRepository.getListOfdata() }.await()
//                Log.d("MyviewModel", "getVal: $result")
//            }
//
//    }
//    fun  getBlockedList(){
//        CoroutineScope(IO).launch {  }
//    }

//    companion object {
//
//    }
}