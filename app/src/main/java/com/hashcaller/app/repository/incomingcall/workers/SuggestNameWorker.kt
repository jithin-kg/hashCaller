package com.hashcaller.app.repository.incomingcall.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.Secrets
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.network.incomingcall.SuggestNameModel
import com.hashcaller.app.repository.incomingcall.IncomingCallRepository
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber

class SuggestNameWorker(private val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val NAME = "name"
        const val NUMBER = "number"
    }

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)
    val countryCodeIso = CountrycodeHelper(context).getCountryISO()

    val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
    val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }

    //
    private val repository : IncomingCallRepository = IncomingCallRepository(
        tokenHelper,
        callerInfoFromServerDAO,
        libCountryHelper,
        countryCodeIso
    )

    override suspend fun doWork(): Result {
        try {
            val formatedNum = libCountryHelper.getES164Formatednumber(formatPhoneNumber(inputData.getString(NUMBER)!!), countryCodeIso)
            val hashedNum  = Secrets().managecipher(null, formatedNum)
            val response = repository.suggestName(
                SuggestNameModel(
                    inputData.getString(NAME)!! ,
                    hashedNum
                )
            )
            return if (response != null) {
                if (response.isSuccessful && response.code() == 200) {
                    Result.success()
                } else Result.failure()
            } else {
                Result.retry()
            }
        }catch (e:Exception){
            return Result.retry()
        }

    }
}