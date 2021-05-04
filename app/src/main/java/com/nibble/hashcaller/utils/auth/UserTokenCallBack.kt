package com.nibble.hashcaller.utils.auth

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception

class UserTokenCallBack(private val user:FirebaseUser) {
    fun onSignedInInititalize(callback : (String, Exception?) ->Unit) {
        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var idToken = task.result?.token
                    // Send token to your backend via HTTPS
                    idToken?.let { callback(it, null) }

                }else{
                    callback("", task.exception)
                }
        }
    }

}