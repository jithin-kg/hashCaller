package com.hashcaller.app.view.ui.IncommingCall

import android.util.Log
import androidx.lifecycle.*
import com.hashcaller.app.network.incomingcall.SuggestNameModel
import com.hashcaller.app.repository.incomingcall.IncomingCallRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class IncommingCallViewUpdatedModel(
    private val repository: IncomingCallRepository
) : ViewModel() {
    class Factory(
        private val repository: IncomingCallRepository

    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return IncommingCallViewUpdatedModel(repository) as T
        }
    }


    fun suggestName(name: String, number: String): LiveData<Boolean> {
        val out = MutableLiveData<Boolean>()

        viewModelScope.launch(Dispatchers.IO) {


            val response = repository.suggestName(SuggestNameModel(name, number))
            if (response?.isSuccessful == true && response.code() == 200) {
                Log.d("``TAG``", "suggestName: ${response.body()?.message}")
                out.postValue(true)
            } else {
                out.postValue(false)
            }
        }
        return out
    }

    fun upvote(name: String, number: String): LiveData<Boolean> {
        val out = MutableLiveData<Boolean>()

        viewModelScope.launch(Dispatchers.IO) {


            val response = repository.upvote(SuggestNameModel(name, number))
            if (response?.isSuccessful == true && response.code() == 200) {
                Log.d("``TAG``", "suggestName: ${response.body()?.message}")
                out.postValue(true)
            } else {
                out.postValue(false)
            }
        }
        return out
    }

    fun downvote(name: String, number: String): LiveData<Boolean> {
        val out = MutableLiveData<Boolean>()

        viewModelScope.launch(Dispatchers.IO) {


            val response = repository.downVote(SuggestNameModel(name, number))
            if (response?.isSuccessful == true && response.code() == 200) {
                Log.d("``TAG``", "suggestName: ${response.body()?.message}")
                out.postValue(true)
            } else {
                out.postValue(false)
            }
        }
        return out
    }

}
