package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import androidx.lifecycle.MutableLiveData

/**
 * Created by Jithin KG on 22,July,2020
 * this class provides live data form content providers
 */
abstract class ContentProviderLiveData<T>(
    private val context: Context,
    private val uri: Uri // The Uri on which content observer will observe
): MutableLiveData<T>(){
    private lateinit var observer: ContentObserver

    override fun onActive() {
        postValue(getContentProviderValue(null)) // we are posting the initial value of the
        //content provider to the observer of our live data
        observer = object : ContentObserver(null){
            override fun onChange(selfChange: Boolean) {
                //calling post value to set the latest value onto the ui controller
                postValue(getContentProviderValue(null))
            }
        }
        context.contentResolver.registerContentObserver(uri, true, observer)
    }

    override fun onInactive() {
        context.contentResolver.unregisterContentObserver(observer)
    }
    abstract fun getContentProviderValue(text:String?) : T
}