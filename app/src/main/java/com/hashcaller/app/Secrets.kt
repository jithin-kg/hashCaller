package com.hashcaller.app

import android.util.Log
import androidx.annotation.Keep
import java.security.MessageDigest

@Keep
class Secrets {

    //Method calls will be added by gradle task addObfuscatedKey
    //external fun getWellHiddenSecret(packageName: String): String

    companion object {
        const val TAG ="__Secrets"
//        init {
//            System.loadLibrary("secrets")
//        }
    }




//    external fun managecipher(packageName: String, key:String):String
//    https://stackoverflow.com/questions/50425424/sha-256-mismatch-between-nodejs-and-java-code/50425781
    fun managecipher(packageName: String?, key:String):String{
        val md = MessageDigest.getInstance("SHA-256")
        md.update(key.toByteArray())
        val byteData: ByteArray = md.digest()

        val hexString = StringBuffer()
        for (i in byteData.indices) {
            val hex = Integer.toHexString(0xff and byteData[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
    Log.d(TAG, "managecipher: ${hexString.toString()}")
    return hexString.toString()
    }

}