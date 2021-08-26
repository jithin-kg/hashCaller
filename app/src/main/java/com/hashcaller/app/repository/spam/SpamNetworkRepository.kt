package com.hashcaller.app.repository.spam

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.blocklist.SpamListDAO
import com.hashcaller.app.local.db.blocklist.SpammerInfo
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.contact.NetWorkResponse
import com.hashcaller.app.network.spam.ISpamService
import com.hashcaller.app.network.spam.ReportedUserDTo
import com.hashcaller.app.utils.auth.TokenManager
import retrofit2.Response

class SpamNetworkRepository(
    private val context: Context,
    private val spamListDAO: SpamListDAO?,
    private val dataStoreRepository: DataStoreRepository
){

    private var retrofitService:ISpamService? = null

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