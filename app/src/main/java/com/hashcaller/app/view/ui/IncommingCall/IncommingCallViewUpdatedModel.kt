package com.hashcaller.app.view.ui.IncommingCall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.hashcaller.app.repository.incomingcall.IncomingCallRepository
import com.hashcaller.app.repository.incomingcall.workers.SuggestNameWorker
import com.hashcaller.app.repository.incomingcall.workers.ThumbsDownWorker
import com.hashcaller.app.repository.incomingcall.workers.ThumbsUpWorker
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class IncommingCallViewUpdatedModel(
    val app: Application,
    private val repository: IncomingCallRepository
) : AndroidViewModel(app) {
    class Factory(
        private val application: Application, private val repository: IncomingCallRepository

    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return IncommingCallViewUpdatedModel(application, repository) as T
        }
    }


    fun suggestName(name: String, number: String)  = viewModelScope.launch {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val data = Data.Builder().putString(SuggestNameWorker.NAME, name)
            .putString(SuggestNameWorker.NUMBER, number).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SuggestNameWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)

//        repository.updateSuggestedName(name , number)

    }

    fun upvote(name: String, number: String) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val data = Data.Builder().putString(ThumbsUpWorker.NAME, name)
            .putString(ThumbsUpWorker.NUMBER, number).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ThumbsUpWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)
    }

    fun downvote(name: String, number: String) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val data = Data.Builder().putString(ThumbsDownWorker.NAME, name)
            .putString(ThumbsDownWorker.NUMBER, number).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ThumbsDownWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(app).enqueue(oneTimeWorkRequest)
    }

}
