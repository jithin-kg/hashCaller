package com.hashcaller.app.view.ui.hashworker

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HasherViewmodel(private val repository:HashedDataRepository) :ViewModel() {

    var hashedNumbersLiveData: LiveData<List<HashedNumber>?> = repository.getLivedata()
    override fun onCleared() {
        super.onCleared()
    }

    fun doWork() = viewModelScope.launch {
//        Log.d(TAG, "doWork: starting work")
//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//
//        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(HashedUploadWorker::class.java)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance().enqueue(oneTimeWorkRequest)
    }
companion object{
    const val TAG = "__HasherViewmodel"
}
}