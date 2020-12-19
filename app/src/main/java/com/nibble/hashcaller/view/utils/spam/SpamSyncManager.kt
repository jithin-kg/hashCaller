package com.nibble.hashcaller.view.utils.spam

import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.repository.spam.SpamSyncRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.MainActivity

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