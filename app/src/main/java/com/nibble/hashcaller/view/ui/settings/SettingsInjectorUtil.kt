package com.nibble.hashcaller.view.ui.settings

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope

/**
 * Created by Jithin KG on 29,July,2020
 */
object SettingsInjectorUtil {
    fun provideContactsViewModelFactory(context: Context?):SettingsViewModelFactory{
        val repository = SettingRepository(context)



        return SettingsViewModelFactory(repository)
    }

}