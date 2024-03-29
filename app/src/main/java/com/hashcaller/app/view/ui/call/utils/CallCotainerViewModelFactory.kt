package com.hashcaller.app.view.ui.call.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockRepository
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.dialer.util.CallLogLiveData
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel


/**
 * Created by Jithin KG on 29,July,2020
 */
class CallContainerViewModelFactory(
    private val callLogLiveData: CallLogLiveData?,
    private val repository: CallContainerRepository?,
    private val callersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return CallContainerViewModel(
            this!!.callLogLiveData!!,
            repository,
            callersInfoFromServerDAO,
            blockListPatternRepository,
            generalBlockRepository
            ) as T
    }
}