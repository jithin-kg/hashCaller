package com.hashcaller.app.network.search

import com.hashcaller.app.network.search.model.SerachRes
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.repository.search.SearchDTO
import com.hashcaller.app.view.ui.search.ManualSearchDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by Jithin KG on 25,July,2020
 */
interface ISearchService  {

    companion object{
//        const val BASE_URL: String = "http://192.168.43.84:3000/"
        const val BASE_URL: String = IuserService.BASE_URL;
    }

@POST("find/search")
suspend fun search(
    @Body phoneNumber:SearchDTO,
    @Header("Authorization") token:String
):Response<SerachRes>

    @POST("find/manualSearch")
    suspend fun searchManual(
        @Body searchBody:ManualSearchDTO,
        @Header("Authorization") token:String
    ):Response<SerachRes>

    @POST("spam/incrementTotalSpamCount")
    suspend fun incrementTotalSpamCount( @Header("Authorization")token: String)

//    @GET("getContacts")
//    fun getContacts(token: String?): Call<JsonObject?>?
}
