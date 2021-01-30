package com.nibble.hashcaller.view.ui.call.repository

import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.view.ui.call.utils.UnknownCallersInfoResponse
import com.nibble.hashcaller.view.ui.sms.work.UnknownSMSsendersInfoResponse
import com.nibble.hashcaller.view.utils.spam.OperatorInformationDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ICallService {
    companion object{
//        const val BASE_URL: String = "http://192.168.43.84:3000/"
        const val BASE_URL: String = IuserService.BASE_URL
    }



    @POST("call/getDetailsForNumbers")
    suspend fun getInfoForThesePhoneNumbers(
        @Body numbers: hashednums,
        @Header ("Authorization") token: String
    ):Response<UnknownCallersInfoResponse>

}