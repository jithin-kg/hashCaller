package com.nibble.hashcaller.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.nibble.hashcaller.R
import java.util.concurrent.TimeUnit

class ActivityEnterOTP : AppCompatActivity() {

private val TAG: String? = "ActivityEnterOTP"

    var phoneNumber: String? = null
    var mAuth: FirebaseAuth? = null
    private var otpSent = false
    var code: String? = null

    //    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_o_t_p)
        phoneNumber = intent.getStringExtra("phoneNumber")
        mAuth = FirebaseAuth.getInstance()
        Log.d(ActivityEnterOTP.TAG, "onCreate: $mAuth")
        sendVerificationCodeToUser()


    }
    private fun sendVerificationCodeToUser() {
        if (!otpSent) {
            Log.d(ActivityEnterOTP.TAG, "sendVerificationCodeToUser:")
            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    "+91$phoneNumber",
                    60, TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD, mCallbacks
                )
            otpSent = true
        }
    }

    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s
                Log.d(ActivityEnterOTP.TAG, "onCodeSent: $mVerificationId")
                val mResendTokne = forceResendingToken
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //getting code sent by sms
                code = phoneAuthCredential.smsCode
                if (code != null) {
                    verifyCode(code)
                    Log.d(ActivityEnterOTP.TAG, "onVerificationCompleted: $code")
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@ActivityEnterOTP, e.message, Toast.LENGTH_SHORT).show()
                Log.d(ActivityEnterOTP.TAG, "onVerificationFailed: " + e.message)
            }
        }
    private fun verifyCode(code: String?) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code!!)
        signInWithCreadential(credential)
    }
    private fun signInWithCreadential(credential: PhoneAuthCredential) {
        Log.d(ActivityEnterOTP.TAG, "signInWithCreadential: $mAuth")
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this@ActivityEnterOTP
            ) { task ->
                if (task.isSuccessful) {
                    Log.d(ActivityEnterOTP.TAG, "successfull: ")
                    onSignedInInitialize(code)
                    Toast.makeText(this@ActivityEnterOTP, "verified", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(ActivityEnterOTP.TAG, "something went wrong: ")
                    Toast.makeText(
                        this@ActivityEnterOTP,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d(ActivityEnterOTP.TAG, "invalid code: ")
                    Toast.makeText(this@ActivityEnterOTP, "invalid code", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
    private fun onSignedInInitialize(code: String?) {
//        setPinInView(code)
        val i = Intent()
        i.putExtra("RC_SIGN_IN", 1)
        setResult(1, i)
        finish()
    }

   companion object{
       private const val TAG= "__ActivityEnterOTP"
   }
}