package com.hashcaller.view.utils.spam

import com.hashcaller.repository.spam.SpamSyncRepository
import com.hashcaller.view.ui.MainActivity

/**
 * handles spammer list according to sim operator
 * and other relevent information regarding location, other ..
 */
object SpamSyncManager {

    fun setSimOpeartor() {

    }

     fun sync(
        operatorInformtaions: MutableList<OperatorInformationDTO>,
        spamSyncRepository: SpamSyncRepository,
        context: MainActivity
    ) {
//       spamSyncRepository.sync(operatorInformtaions, context)
    }

}