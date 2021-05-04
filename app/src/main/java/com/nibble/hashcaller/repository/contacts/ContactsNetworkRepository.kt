package com.nibble.hashcaller.repository.contacts


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.network.contact.IContactsService
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.ContactsUploadResponse
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EncryptorObject
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import retrofit2.Response

import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Jithin KG on 25,July,2020
 */
class ContactsNetworkRepository (private val context: Context){

//    private var retrofitService: IContactsService? = null
    private var keyStore: KeyStore? = null
    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private var decryptor: Decryptor? = null
    private val TAG = "__ContactsNetworkRepository"
    private var retrofitService : IContactsService?= RetrofitClient.createaService(IContactsService::class.java)

    init {
        initKeyStore()
    }


    @SuppressLint("LongLogTag")
    suspend fun
            uploadContacts(
        contacts: ContactsSyncDTO
    ): Response<ContactsUploadResponse>? {
        // Execute web request through coroutine call adapter & retrofit
//        val webResponse = WebAccess.partsApi.getPartsAsync().await()


//            val contactListObject =
//                ContactsListHelper(contacts)
//            val list:MutableList<String> = ArrayList<String>()
//            list.add("hi")
        var token = ""
        try {
//            decryptor = Decryptor()
            val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
            val tokenManager = TokenManager(sp, DataStoreRepository(context))
            token = tokenManager.getToken()
//            token = decryptor?.decryptData(
//                SAMPLE_ALIAS,
//                EncryptorObject.encryption,
//                EncryptorObject.iv
//            ).toString()

        } catch (e: Exception) {
            Log.e(TAG, "decryptData() called with: " + e.message, e)
        }


        return retrofitService?.uploadContacts(contacts, token)


//        val decryptFromStringToke = decryptor.decryptFromStringToke(SAMPLE_ALIAS, tkn.toString())

//        var tokenManager = TokenManager()
//        Log.d(TAG, "uploadContacts: ${decryptFromStringToke.toString()}")

//        val response = retrofitService?.uploadContacts(contacts, token)


//        val isSuccess = response?.isSuccessful ?: false
//        if(isSuccess){
//            val result =response?.body()?.message
////            val topic = Gson().fromJson(result, NetWorkResponse::class.java)
////            Log.d(TAG, "uploadContacts: $topic")
////            Log.d(TAG, "uploadContacts: ${response?.code()}")
////            Log.d(TAG, "uploadContacts: $result")
//            val r = response?.message()
////            Log.d(TAG, "uploadContacts: $r")
//            Log.d(TAG, "phone number: ${response?.body()!!.cntcts[0]?.phoneNumber}")
//
//
//        }else{
//            Log.d(TAG, "uploadContacts:failure ")
//        }

    }
    companion object{
        private const val TAG = "__ContactsNetworkRepository"
    }
    @Throws(Exception::class)
    private fun decrypt(raw: ByteArray, encrypted: ByteArray): ByteArray? {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE,getSecretKey(SAMPLE_ALIAS))
        return cipher.doFinal(encrypted)
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class
    )
    private fun initKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore!!.load(null)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        UnrecoverableEntryException::class,
        KeyStoreException::class
    )
    private fun getSecretKey(alias: String): SecretKey? {
        return (keyStore!!.getEntry(
            alias,
            null
        ) as KeyStore.SecretKeyEntry).secretKey


    }
}