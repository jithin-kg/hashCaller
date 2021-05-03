package com.nibble.hashcaller.view.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityPhoneAuthBinding
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.PhoneAuthInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import kotlinx.android.synthetic.main.activity_phone_auth.*


class ActivityPhoneAuth : AppCompatActivity(), View.OnClickListener {
    var displayedInstruction = false
    private lateinit var userInfoViewModel: UserInfoViewModel

    private lateinit var binding:ActivityPhoneAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initListeners()
        initViewModel()
        binding.editTextPhone?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if(binding.editTextPhone.text.isEmpty()){
                    binding.textViewHashValue.text = ""
                    binding.textViewInstruction?.text = ""
                }else{
                    val hashGenerator = HasValueGenerator()
                    val hashValue: String? =
                        hashGenerator.generateHash(binding.editTextPhone?.text.toString())
                    if (!displayedInstruction) {
                       binding.textViewInstruction?.text = "Take a look at how we save your phone number"
                        displayedInstruction = true
                    }
                    binding.textViewHashValue?.text = hashValue
                }

            }

            override fun afterTextChanged(s: Editable) {
                Log.d("Activity_phone_auth", "afterTextChanged: ")
            }
        })
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
        binding.buttonGo.setOnClickListener(this)
    }

    fun passPhoneNumber() {

        val phoneNumber =
            binding.editTextPhone?.text.toString().trim { it <= ' ' }
        userInfoViewModel.saveUserPhoneHash(this, phoneNumber).observe(this, Observer {
            when(it){
                OPERATION_COMPLETED ->{
                    val i = Intent(this@ActivityPhoneAuth, ActivityVerifyOTP::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                    i.putExtra("phoneNumber", phoneNumber)
                    startActivity(i)
                    finish()
                }
            }
        })


    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.buttonGo ->{
                passPhoneNumber()
            }
        }
    }
}