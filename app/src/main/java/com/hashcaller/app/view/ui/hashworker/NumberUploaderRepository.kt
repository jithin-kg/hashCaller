package com.hashcaller.app.view.ui.hashworker

import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.repository.ICallService
import com.hashcaller.app.view.ui.call.utils.UnknownCallersInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class NumberUploaderRepository(private val tokenHelper: TokenHelper?) {
    val  retrofitService = RetrofitClient.createaService(ICallService::class.java)
    suspend fun uploadNumbersToGetInfo(phoneNumberListToBeUPloaded: hashednums): Response<UnknownCallersInfoResponse>?  = withContext( Dispatchers.IO){
        val token = tokenHelper?.getToken()

        var response: Response<UnknownCallersInfoResponse>? = null
        token?.let {
            response = retrofitService!!.getInfoForThesePhoneNumbers(phoneNumberListToBeUPloaded, token)
        }

        return@withContext response
    }

}