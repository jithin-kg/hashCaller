package com.nibble.hashcaller.view.ui.auth

import android.Manifest
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
        private const val TAG = "__PermissionRequestActivity"
        private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_request)
        this.permissionGivenLiveDAta.value = false

        initListeners()
        observerPermission()


    }

    private fun observerPermission() {
        permissionGivenLiveDAta.observe(this, Observer {
                it->
//            run {
                if (it == true) {
                    setSharedPref(true)
                    val i = Intent()
                    setResult(PERMISSION_RESULT_CODE, i)
                    finish()
                }
//            }
        })
    }

    private fun initListeners() {
        btnRequestPermission.setOnClickListener(this)
        btnSetAsDefaultSMS.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRequestPermission -> {
//                val isPermissionGiven = requesetPermission()
//                if(isPermissionGiven){


                checkDefaultSettings()

            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun checkDefaultSettings(): Boolean {
        var requestCode=  222
        var resultCode = 232
        var isDefault = false
      try{

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              val roleManager: RoleManager = this.getSystemService(RoleManager::class.java)
              // check if the app is having permission to be as default SMS app
              val isRoleAvailable =
                  roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
              if (isRoleAvailable) {
                  // check whether your app is already holding the default SMS app role.
                  val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                  if (!isRoleHeld) {
                      val roleRequestIntent =
                          roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                      startActivityForResult(roleRequestIntent, requestCode)
                  }else{
                      requesetPermission()
                  }
              }
          } else {
              val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
              intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
              startActivityForResult(intent, requestCode)
          }

      }catch (e:Exception){
          Log.d(TAG, "checkDefaultSettings: exception $e")
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
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS

            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
//
                    report.let {
                        if(report?.areAllPermissionsGranted()!!){
                            permissionGiven = true
                            setSharedPref(true)
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
    @SuppressLint("LongLogTag")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        /**
         * Set as default SMS app onActivityResult if user chosen as deafult SMS app
         * is -1
         * else the result is 0
         */
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== -1 && requestCode == 222){
            permissionGivenLiveDAta.value  = true

            if(requesetPermission()){
//                setSharedPref(true)
            }else{
                setSharedPref(false)
            }
        }
        Log.d(TAG, "onActivityResult: requestCode :$requestCode")
        Log.d(TAG, "onActivityResult: resultCode :$resultCode")

    }

}

