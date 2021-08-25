package com.hashcaller.app.network.contact

import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.repository.contacts.ContactsSaveDTO
import com.hashcaller.app.repository.contacts.ContactsSyncDTO
import com.hashcaller.app.view.ui.call.utils.UnknownCallersInfoResponse
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
//        const val BASE_URL: String = "http://192.168.43.84:3000/"
        const val BASE_URL: String = IuserService.BASE_URL;
    }

//@POST("contacts/uploadcontacts")
//suspend fun uploadContacts(
//    @Body contacts: ContactsSyncDTO,
//    @Header("Authorization") token:String
//):Response<SerachRes>

    @POST("contacts/uploadcontacts")
    suspend fun uploadContacts(
        @Body contacts: ContactsSyncDTO,
        @Header("Authorization") token:String
    ):Response<UnknownCallersInfoResponse>

    @POST("contacts/savecontacts")
    suspend fun uploadContactsOf1000(
        @Body contacts: ContactsSaveDTO,
        @Header("Authorization") token:String
    ):Response<ContactsSaveResponse>

//    @GET("getContacts")
//    fun getContacts(token: String?): Call<JsonObject?>?
}
