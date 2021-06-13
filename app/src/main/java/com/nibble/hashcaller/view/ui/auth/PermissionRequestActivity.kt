package com.nibble.hashcaller.view.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityPermissionRequestBinding
import com.nibble.hashcaller.databinding.ContactPermissionAlertBinding
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.hasReadContactsPermission
import com.nibble.hashcaller.view.ui.contacts.hasReadPhoneStatePermission
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest


class PermissionRequestActivity : AppCompatActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityPermissionRequestBinding
    private var rational = ""
    private val permsRequestCode = 200

    companion object{
        private const val TAG = "__PermissionRequestActivity"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        hideViewsThatDoesntNeedPermission()
    }

    private fun hideViewsThatDoesntNeedPermission() {
        var contactsEnabled = false
        if(hasReadContactsPermission()){
            binding.layoutContact.beGone()
            contactsEnabled = true
        }else {
            rational = getString(R.string.rational_contacts)
            binding.layoutContact.beVisible()
        }

        if(hasReadPhoneStatePermission()){
            binding.layoutPhoneAcccess.beGone()
        }else {
            if(!contactsEnabled){
                rational = getString(R.string.rational_phone_state)
            }else{
                rational = getString(R.string.rational_contact_and_phone_state)
            }
            binding.layoutPhoneAcccess.beVisible()
        }
    }




    private fun initListeners() {
        binding.btnRequestPermission.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRequestPermission -> {
//                val isPermissionGiven = requesetPermission()
//                if(isPermissionGiven){

//                requestPermission()
//                showAlert()
                requestPermission()
            }
//            R.id.tvTermsAgree ->{
//                startPrivacyIntent()
//            }
            R.id.btnContinueAlert -> {
//                requestPermission()
            }
        }
    }




    private fun startPrivacyIntent() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hashcaller.com/privacy"))
        startActivity(browserIntent)
    }


    private fun requestPermission() {
        val perms = arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE
        )
        val permissionRequest =  PermissionRequest.Builder(this)
            .code(PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS)
            .perms(perms)
            .rationale(getString(R.string.contact_and_phone_state_rational))
            .build()
        EasyPermissions.requestPermissions(this, permissionRequest )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    @SuppressLint("LongLogTag")
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied: ")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startMainActivityAndfinish()
    }



    private fun startMainActivityAndfinish() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

}

