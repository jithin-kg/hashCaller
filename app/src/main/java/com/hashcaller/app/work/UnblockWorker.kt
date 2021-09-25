package com.hashcaller.app.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.Secrets
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.spam.ISpamService
import com.hashcaller.app.network.spam.SpamNumbersDTO
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ADDRES
import com.hashcaller.app.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.hashcaller.app.view.ui.sms.individual.util.SPAMMER_TYPE_NOT_SPECIFIC
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hashcaller.app.utils.SpamNetworkRepository as SpamNetworkRepository1

class UnblockWorker (private val context: Context, private val params:WorkerParameters ) :
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

        val commaSeperatedNums = inputData.getString(CONTACT_ADDRES)
        val numsList = commaSeperatedNums?.split(",")
        val hashedNumsList : MutableList<String>  = mutableListOf()
        val countryIso = countryCodeHelper.getCountryISO()
        if (numsList != null) {
            for(num in numsList){
                var hasehdNum:String? = num?.let { libCountryHelper.getES164Formatednumber(formatPhoneNumber(it), countryIso) }?.let { Secrets().managecipher(context.packageName, it) }
                if (hasehdNum != null) {
                    hashedNumsList.add(hasehdNum)
                }
            }
        }

        val spammerType =SPAMMER_TYPE_NOT_SPECIFIC
        if(hashedNumsList.isNotEmpty()){
            try {
                val report = SpamNumbersDTO(hashedNumsList, CountrycodeHelper(context).getCountrycode(), spammerType.toString(),)
                val tokenHelper = TokenHelper( FirebaseAuth.getInstance().currentUser)
                val token = tokenHelper?.getToken()
                val response = token?.let { report?.let { it1 -> retrofitService?.unblock(it1, it) } }
                if(response?.code() in 500..599){
                    return@withContext Result.retry()
                }else if(response?.code() == 200){
                    return@withContext Result.success()
                }
            } catch (e: Exception) {
                Log.d(TAG, "doWork: ")
                return@withContext Result.retry()
            }
        }
        return@withContext Result.success()
    }
companion object{
    const val TAG ="__SpamReportWorker"
}

}