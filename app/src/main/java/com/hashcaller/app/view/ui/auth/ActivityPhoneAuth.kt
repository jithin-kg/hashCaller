package com.hashcaller.app.view.ui.auth

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityPhoneAuthBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.auth.getinitialInfos.PhoneAuthInjectorUtil
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.TYPE_DELETE
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.sms.individual.util.toast
import com.hashcaller.app.view.utils.ConfirmDialogFragment
import com.hashcaller.app.view.utils.ConfirmationClickListener
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_phone_auth.*


class ActivityPhoneAuth : AppCompatActivity(), View.OnClickListener, ConfirmationClickListener {
    var displayedInstruction = false
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var binding:ActivityPhoneAuthBinding
    private lateinit var libCountryCodeHelper: LibPhoneCodeHelper
    private var phoneNumber: String = ""
    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var pickerResultCb: ActivityResultLauncher<IntentSenderRequest>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerPickerCb()
        dataStoreRepository = DataStoreRepository(this.tokeDataStore)
        setDefaultDataStoreValues()
        initListeners()
        initViewModel()

        libCountryCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        lifecycleScope.launchWhenCreated {
            requestHint()
        }

    }
    fun registerPickerCb() {
        pickerResultCb = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if(it.resultCode == RESULT_OK){
                // get data from the dialog which is of type Credential
                val credential: Credential? = it.data?.getParcelableExtra(Credential.EXTRA_KEY)

                // set the received data t the text view
                credential?.apply {
                    val num = credential.id
                    val countryCodeIso = CountrycodeHelper(this@ActivityPhoneAuth).getCountryISO()
                    binding.edtTextPhone.setText(libCountryCodeHelper.getNumWithoutCountyCode(num, countryCodeIso))

                }
            }
        }
    }
    private fun setDefaultDataStoreValues() {
        lifecycleScope.launchWhenCreated {
            dataStoreRepository.setBoolean(true, PreferencesKeys.KEY_BLOCK_COMMONG_SPAMMERS)
        }
    }

    private fun initViewModel() {
        userInfoViewModel = ViewModelProvider(
            this, PhoneAuthInjectorUtil.provideUserInjectorUtil(
                applicationContext
            )
        ).get(
            UserInfoViewModel::class.java
        )
    }

    private fun initListeners() {
        binding.btnGo.setOnClickListener(this)
    }

    fun passPhoneNumber() {
        phoneNumber = formatPhoneNumber(
            binding.coutryCodePicker.selectedCountryCode +
                    binding.edtTextPhone?.text.toString()

        )

        val isValidForRegion = libCountryCodeHelper.isValidForRegion(phoneNumber, binding.coutryCodePicker.selectedCountryNameCode)

        if (isValidForRegion){
            showAlert(phoneNumber)

        }else {
            binding.btnGo.isEnabled = true
            toast("Please Enter a valid phone number.")
        }
    }

    private fun showAlert(phoneNumber: String) {
        val dialog = ConfirmDialogFragment(this,
            getSpannableString("+$phoneNumber \n \n Is your phone number above correct?"),
            getSpannableString("A verification code will be sent to :"),
            TYPE_DELETE
        )
        dialog.show(supportFragmentManager, "sample")
    }

    override fun onYesConfirmationDelete() {
        savePhoneNumHashInDb(phoneNumber)
        binding.btnGo.isEnabled = false
    }

    override fun onYesConfirmationMute() {

    }


    private fun savePhoneNumHashInDb(phoneNumber: String) {
        userInfoViewModel.saveUserPhoneHash(this, phoneNumber).observe(this, Observer {
            when(it){
                OPERATION_COMPLETED ->{
                    val i = Intent(this@ActivityPhoneAuth, ActivityVerifyOTP::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                    i.putExtra("phoneNumber", phoneNumber)
                    startActivity(i)
                    overridePendingTransition(R.anim.in_anim,
                        R.anim.out_anim
                    );
                    finish()
                }
            }
        })
    }


    override fun onBackPressed() {
        finishAfterTransition()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnGo ->{
                passPhoneNumber()
            }
        }
    }


//    override fun finish() {
//        super.finish()
//        overridePendingTransition(R.anim.in_anim,
//            R.anim.out_anim
//        );
//    }
private fun requestHint() {
    // To retrieve the Phone Number hints, first, configure
    // the hint selector dialog by creating a HintRequest object.
    val hintRequest = HintRequest.Builder()
        .setPhoneNumberIdentifierSupported(true)
        .build()

    val options = CredentialsOptions.Builder()
        .forceEnableSaveDialog()
        .build()
    // Then, pass the HintRequest object to
    // credentialsClient.getHintPickerIntent()
    // to get an intent to prompt the user to
    // choose a phone number.
    val credentialsClient = Credentials.getClient(this, options)
    val intent = credentialsClient.getHintPickerIntent(hintRequest)
    try {
//        startIntentSenderForResult(
//            intent.intentSender,
//            CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle()
//        )
        val intentSenderRequest = IntentSenderRequest.Builder(intent.intentSender).build()
        pickerResultCb.launch(intentSenderRequest)

    } catch (e: IntentSender.SendIntentException) {
        e.printStackTrace()
    }

}

    companion object {
        const val TAG = "__ActivityPhoneAuth"
        var CREDENTIAL_PICKER_REQUEST = 1

    }
}