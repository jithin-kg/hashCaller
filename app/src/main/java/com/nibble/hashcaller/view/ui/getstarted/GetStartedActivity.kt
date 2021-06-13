package com.nibble.hashcaller.view.ui.getstarted

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityGetStartedBinding
import com.nibble.hashcaller.databinding.ContactPermissionAlertBinding
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.extensions.getCurrentDisplayMetrics
import com.vmadalin.easypermissions.EasyPermissions

class GetStartedActivity : AppCompatActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks  {
    private lateinit var binding: ActivityGetStartedBinding
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var alertBinding: ContactPermissionAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAlertView()
        initListeners()
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
        binding.tvTermsAgree.setOnClickListener(this)
        alertBinding.btnContinueAlert.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRequestPermission -> {
//                val isPermissionGiven = requesetPermission()
//                if(isPermissionGiven){

//                requestPermission()
//                showAlert()
                if(checkPermission()){
                    showAlert()
                }else {
                    startPhoneAuthActivity()
                }


            }
            R.id.tvTermsAgree ->{
                startPrivacyIntent()
            }
            R.id.btnContinueAlert -> {
                requestPermission()
            }
        }
    }
    private fun checkPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
//            READ_CALL_LOG,
//            WRITE_CALL_LOG,
//            READ_CONTACTS,
//            READ_PHONE_STATE

        )
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
            "read contacts ",
            requestCode = PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS,
            perms = arrayOf(
                Manifest.permission.READ_CONTACTS,
//                CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
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
        startPhoneAuthActivity()
    }

    private fun startPhoneAuthActivity() {
        val i = Intent(this, ActivityPhoneAuth::class.java)
        startActivity(i)
        finish()
    }

    companion object {
        private const val TAG = "__GetStartedActivity"
    }
}