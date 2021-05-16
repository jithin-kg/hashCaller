package com.nibble.hashcaller.utils.auth

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.view.ui.auth.ActivityVerifyOTP
import kotlin.coroutines.suspendCoroutine

/**
 * This class helps to get token
 */
class TokenHelper(private val user: FirebaseUser?) {
    /**
     * 
     * made the call back style to suspend and coroutine
     * https://vineeth.ink/coroutine-basics-converting-callbacks-to-coroutines-207c9d59eb02#:~:text=It%20is%20fairly%20easy%20to,synchronous%20code%2C%20promising%20easier%20readability.
     */
   suspend fun getToken(): String {
       return suspendCoroutine<String> { cont->
           try {
               user?.getIdToken(true)
                   ?.addOnCompleteListener { task ->
                       if (task.isSuccessful) {
                           var token = task.result?.token
                           // Send token to your backend via HTTPS
                           if(!token.isNullOrEmpty()){
                               //returns with token, converted callback to coroutine suspend
                               cont.resumeWith(Result.success(token))
                           }

                       }else{
                          cont.resumeWith(Result.success(""))
                       }
                   }
           }catch (e:Exception){
               cont.resumeWith(Result.success(""))
               Log.d(TAG, "getToken:exception $e")
           }
       }


    }
companion object{
    const val TAG = "__TokenHelper"
}
}