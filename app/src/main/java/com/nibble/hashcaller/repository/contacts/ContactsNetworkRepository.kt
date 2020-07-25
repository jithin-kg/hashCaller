package com.nibble.hashcaller.repository.contacts

import android.util.Log
import com.google.gson.Gson
import com.nibble.hashcaller.network.ContactsListHelper
import com.nibble.hashcaller.network.IContactsService
import com.nibble.hashcaller.network.NetWorkResponse
import com.nibble.hashcaller.network.RetrofitClient
import kotlin.math.log

/**
 * Created by Jithin KG on 25,July,2020
 */
class ContactsNetworkRepository {

      private var retrofitService:IContactsService? = null




    suspend fun uploadContacts(contacts:MutableList<ContactUploadDTO>) {
        // Execute web request through coroutine call adapter & retrofit
//        val webResponse = WebAccess.partsApi.getPartsAsync().await()

            retrofitService = RetrofitClient.createaService(IContactsService::class.java)

            val contactListObject = ContactsListHelper(contacts)

        Log.d(TAG, "uploadContacts: ")
            val list:MutableList<String> = ArrayList<String>()
        list.add("hi")
        val uploadContacts = retrofitService?.uploadContacts(list)
        Log.d(TAG, "after uploading $uploadContacts")
        val isSuccess = uploadContacts?.isSuccessful ?: false
        if(isSuccess){
            val result =uploadContacts?.body()?.message
//            val topic = Gson().fromJson(result, NetWorkResponse::class.java)
//            Log.d(TAG, "uploadContacts: $topic")
            Log.d(TAG, "uploadContacts: ${uploadContacts?.code()}")
            Log.d(TAG, "uploadContacts: $result")
            val r = uploadContacts?.message()
            Log.d(TAG, "uploadContacts: $r")


        }else{
            Log.d(TAG, "uploadContacts:failure ")
        }


    }
    companion object{
        private const val TAG = "__ContactsUploadWorker"
    }
}