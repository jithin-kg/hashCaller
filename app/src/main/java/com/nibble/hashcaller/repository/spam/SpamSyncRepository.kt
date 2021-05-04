package com.nibble.hashcaller.repository.spam

import android.content.Context
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.utils.spam.OperatorInformationDTO

class SpamSyncRepository {
    private var retrofitService:ISpamService? = null

    suspend fun sync(operatorInformations: MutableList<OperatorInformationDTO>, context: MainActivity) {
        retrofitService = RetrofitClient.createaService(ISpamService::class.java)
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

//        val tokenManager = TokenManager(sp)
//        val token = tokenManager.getToken()

//        val response = retrofitService?.search(SearchDTO(phoneNum), token)
//        Log.d(TAG, "signup: ${response?.body()?.message}")


//        return response
//        retrofitService?.syncSpamListOfOperator(operatorInformations, token)
    }
}