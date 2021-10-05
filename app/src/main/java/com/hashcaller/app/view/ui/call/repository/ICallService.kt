package com.hashcaller.app.view.ui.call.repository

import com.hashcaller.app.network.spam.hashednums
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.view.ui.call.utils.UnknownCallersInfoResponse
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
//A12218
}