package com.nibble.hashcaller.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by Jithin KG on 25,July,2020
 */
interface IContactsService  {
    //    Call<ResponseBody> uploadContacts(@Body ContactsUploadDTO contactsUploadDTO);

//    @POST("contacts/uploadcontacts")
//    suspend fun uploadContacts(
//        @Body contacts: ContactsListHelper?,
//        @Header("Authorization") token: String?
//    ):NetWorkResponse

//    @POST("contacts/uploadcontacts")
//    suspend fun uploadContacts(
//        @Body contacts: ContactsListHelper
//    ):Response<NetWorkResponse>

    companion object{
        const val BASE_URL: String = "http://192.168.43.84:8000/"
    }

@POST("contacts/uploadcontacts")
suspend fun uploadContacts(
    @Body contacts: List<String>
):Response<NetWorkResponse>

//    @GET("getContacts")
//    fun getContacts(token: String?): Call<JsonObject?>?
}
