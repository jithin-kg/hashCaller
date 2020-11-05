package com.nibble.hashcaller.network.contact

import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import retrofit2.Response
import retrofit2.http.Body
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
        const val BASE_URL: String = "http://192.168.43.110:3000/"
    }

@POST("contacts/uploadcontacts")
suspend fun uploadContacts(
    @Body contacts: List<ContactUploadDTO>,
    @Header("Authorization") token:String
):Response<NetWorkResponse>

//    @GET("getContacts")
//    fun getContacts(token: String?): Call<JsonObject?>?
}
