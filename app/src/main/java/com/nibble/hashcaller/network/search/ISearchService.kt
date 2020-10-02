package com.nibble.hashcaller.network.search

import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.repository.search.SearchDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by Jithin KG on 25,July,2020
 */
interface ISearchService  {

    companion object{
        const val BASE_URL: String = "http://192.168.43.84:3000/"
    }

@POST("find/search")
suspend fun search(
    @Body phoneNumber:SearchDTO,
    @Header("Authorization") token:String
):Response<SerachRes>

//    @GET("getContacts")
//    fun getContacts(token: String?): Call<JsonObject?>?
}
