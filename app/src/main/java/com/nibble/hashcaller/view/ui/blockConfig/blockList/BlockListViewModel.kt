package com.nibble.hashcaller.view.ui.blockConfig.blockList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import kotlinx.coroutines.*

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListViewModel(application: Application) : AndroidViewModel(application) {
    private  val blockListPatternRepository: BlockListPatternRepository

    val allblockedList: LiveData<List<BlockedListPattern>>

    init {
        val blockedLIstDao = HashCallerDatabase.getDatabaseInstance(application).blocklistDAO()
        val mutedCallersDao = HashCallerDatabase.getDatabaseInstance(application).mutedCallersDAO()

        blockListPatternRepository = BlockListPatternRepository(blockedLIstDao, mutedCallersDao)

        allblockedList = blockListPatternRepository.allBlockedList
    }

    //creating a coroutine to call suspending function
    //view models have their on scope we are launching coroutine on the viewmodelScope
    fun insert(blockedListPattern: BlockedListPattern) = viewModelScope.launch(Dispatchers.IO){
        blockListPatternRepository.insert(blockedListPattern)

    }
    fun delete(blockedListPattern: String) = viewModelScope.launch(Dispatchers.IO){
        blockListPatternRepository.delete(blockedListPattern)

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