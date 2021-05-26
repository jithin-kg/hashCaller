package com.nibble.hashcaller.view.ui.sms.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.blockConfig.GeneralBlockRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel

/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSListViewModelFactory(
    private val SMSLiveData: SMSLiveData?,
    private val repository: SMSLocalRepository?,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model
        return SMSViewModel(this!!.SMSLiveData!!, repository, blockListPatternRepository, generalBlockRepository) as T
    }
}