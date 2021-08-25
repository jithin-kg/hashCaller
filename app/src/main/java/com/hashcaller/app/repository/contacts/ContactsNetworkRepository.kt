package com.hashcaller.app.repository.contacts


import android.annotation.SuppressLint
import android.content.Context
import com.hashcaller.app.network.contact.IContactsService
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.contact.ContactsSaveResponse
import com.hashcaller.app.utils.auth.Decryptor
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.call.utils.UnknownCallersInfoResponse
import retrofit2.Response

import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Jithin KG on 25,July,2020
 */
class ContactsNetworkRepository(private val context: Context,
                                private val tokenHelper: TokenHelper?){

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
    ): Response<UnknownCallersInfoResponse>? {

        var token:String? = tokenHelper?.getToken()
        return token?.let { retrofitService?.uploadContacts(contacts, it) }
    }

    suspend fun
            uploadContactsOf1000(
        contacts: ContactsSaveDTO
    ): Response<ContactsSaveResponse>? {

        var token:String? = tokenHelper?.getToken()
        return token?.let { retrofitService?.uploadContactsOf1000(contacts, it) }
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