package com.nibble.hashcaller.repository.spam

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO
import com.nibble.hashcaller.local.db.blocklist.SpammerInfo
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import retrofit2.Response

class SpamNetworkRepository(
    private val context: Context,
    private val spamListDAO: SpamListDAO?,
    private val dataStoreRepository: DataStoreRepository
){

    private var retrofitService:ISpamService? = null
    @SuppressLint("LongLogTag")

    suspend fun report(callerInfo: ReportedUserDTo): Response<NetWorkResponse>? {
        retrofitService = RetrofitClient.createaService(ISpamService::class.java)

        val tokenManager = TokenManager(dataStoreRepository)
        val token = tokenManager.getDecryptedToken()

//        val response = retrofitService?.search(SearchDTO(phoneNum), token)
//        Log.d(TAG, "signup: ${response?.body()?.message}")


//        return response
         return retrofitService?.report(callerInfo, token)
    }

    suspend fun save(spammerInfo: SpammerInfo){
//        spamListDAO?.insert(spammerInfo)
    }

     fun getSpammerInfo(contactAddress: String): LiveData<List<SpammerInfo>>? {
        return spamListDAO?.getAllBLockListPattern()
    }

    suspend fun delete(contactAddress: String) {
        spamListDAO?.delete(contactAddress)
    }


    companion object{
        private const val TAG = "__SearchNetworkRepository"
    }
}