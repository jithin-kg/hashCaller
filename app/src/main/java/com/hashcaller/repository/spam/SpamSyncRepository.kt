package com.hashcaller.repository.spam

import android.content.Context
import com.hashcaller.network.RetrofitClient
import com.hashcaller.network.spam.ISpamService
import com.hashcaller.view.ui.MainActivity
import com.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.hashcaller.view.utils.spam.OperatorInformationDTO

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