package com.nibble.hashcaller.view.ui.hashworker

import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.call.repository.ICallService
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
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