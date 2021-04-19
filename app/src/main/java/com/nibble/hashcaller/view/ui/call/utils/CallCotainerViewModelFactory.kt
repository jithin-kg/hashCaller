package com.nibble.hashcaller.view.ui.call.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.work.CallContainerViewModel


/**
 * Created by Jithin KG on 29,July,2020
 */
class CallContainerViewModelFactory(
    private val callLogLiveData: CallLogLiveData?,
    private val repository: CallContainerRepository?,
    private val callersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val blockListPatternRepository: BlockListPatternRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return CallContainerViewModel(this!!.callLogLiveData!!, repository, callersInfoFromServerDAO, blockListPatternRepository) as T
    }
}