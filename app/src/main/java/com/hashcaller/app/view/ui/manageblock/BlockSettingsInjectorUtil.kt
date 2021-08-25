package com.hashcaller.app.view.ui.manageblock

import android.content.Context

/**
 * Created by Jithin KG on 29,July,2020
 */
object BlockSettingsInjectorUtil {
    fun provideContactsViewModelFactory(context: Context?):BlockSettingsViewModelFactory{


        return BlockSettingsViewModelFactory()
    }

}