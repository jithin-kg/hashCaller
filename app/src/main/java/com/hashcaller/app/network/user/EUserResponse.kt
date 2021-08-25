package com.hashcaller.app.network.user

import androidx.annotation.Keep

@Keep
 class EUserResponse {
    companion object{
        val NO_SUCH_USER = "0";
        val EXISTING_USER = "1";
    }
}