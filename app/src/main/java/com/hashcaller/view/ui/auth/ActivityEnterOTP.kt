package com.hashcaller.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hashcaller.R
import kotlinx.android.synthetic.main.activity_enter_o_t_p.*
import java.util.concurrent.TimeUnit


class ActivityEnterOTP : AppCompatActivity(), View.OnClickListener {

    private val TAG: String? = "ActivityEnterOTP"
    var phoneNumber: String? = null
    var mAuth: FirebaseAuth? = null
    private var otpSent = false
    var code: String? = null
    private lateinit var viemodel:EnterOtpViewModel
    //    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_o_t_p)

        viemodel = EnterOtpViewModel()

        phoneNumber = intent.getStringExtra("phoneNumber")
        phoneNumber = "+16605551234"
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
        btnCaptcha.setOnClickListener(this)
        button2.setOnClickListener(this)
        observeCapchaRequest()

    }

    private fun observeCapchaRequest() {
        this.viemodel.captchRequest.observe(this, Observer {
            res->
            if(res == true){
                Log.d(TAG, "observeCapchaRequest: verification succesfull")
            }
        })
    }

    private fun sendVerificationCodeToUser() {
        if (!otpSent) {
            Log.d(ActivityEnterOTP.TAG, "sendVerificationCodeToUser:")
//            PhoneAuthProvider.getInstance()
//                .verifyPhoneNumber(
//                    "+91$phoneNumber",
//                    60, TimeUnit.SECONDS,
//                    TaskExecutors.MAIN_THREAD, mCallbacks
//                )
            val options = PhoneAuthOptions.newBuilder(this.mAuth!!)
                .setPhoneNumber(this.phoneNumber!!)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
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
                if(code == null) code = "123456" //Only for testing purpose
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
                    Log.d(TAG, "invalid code: ")
                    Toast.makeText(this@ActivityEnterOTP, "invalid code", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
    private fun onSignedInInitialize(code: String?) {
//        setPinInView(code)z
        val i = Intent()
        i.putExtra("RC_SIGN_IN", 1)
        setResult(1, i)
        finish()
    }



    @Synchronized
    protected fun buildGoogleApiClient() {
        val options =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build()

        val mSignInClient = GoogleSignIn.getClient(this, options)
    }

    fun manualVerification(view: View) {

        verifyCode("123456")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCaptcha -> {
                Log.d(TAG, "onClick: ")
                SafetyNet.getClient(this).verifyWithRecaptcha(SAFETY_NET_API_KEY)
                    .addOnSuccessListener(this) { response ->
                        Log.d(TAG, "onSuccess")

                        if (!response.tokenResult.isEmpty()) {

                            // Received reCaptcha token and This token still needs to be validated on
                            // the server using the SECRET key api.
//                        verifyTokenFromServer(response.tokenResult, "feedback").execute()
                            Log.d(TAG, "onSuccess: " + response.tokenResult)
                            sendTokenToServer(response.tokenResult)
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        if (e is ApiException) {
                            Log.d(
                                TAG,
                                "SafetyNet Error: " + CommonStatusCodes.getStatusCodeString(e.statusCode)
                            )
                        } else {
                            Log.d(TAG, "Unknown SafetyNet error: " + e.message)
                        }
                    }
            }
            R.id.button2->{
                sendVerificationCodeToUser()

            }
        }
    }

    private fun sendTokenToServer(tokenResult: String?) {
            viemodel.sendToken(tokenResult!!)
    }

    protected fun handleSiteVerify(responseToken: String) {
//        //it is google recaptcha siteverify server
//        //you can place your server url
//        val url = "https://www.google.com/recaptcha/api/siteverify"
//        val request: StringRequest = object : StringRequest(Request.Method.POST, url,
//            object : Listener<String?>() {
//                fun onResponse(response: String?) {
//                    try {
//                        val jsonObject = JSONObject(response)
//                        if (jsonObject.getBoolean("success")) {
//                            //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(
//                                applicationContext,
//                                jsonObject.getString("error-codes").toString(),
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    } catch (ex: Exception) {
//                        Log.d(TAG, "JSON exception: " + ex.message)
//                    }
//                }
//            },
//            object : ErrorListener() {
//                fun onErrorResponse(error: VolleyError) {
//                    Log.d(TAG, "Error message: " + error.getMessage())
//                }
//            }) {
//            protected val params: Map<String, String>?
//                protected get() {
//                    val params: MutableMap<String, String> = HashMap()
//                    params["secret"] = SECRET_KEY
//                    params["response"] = responseToken
//                    return params
//                }
//        }
//        request.setRetryPolicy(
//            DefaultRetryPolicy(
//                50000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//            )
//        )
//        queue.add(request)
    }
    companion object{
        const val SAFETY_NET_API_KEY = "6Ld1mSAaAAAAANRKEJu543SE7yl-1JkOKPEgYTf7"
        private const val TAG= "__ActivityEnterOTP"

    }
}