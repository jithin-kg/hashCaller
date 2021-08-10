package com.hashcaller

import androidx.annotation.Keep

@Keep
class Secrets {

    //Method calls will be added by gradle task addObfuscatedKey
    //external fun getWellHiddenSecret(packageName: String): String

    companion object {
        init {
            System.loadLibrary("secrets")
        }
    }

    external fun getbcf2a937004d5b229fdaff17b9fd6d0328d3eb80a709e8234ede7c5501af648b(packageName: String): String

    external fun getIBZQHPWG(packageName: String): String

    external fun getSGBEDOKF(packageName: String): String

    external fun managecipher(packageName: String, key:String):String

}