package com.hashcaller.app.repository.incomingcall.workers;

import android.content.Context;
import androidx.work.CoroutineWorker

import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.network.incomingcall.SuggestNameModel
import com.hashcaller.app.repository.incomingcall.IncomingCallRepository
import com.hashcaller.app.utils.auth.TokenHelper

class ThumbsDownWorker(private val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val NAME = "name"
        const val NUMBER = "number"
    }

    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var tokenHelper: TokenHelper? = TokenHelper(user)
    private val repository = IncomingCallRepository(tokenHelper)

    override suspend fun doWork(): Result {

        val response = repository.downVote(
            SuggestNameModel(
                inputData.getString(NAME)!!,
                inputData.getString(NUMBER)!!
            )
        )
        return if (response != null) {
            if (response.isSuccessful && response.code() == 200) {
                Result.success()
            } else Result.failure()
        } else {
            Result.retry()
        }
    }
}