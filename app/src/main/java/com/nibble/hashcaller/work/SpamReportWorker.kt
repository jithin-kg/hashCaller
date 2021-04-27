package com.nibble.hashcaller.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.nibble.hashcaller.utils.SpamNetworkRepository as SpamNetworkRepository1

class SpamReportWorker (private val context: Context, private val params:WorkerParameters ) :
    CoroutineWorker(context, params) {
    private val retrofitService: ISpamService =
        RetrofitClient.createaService(ISpamService::class.java)
    private val sp =
        context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
    private val repository: SpamNetworkRepository1 = SpamNetworkRepository1(context)


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val num = inputData.getString(CONTACT_ADDRES)
        val report = ReportedUserDTo(num!!, "kerala", "0", "1")
//        repository.report(report)
        try {
            val tokenManager = TokenManager(sp)
            val token = tokenManager.getToken()

           val response =  retrofitService?.report(report, token)
            if(response.code() in 500..599){
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.d(TAG, "doWork: ")
            return@withContext Result.retry()
        }

        return@withContext Result.success()
    }
companion object{
    const val TAG ="__SpamReportWorker"
}

}