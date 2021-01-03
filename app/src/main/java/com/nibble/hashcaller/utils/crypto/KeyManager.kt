package com.nibble.hashcaller.utils.crypto

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.nibble.hashcaller.repository.cipher.CipherNetworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * saves public keys received from server in encrypted shared preferences
 */
object KeyManager {

    fun isKeyPresent(context:Context){

    }


    fun getKey(){
    }

    fun saveInSharedPreferences(context: Context, key:String){
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encSharedPref = EncryptedSharedPreferences.create(
            "my_secret_prefs",
            keyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val editor = encSharedPref.edit()
        editor.putString("keyOne",key)
        editor.apply()
        editor.commit()

    }

    fun setCipherInSharedPreferences(context: Context) {
        //Todo move these calls to a viewmodel of mainactivity
        val repository = CipherNetworkRepository(context)
        GlobalScope.launch {
            val response  = repository.getCipher()
            val pubkey = response?.body()?.cipher
            Log.d(TAG, "cipher is $pubkey")
            //save the pkey in secured sharedpref
            saveInSharedPreferences(context, pubkey!!)
        }
    }

    fun isKeyStored(context:Context): Boolean {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val encSharedPref = EncryptedSharedPreferences.create(
            "my_secret_prefs",
            keyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
       val reslt  = encSharedPref.getString("keyOne","no")
        if(reslt != "no")
            return true
        return false
    }

    const val TAG = "__KeyManager"
}