package com.hashcaller.app.utils.updatemanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UpdateManagerViewmodelFactory(
    private val context: Context,
    private val repository: UpdateManagerRepository
) : ViewModelProvider.NewInstanceFactory(){
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            //preparing view model
            return UpdateManagerViewmodel(context, repository ) as T
        }
}