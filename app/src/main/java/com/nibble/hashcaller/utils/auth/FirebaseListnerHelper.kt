package com.nibble.hashcaller.utils.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseListnerHelper(private val listener:IFirebaseAuthStateListener) {
     lateinit var rcAuthStateListener: FirebaseAuth.AuthStateListener
     fun firebaseAuthListener() {
        rcAuthStateListener =
            FirebaseAuth.AuthStateListener { firebaseAuth ->
                //                    Task<GetTokenResult> idToken = FirebaseUser.getIdToken();
                if (firebaseAuth.currentUser != null) {
                    //user is signed in
                    listener.onSignedInInitialize(firebaseAuth.currentUser!!)
                } else {
                    // user is signed out
                    listener.onSingnedOutcleanUp()
//                    val i = Intent(this@SplashActivity, ActivityPhoneAuth::class.java)
//                    startActivityForResult(i, RC_SIGN_IN)
                }
            }
    }
    interface IFirebaseAuthStateListener{
        fun onSignedInInitialize(currentUser: FirebaseUser)
        fun onSingnedOutcleanUp()
    }
}