package com.hashcaller.view.ui.settings

import android.content.Context

/**
 * Created by Jithin KG on 29,July,2020
 */
object SettingsInjectorUtil {
    fun provideContactsViewModelFactory(context: Context?):SettingsViewModelFactory{
        val repository = SettingRepository(context)



        return SettingsViewModelFactory(repository)
    }

}