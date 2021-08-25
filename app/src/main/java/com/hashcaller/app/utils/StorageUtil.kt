package com.hashcaller.app.utils

import android.os.Build

/**
 * This function make sure that app is running on SDK 29 and above
 * else return null
 */
inline fun <T> sdk29AndUp(onSdk29: () -> T):T? {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        onSdk29()
    }else {
        return null
    }
}
