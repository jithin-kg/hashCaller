package com.hashcaller.app.view.ui.auth


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityTestauthBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.utils.auth.CustometokenSigner
import com.hashcaller.app.utils.auth.EnCryptor
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.auth.getinitialInfos.GetInitialUserInfoActivity
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.auth.permissionrequest.PermissionRequestActivity
import com.hashcaller.app.view.ui.contacts.hasMandatoryPermissions
import com.hashcaller.app.view.ui.contacts.showBadRequestToast
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_FAILED
import com.hashcaller.app.view.ui.contacts.utils.SAMPLE_ALIAS
import com.hashcaller.app.view.ui.sms.individual.util.beGone
import com.hashcaller.app.view.ui.sms.individual.util.beInvisible
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.ui.sms.individual.util.toast
import kotlinx.android.synthetic.main.activity_testauth.*
import java.util.concurrent.TimeUnit


class ActivityVerifyOTP : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityTestauthBinding
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private var _userInfoViewModel: UserInfoViewModel? = null
    private val userInfoViewModel get() = _userInfoViewModel!!
    private lateinit var dataStoreViewmodel: DataStoreViewmodel

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
        phoneNumber = intent.getStringExtra("phoneNumber")
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        initListeners()
        // Initialize Firebase Auth
        auth = Firebase.auth
        registerCallback()
        customTokenSigner = CustometokenSigner(auth, this)
        startPhoneNumberVerification("+$phoneNumber")
    }

    private fun initListeners() {
        binding.verifyManually.setOnClickListener(this)
        binding.imgBtnBack.setOnClickListener(this)
        binding.btnResend.setOnClickListener(this)

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
                // [START_EXCLUDE silent]
                code = credential.smsCode

//                if(code == null) code = "123456" //Only for testing purpose
                if (code != null) {
                    verifycode(code!!)
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
//                    binding.fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                    toast("Invalid phone number", Toast.LENGTH_LONG)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
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
                binding.tvDescResend.beVisible()
                binding.btnResend.beVisible()
                binding.pgBarOtpVerify.beGone()
                tvOTPInfo.text = "we just sent you an SMS verification code to +${phoneNumber}"
                tvOTPInfo.beVisible();
                startOtpTimer()
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

    fun startOtpTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResend.setTextColor(
                    ContextCompat.getColor(
                        this@ActivityVerifyOTP,
                        R.color.textColor
                    )
                )
                binding.btnResend.text = "00:${String.format("%02d", millisUntilFinished / 1000)}"
                binding.btnResend.isEnabled = false
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                binding.btnResend.setTextColor(getColor(R.color.colorPrimary))
                binding.btnResend.text = getString(R.string.resend_otp)
                binding.btnResend.isEnabled = true
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

                    val user = task.result?.user
                    // [START_EXCLUDE]
//                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
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

                var isOtpValid = false
                if (!binding.otpview.text.isNullOrEmpty()) {
                    if (binding.otpview.text.toString().length == 6) {
                        isOtpValid = true
                    }
                }
                if (isOtpValid) {
                    binding.verifyManually.isEnabled = false

                    verifycode(otpview.text.toString())
                } else {
                    toast("Please enter the OTP")
                }

            }
            R.id.imgBtnBack -> {
                startPhoneAuthActivity()
            }
            R.id.btnResend -> {
                startPhoneNumberVerification("+$phoneNumber")
            }
        }
    }

    private fun verifycode(code: String) {
        binding.tvDescResend.beInvisible()
        binding.btnResend.beInvisible()
        binding.pgBarOtpVerify.beVisible()

        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code!!)
        signInWithCreadential(credential, code)
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        startPhoneAuthActivity()
    }

    private fun startPhoneAuthActivity() {
        val intent = Intent(this, ActivityPhoneAuth::class.java)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.enter_from_left,
            R.anim.fade_out_animation
        );
        finish()
    }

    private fun signInWithCreadential(
        credential: PhoneAuthCredential,
        code: String
    ) {
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    onSignedInInitialize(code)
                    otpview.setText(code)
//                    Toast.makeText(this, "verified", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
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
    }

    private fun saveTokenInDataStore(task: Task<GetTokenResult>) {
        if (task.isSuccessful) {
            var token = task.result?.token
            // Send token to your backend via HTTPS
            if (!token.isNullOrEmpty()) {
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

        } else {
            toast(task?.exception.toString())
        }
    }

    private fun checkUserInfoInServer() {
        binding.pgBarOtpVerify.beVisible()
        binding.tvVerifying.beVisible()
        userInfoViewModel.getUserInfoFromServer(phoneNumber, this@ActivityVerifyOTP).observe(
            this@ActivityVerifyOTP,
            Observer { res ->
                when (res.code()) {
                    HttpStatusCodes.STATUS_OK -> {
                        lifecycleScope.launchWhenStarted {
                            res.body()?.let { userinfo ->
                                customTokenSigner.signInWithCustomToken(userinfo.data.customToken)
                                if (userinfo != null) {
                                    if (!userinfo.data.firstName.isNullOrEmpty()) {
                                        //user exists in server
                                        userInfoViewModel.saveUserInfoInLocalDb(
                                            userinfo,
                                            dataStoreViewmodel
                                        ).observe(this@ActivityVerifyOTP, Observer { status ->
                                                when (status) {
                                                    OPERATION_COMPLETED -> {


                                                        binding.pgBarOtpVerify.beGone()
                                                        if (hasMandatoryPermissions()) {
                                                            startMainActivity()
                                                        } else {
//                                                                val i = Intent(this@ActivityVerifyOTP, PermissionRequestActivity::class.java)
                                                            val i = Intent(
                                                                this@ActivityVerifyOTP,
                                                                PermissionRequestActivity::class.java
                                                            )
                                                            startActivity(i)
                                                            overridePendingTransition(
                                                                R.anim.in_anim,
                                                                R.anim.out_anim
                                                            )
                                                            finish()
                                                        }
                                                    }
                                                    OPERATION_FAILED-> {
                                                        toast("Something went wrong")
                                                    }
                                                }
                                            })
                                    } else {
                                        //user info not exists in server
                                        startGetUserInfoActivity()
                                    }
                                }
                            }

                        }
                    }
                    else -> {
                        this.showBadRequestToast(res.code())
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
        val i = Intent(this, GetInitialUserInfoActivity::class.java)
        startActivity(i)
        finish()

    }


}