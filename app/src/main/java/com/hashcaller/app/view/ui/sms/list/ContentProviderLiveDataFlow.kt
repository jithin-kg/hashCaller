package com.hashcaller.app.view.ui.sms.list

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created by Jithin KG on 22,July,2020
 * this class provides live data form content providers
 */
abstract class ContentProviderLiveDataFlow<T>(
    private val context: Context,
    private val uri: Uri // The Uri on which content observer will observe
): MutableLiveData<T>(){
    private lateinit var observer: ContentObserver

    @SuppressLint("LongLogTag")
    override fun onActive() {
        try {
            GlobalScope.launch {
                postValue(getContentProviderValue(null)) // we are posting the initial value of the
            }

            //content provider to the observer of our live data
            observer = object : ContentObserver(null){
                override fun onChange(selfChange: Boolean) {
                    //calling post value to set the latest value onto the ui controller
                    GlobalScope.launch {
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