package com.nibble.hashcaller.network.search

import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.network.user.IuserService
import com.nibble.hashcaller.repository.search.SearchDTO
import com.nibble.hashcaller.view.ui.search.ManualSearchDTO
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
