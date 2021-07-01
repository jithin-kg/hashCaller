package com.nibble.hashcaller.view.ui.auth


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityTestauthBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.utils.auth.CustometokenSigner
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.SAMPLE_ALIAS
import com.nibble.hashcaller.view.ui.sms.individual.util.PRIMARY_COLOR
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import kotlinx.android.synthetic.main.activity_testauth.*
import java.util.concurrent.TimeUnit


class ActivityVerifyOTP : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityTestauthBinding
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private  var _userInfoViewModel: UserInfoViewModel? = null
    private val userInfoViewModel get() = _userInfoViewModel!!
    private lateinit var dataStoreViewmodel:DataStoreViewmodel
//    private var _dataStoreViewmodel: DataStoreViewmodel? = null
//    private val dataStoreViewmodel get() = _dataStoreViewmodel!!
    private lateinit var encryptor: EnCryptor

    private var tokenHelper: TokenHelper? = null
    private lateinit var customTokenSigner: CustometokenSigner


    var code: String? = null

    // [END declare_auth]
    var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestauthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resendbtn.setTextColor(Color.BLACK)
        binding.resendbtn.beGone()
        binding.descresend.beGone()
        binding.resendbtn.isEnabled = false

        binding.resendbtn.setOnClickListener {
            startPhoneNumberVerification("+$phoneNumber")
        }

        phoneNumber = intent.getStringExtra("phoneNumber")
        tvOTPInfo.text = "we just sent you an SMS verification code to +${phoneNumber}"


        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        binding.verifyManually.setOnClickListener(this)

        // Initialize Firebase Auth
        auth = Firebase.auth
        registerCallback()
        customTokenSigner = CustometokenSigner(auth, this)
        startPhoneNumberVerification("+$phoneNumber")


    }


    private fun initViewModel() {

        tokenHelper = TokenHelper(FirebaseAuth.getInstance().currentUser)
        _userInfoViewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
//
        dataStoreViewmodel = ViewModelProvider(
            this, DataStoreInjectorUtil.providerViewmodelFactory(
                applicationContext
            )
        ).get(
            DataStoreViewmodel::class.java
        )
    }
    private fun registerCallback() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                code = credential.smsCode
//                if(code == null) code = "123456" //Only for testing purpose
                if (code != null) {
                    verifycode(code!!)
                    Log.d(TAG, "onVerificationCompleted: $code")
                }

                verificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
//                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
//                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    Log.d(TAG, "onVerificationFailed: ")
//                    binding.fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                    toast("Invalid phone number", Toast.LENGTH_LONG)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Log.d(TAG, "onVerificationFailed: quote exceeeded")
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
//                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                binding.descresend.beVisible()
                binding.resendbtn.beVisible()
                binding.pgBarOtpVerify.beGone()
                otpTimer()
                storedVerificationId = verificationId
                resendToken = token
                binding.verifyManually.isEnabled = true

                // [START_EXCLUDE]
                // Update UI
//                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)



            }
        }
    }

    fun otpTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.resendbtn.setTextColor(Color.BLACK)
                binding.resendbtn.setText("00:${String.format("%02d", millisUntilFinished / 1000)}")
                binding.resendbtn.isEnabled = false
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                binding.resendbtn.setTextColor(getColor(R.color.colorPrimary))
                binding.resendbtn.setText("Resend OTP")
                binding.resendbtn.isEnabled = true
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        // [START_EXCLUDE]
        if (verificationInProgress && validatePhoneNumber()) {
//            startPhoneNumberVerification(binding.fieldPhoneNumber.text.toString())
        }

        // [END_EXCLUDE]
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }
    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        binding.pgBarOtpVerify.beVisible()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]

        verificationInProgress = true
    }
    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // [START_EXCLUDE]
                    Log.d(TAG, "signInWithPhoneAuthCredential: signin succes $user")
//                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        Log.d(TAG, "signInWithPhoneAuthCredential: failed invalid code ")
//                        binding.fieldVerificationCode.error = "Invalid code."
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
//                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }
    private fun signOut() {
        auth.signOut()
//        updateUI(STATE_INITIALIZED)
    }
    private fun validatePhoneNumber(): Boolean {
//        val phoneNumber = binding.fieldPhoneNumber.text.toString()
//        if (TextUtils.isEmpty(phoneNumber)) {
//            binding.fieldPhoneNumber.error = "Invalid phone number."
//            return false
//        }

        return true
    }
    companion object {
        const val TAG = "__ActivityVerifyOTP"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }

    override fun onClick(v: View?) {
        when (v?.id) {


            R.id.verifyManually -> {
                binding.verifyManually.isEnabled = false
                binding.pgBarOtpVerify.beVisible()
                verifycode(otpview.text.toString())
            }
        }
    }

    private fun verifycode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code!!)
        signInWithCreadential(credential, code)
    }

    private fun signInWithCreadential(
        credential: PhoneAuthCredential,
        code: String
    ) {
        Log.d(TAG, "signInWithCreadential: $auth")
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "successfull: ")
                    onSignedInInitialize(code)
                    otpview.setText(code)
//                    Toast.makeText(this, "verified", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "something went wrong: ")
                    Toast.makeText(
                        this,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG, "invalid code: ")
                    Toast.makeText(this, "invalid code", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun onSignedInInitialize(code: String?) {
//        setPinInView(code)
        val i = Intent()
        i.putExtra("RC_SIGN_IN", 1)
        val user = Firebase.auth.currentUser
//        val userTokenCallBack = user?.let { UserTokenCallBack(it) }
        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
//               saveTokenInDataStore(task)
                initViewModel()
                checkUserInfoInServer()
            }
//        userTokenCallBack?.onSignedInInititalize { token, exception ->
//            if(!token.isNullOrEmpty()){
//                encryptor = EnCryptor()
//                val encryptedText = encryptor?.encryptText(SAMPLE_ALIAS,token.toString())
//                val encodeTokenString = Base64.encodeToString(
//                    encryptedText,
//                    Base64.DEFAULT
//                )
//                dataStoreViewmodel.saveToken(encodeTokenString).observe(this, Observer {
//                    if (it == OPERATION_COMPLETED) {
////        setResult(1, i)
////        finish()
//                userInfoViewModel.getUserInfoFromServer(encodeTokenString).observe(this, Observer { userinfo ->
//                    if(userinfo!= null){
//                        if(!userinfo.result.firstName.isNullOrEmpty()){
//                            //user exists in server
//                            userInfoViewModel.saveUserInfoInLocalDb(userinfo).observe(this, Observer { status ->
//                               when(status){
//                                   OPERATION_COMPLETED -> {
//                                     startMainActivity()
//                                   }
//                               }
//                            })
//                        }else{
//                            //user info not exists in server
//                            startGetUserInfoActivity()
//                        }
//                    }
//                })
//                    }
//                })
//            }
//
//
//        }
//
//        setResult(1, i)
//        finish()
    }

    private fun saveTokenInDataStore(task: Task<GetTokenResult>) {
        if (task.isSuccessful) {
            var token = task.result?.token
            // Send token to your backend via HTTPS
            if(!token.isNullOrEmpty()){
                encryptor = EnCryptor()
                val encryptedText = encryptor?.encryptText(SAMPLE_ALIAS, token.toString())
                val encodeTokenString = Base64.encodeToString(
                    encryptedText,
                    Base64.DEFAULT
                )
//                dataStoreViewmodel.saveToken(encodeTokenString).observe(this, Observer {
//                    if (it == OPERATION_COMPLETED) {
//                        //        setResult(1, i)
//                        //        finish()
////                        checkUserInfoInServer(encodeTokenString)
//                    }
//                })
            }

        }else{
            Log.d(TAG, "onSignedInInitialize:${task.exception}")
        }
    }

    private fun checkUserInfoInServer() {
        binding.pgBarOtpVerify.beVisible()
        binding.tvVerifying.beVisible()

            userInfoViewModel.getUserInfoFromServer(phoneNumber, this@ActivityVerifyOTP).observe(
                this@ActivityVerifyOTP,
                Observer { userinfo ->
                    lifecycleScope.launchWhenStarted {
                        customTokenSigner.signInWithCustomToken(userinfo.result.customToken)
                        if (userinfo != null) {
                            if (!userinfo.result.firstName.isNullOrEmpty()) {
                                //user exists in server
                                userInfoViewModel.saveUserInfoInLocalDb(
                                    userinfo,
                                    dataStoreViewmodel
                                )
                                    .observe(this@ActivityVerifyOTP, Observer { status ->
                                        when (status) {
                                            OPERATION_COMPLETED -> {


                                                binding.pgBarOtpVerify.beGone()
                                                startMainActivity()
                                            }
                                        }
                                    })
                            } else {
                                //user info not exists in server
                                startGetUserInfoActivity()
                            }
                        }
                    }
                })

    }

    private fun startMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun startGetUserInfoActivity() {
        Log.d(TAG, "startGetUserInfoAcitvity: called")
        val i = Intent(this, GetInitialUserInfoActivity::class.java)
        startActivity(i)
        finish()

    }

    private fun startSplashActivity() {
        val i = Intent()
        setResult(1, i)
        finish()
    }
}