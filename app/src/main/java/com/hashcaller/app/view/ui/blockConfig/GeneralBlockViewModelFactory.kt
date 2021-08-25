package com.hashcaller.app.view.ui.blockConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.repository.BlockListPatternRepository

class GeneralBlockViewModelFactory(
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository,
)
    :ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GeneralblockViewmodel(generalBlockRepository,
            blockListPatternRepository
            ) as T
    }
}