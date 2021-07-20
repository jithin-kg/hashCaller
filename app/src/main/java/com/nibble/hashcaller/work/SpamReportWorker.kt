package com.nibble.hashcaller.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.network.RetrofitClient
import com.nibble.hashcaller.network.spam.ISpamService
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.utils.Constants
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.sms.individual.util.SPAMMER_TYPE_SCAM
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.nibble.hashcaller.utils.SpamNetworkRepository as SpamNetworkRepository1

class SpamReportWorker (private val context: Context, private val params:WorkerParameters ) :
    CoroutineWorker(context, params) {
    private val retrofitService: ISpamService =
        RetrofitClient.createaService(ISpamService::class.java)
    private val sp =
        context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
    private val  dataStoreRepostory = DataStoreRepository(context.tokeDataStore)
    private val repository: SpamNetworkRepository1 = SpamNetworkRepository1(context, dataStoreRepostory)
    private val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    private val countryCodeHelper = CountrycodeHelper(context)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val num = inputData.getString(CONTACT_ADDRES)
        val countryIso = countryCodeHelper.getCountryISO()
        var hasehdNum:String? = num?.let { libCountryHelper.getES164Formatednumber(formatPhoneNumber(it), countryIso) }?.let { Secrets().managecipher(context.packageName, it) }
//        hasehdNum = hashUsingArgon(hasehdNum)
        val spammerType = inputData.getInt( Constants.SPAMMER_TYPE, SPAMMER_TYPE_SCAM)
        val report = hasehdNum?.let { ReportedUserDTo(it, CountrycodeHelper(context).getCountrycode(), spammerType.toString(),) }
//        repository.report(report)
        try {
            val tokenHelper = TokenHelper( FirebaseAuth.getInstance().currentUser)
            val token = tokenHelper?.getToken()
           val response = token?.let { report?.let { it1 -> retrofitService?.report(it1, it) } }
            if(response?.code() in 500..599){
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