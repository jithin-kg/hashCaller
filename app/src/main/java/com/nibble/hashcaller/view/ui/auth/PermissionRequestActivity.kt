package com.nibble.hashcaller.view.ui.auth

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityPermissionRequestBinding
import com.nibble.hashcaller.databinding.ContactPermissionAlertBinding
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.hasReadContactsPermission
import com.nibble.hashcaller.view.ui.contacts.hasReadPhoneStatePermission
import com.nibble.hashcaller.view.ui.extensions.getCurrentDisplayMetrics
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_permission_request.*


class PermissionRequestActivity : AppCompatActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityPermissionRequestBinding
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var alertBinding:ContactPermissionAlertBinding
    private var rational = ""

    companion object{
        private const val TAG = "__PermissionRequestActivity"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAlertView()
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

    private fun initAlertView() {
        alertBinding = ContactPermissionAlertBinding.inflate(layoutInflater, null, false)

        alertBuilder = AlertDialog.Builder(this)
//        alertBuilder.setTitle("Search filter")
            //        builder.setMessage(" MY_TEXT ")
            .setView(alertBinding.root)
            .setCancelable(true)
//                    .setPositiveButton("", DialogInterface.OnClickListener { dialog, id ->
//                         Log.d(TAG, "showSearchFilterAlert: onyes clicked")
//                     })
//
//            .setNegativeButton("Cancel",
//                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
//        alertBinding.root.maxWidth = 32


    }


    private fun initListeners() {
        binding.btnRequestPermission.setOnClickListener(this)
        alertBinding.btnContinueAlert.setOnClickListener(this)
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

    private fun showAlert() {
        if(alertBinding.root.parent!=null) {
            (alertBinding.root.parent as ViewGroup).removeView(alertBinding.root)
        }

        alertBuilder.setView(alertBinding.root)


//        alertBuilder.show()
        val alert: AlertDialog = alertBuilder.create()
        alert.show()
        val dm = getCurrentDisplayMetrics()
        alert.window?.setLayout( dm.widthPixels - 160, ViewGroup.LayoutParams.WRAP_CONTENT)
    }



    private fun startPrivacyIntent() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hashcaller.com/privacy"))
        startActivity(browserIntent)
    }


    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            host = this,
            rational,
            requestCode = PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS,
            perms = arrayOf(
                READ_CONTACTS,
//                CALL_PHONE,
                READ_PHONE_STATE,
//                READ_CALL_LOG,
//                WRITE_CALL_LOG,
//                READ_CONTACTS,
//                READ_PHONE_STATE

            )
        )
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
     if(hasReadContactsPermission() && hasReadPhoneStatePermission()){
         startMainActivityAndfinish()
     }
    }

    private fun startMainActivityAndfinish() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

}

