package com.hashcaller.app.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.blocklist.SpamThresholdUpdatedDate
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.spam.ISpamService
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.Constants.Companion.isDataOutdated
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.call.work.CallNumUploadWorker.Companion.NUMBER_OF_DAYS
import java.lang.Exception
import java.util.*

class SpamThresholdUpdateWorker(private  val context: Context, private val params: WorkerParameters) :
 CoroutineWorker(context, params){
    private val retrofitService: ISpamService =
        RetrofitClient.createaService(ISpamService::class.java)
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)
    val spamThresholdUpdateDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamThresholdUpdateDAO() }
    val dataStoreRepository = DataStoreRepository(context.tokeDataStore)

    override suspend fun doWork(): Result {
        try {
            var isDataOutdated = false

            val currentThresholdValue:Int? = dataStoreRepository.getInt(
                SPAM_THRESHOLD
            )

            val queryRes = spamThresholdUpdateDAO.find()
            isDataOutdated = if(queryRes.isNotEmpty()){
                //check date difference
                isDataOutdated(queryRes[0].date,DATA_OUTDATE_LIMIT )

            }else {
                true
            }
            Log.d(TAG, "doWork: $currentThresholdValue")
            if(currentThresholdValue == null)
                isDataOutdated = true

            if(isDataOutdated){
                val token = tokenHelper?.getToken()

                if( token != null){
                    isDataOutdated(Date(), NUMBER_OF_DAYS)
                    val result = retrofitService.getSpamThreshold(token)
                    result.body()?.data?.let {
                        spamThresholdUpdateDAO.deleteAll()
                        dataStoreRepository.setInt( it.threshold, SPAM_THRESHOLD)
                        spamThresholdUpdateDAO.insert(SpamThresholdUpdatedDate(null, Date()))
                    }
                }else {
                    return Result.retry()
                }
            }

            return Result.success()
        }catch (e:Exception){
            return Result.retry()
        }
    }

    companion object {
        const val TAG = "__SpamThresholdUpdateWorker"
        const val DATA_OUTDATE_LIMIT =7
    }
}