package com.nibble.hashcaller.view.ui.auth

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.PERMISSION_RESULT_CODE
import kotlinx.android.synthetic.main.activity_permission_request.*

class PermissionRequestActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var sharedPreferences: SharedPreferences
    private var permissionGivenLiveDAta: MutableLiveData<Boolean> = MutableLiveData()

    companion object{
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_request)
        this.permissionGivenLiveDAta.value = false

        initListeners()
//        observerPermission()


    }

    private fun observerPermission() {
        permissionGivenLiveDAta.observe(this, Observer {
                it->
            run {
                if (it == true) {
                    setSharedPref(true)
                    val i = Intent()
                    setResult(PERMISSION_RESULT_CODE, i)
                    finish()
                }
            }
        })
    }

    private fun initListeners() {
        btnRequestPermission.setOnClickListener(this)
        btnSetAsDefaultSMS.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnRequestPermission ->{
                val isPermissionGiven = requesetPermission()
                if(isPermissionGiven){


                    if(checkDefaultSettings()){
                        setSharedPref(true)
                        val i = Intent()
                        setResult(PERMISSION_RESULT_CODE, i)
                        finish()
                    }

                }
                else{

                    setSharedPref(false)
                }
            }
            R.id.btnSetAsDefaultSMS->{
                checkDefaultSettings()
            }
        }
    }

    private fun checkDefaultSettings(): Boolean {
        var isDefault = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //is to set the default sms app
            isDefault = if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setMessage("This app is not set as your default messaging app. Do you want to set it as default?")
                    .setCancelable(false)
                    .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
//                        checkPermissions()
                    }
                    .setPositiveButton("Yes") { _: DialogInterface?, id: Int ->
                        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                        startActivity(intent)
//                        checkPermissions()
                    }
                builder.show()
                false
            } else true
        }
        return isDefault
    }

    private fun setSharedPref(b: Boolean) {
        sharedPreferences = applicationContext.getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putBoolean("isPermissionGiven", b)
        editor.commit()
    }

    private fun requesetPermission(): Boolean {
        var permissionGiven = false
        //persmission
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.RECEIVE_MMS
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
//
                    report.let {
                        if(report?.areAllPermissionsGranted()!!){
                            permissionGiven = true
                            permissionGivenLiveDAta.value = true
//                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()

                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
//                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
                }
            }).check()
        return permissionGiven
    }

}

