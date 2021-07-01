package com.nibble.hashcaller.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityPhoneAuthBinding
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.PhoneAuthInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_DELETE
import com.nibble.hashcaller.view.ui.extensions.getSpannableString
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.view.utils.ConfirmDialogFragment
import com.nibble.hashcaller.view.utils.ConfirmationClickListener
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_phone_auth.*


class ActivityPhoneAuth : AppCompatActivity(), View.OnClickListener, ConfirmationClickListener {
    var displayedInstruction = false
    private lateinit var userInfoViewModel: UserInfoViewModel

    private lateinit var binding:ActivityPhoneAuthBinding
    private lateinit var libCountryCodeHelper: LibPhoneCodeHelper
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initListeners()
        initViewModel()
        libCountryCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())

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
            getSpannableString("Confirm phone number"),

            getSpannableString("The phone number You entered is +$phoneNumber. Is thi correct ?"),
            TYPE_DELETE
        )
        dialog.show(supportFragmentManager, "sample")
    }

    override fun onYesConfirmationDelete() {
        savePhoneNumHashInDb(phoneNumber)
        binding.btnGo.isEnabled = false
    }

    override fun onYesConfirmationMute() {

        Log.d(TAG, "onYesConfirmationMute: ")
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
//                    this.overridePendingTransition(R.anim.enter_from_right,
//                        R.anim.fade_out_animation);

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

    companion object {
        const val TAG = "__ActivityPhoneAuth"
    }
}