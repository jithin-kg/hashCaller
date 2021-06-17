package com.nibble.hashcaller.view.ui.contacts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.view.ui.call.db.CallLogAndInfoFromServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created by Jithin KG on 22,July,2020
 * this class provides live data form content providers
 */
abstract class ContentProviderLiveData<T>(
    private val context: Context,
    private val uri: Uri, // The Uri on which content observer will observe, lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope){}, lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope){}
    private val scope: LifecycleCoroutineScope
): MutableLiveData<T>(){
    private lateinit var observer: ContentObserver

    @SuppressLint("LongLogTag")
    override fun onActive() {
        try {
            scope.launchWhenStarted {
                postValue(getContentProviderValue(null)) // we are posting the initial value of the
            }

            //content provider to the observer of our live data
            observer = object : ContentObserver(null){
                override fun onChange(selfChange: Boolean) {
                    //calling post value to set the latest value onto the ui controller
                    scope.launch {
                        postValue(getContentProviderValue(null))
                    }
                }
            }
            context.contentResolver.registerContentObserver(uri, true, observer)
        }catch (e:Exception){
            Log.d(TAG, "onActive: execption $e ")
        }
    }

    override fun onInactive() {
        context.contentResolver.unregisterContentObserver(observer)
    }
    abstract suspend fun getContentProviderValue(text:String?) : T

    companion object{
        const val TAG = "__ContentProviderLiveData"
    }
}