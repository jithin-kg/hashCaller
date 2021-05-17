package com.nibble.hashcaller.view.ui.auth

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.PERMISSION_RESULT_CODE
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_permission_request.*


class PermissionRequestActivity : AppCompatActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private lateinit var sharedPreferences: SharedPreferences
        private var permissionGivenLiveDAta: MutableLiveData<Boolean> = MutableLiveData()

    companion object{
        private const val TAG = "__PermissionRequestActivity"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_request)
        this.permissionGivenLiveDAta.value = false

        initListeners()


    }



    private fun initListeners() {
        btnRequestPermission.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRequestPermission -> {
//                val isPermissionGiven = requesetPermission()
//                if(isPermissionGiven){

                requestPermission()

            }
        }
    }


    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            host = this,
            "read contacts ",
            requestCode = PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS,
            perms = arrayOf(
                READ_CONTACTS,
                CALL_PHONE,
                READ_PHONE_STATE,
                READ_CALL_LOG,
                WRITE_CALL_LOG,
                READ_CONTACTS,
                READ_PHONE_STATE

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
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

}

