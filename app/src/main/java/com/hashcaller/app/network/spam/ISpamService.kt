package com.hashcaller.app.network.spam

import com.hashcaller.app.network.contact.NetWorkResponse
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.view.ui.sms.work.UnknownSMSsendersInfoResponse
import com.hashcaller.app.view.utils.spam.OperatorInformationDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ISpamService {
    companion object{
//        const val BASE_URL: String = "http://192.168.43.84:3000/"
        const val BASE_URL: String = IuserService.BASE_URL
    }

    @POST("spam/report")
    suspend fun report(
        @Body userInfo :ReportedUserDTo,
        @Header ("Authorization") token:String
    ):Response<NetWorkResponse>


    @POST("spam/getReleventSpamInfo")
    suspend fun syncSpamListOfOperator(
        @Body ops: MutableList<OperatorInformationDTO>,
        @Header ("Authorization") token: String
    ):Response<NetWorkResponse>

    @POST("multipleNumberSearch/getDetailsForNumbers")
    suspend fun getInfoForThesePhoneNumbers(
        @Body numbers: hashednums,
        @Header ("Authorization") token: String
    ):Response<UnknownSMSsendersInfoResponse>

}