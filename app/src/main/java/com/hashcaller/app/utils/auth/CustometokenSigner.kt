package com.hashcaller.app.utils.auth

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.suspendCoroutine


class CustometokenSigner(private val mAuth:FirebaseAuth,private val activity:AppCompatActivity ) {

    suspend fun signInWithCustomToken(mCustomToken:String?): String {
        return suspendCoroutine<String> {continuation ->
            if (mCustomToken != null) {
                mAuth.signInWithCustomToken(mCustomToken)
                    .addOnCompleteListener(activity,
                        OnCompleteListener<AuthResult?> { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCustomToken:success")
                                val user: FirebaseUser? = mAuth.getCurrentUser()
                                continuation.resumeWith(Result.success(""))
                                //                        updateUI(user)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCustomToken:failure", task.exception)

                                Toast.makeText(
                                    activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                continuation.resumeWith(task.exception?.let { Result.failure(it) }!!)

                                //                        updateUI(null)
                            }
                        })
            }

//            continuation.resumeWith(Result.success(""))
        }

    }
    companion object{
        const val TAG = "__CustometokenSinger"
    }
}