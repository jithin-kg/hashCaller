package com.nibble.hashcaller.view.ui.auth

import android.Manifest
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityPermissionRequestBinding
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.READ_CNCT_DISPLAY_OVER
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_CONTACTS
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_PHONE_STATE
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.hasReadContactsPermission
import com.nibble.hashcaller.view.ui.contacts.hasReadPhoneStatePermission
import com.nibble.hashcaller.view.ui.extensions.requestAlertWindowPermission
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest


class PermissionRequestActivity : AppCompatActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityPermissionRequestBinding
    private var rational = ""
    private val permsRequestCode = 200
    private lateinit var animatedCmpt: AnimatedVectorDrawableCompat
    private lateinit var animatedVector : AnimatedVectorDrawable
    private var isContactPermissionGivenFromView = false
    private var isStatePermissionGivenFromView = false

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

        if(Settings.canDrawOverlays(this)){
            //overlay not given
            binding.layoutDispalyOver.beGone()
        }
    }




    private fun initListeners() {
        binding.btnRequestPermission.setOnClickListener(this)
        binding.btnContactAcces.setOnClickListener(this)
        binding.btnPhoneState.setOnClickListener(this)
        binding.btnOverlay.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRequestPermission -> {
//                val isPermissionGiven = requesetPermission()
//                if(isPermissionGiven){

//                requestPermission()
//                showAlert()
                requestAllPermission()
            }
            R.id.btnOverlay -> {
                requestAlertWindowPermission()
            }
            R.id.btnPhoneState -> {
                requestPermission(
                    READ_PHONE_STATE,
                    REQUEST_CODE_READ_PHONE_STATE,
                    getString(R.string.rational_phone_state)
                )
            }
            R.id.btnContactAcces -> {
                requestPermission(
                    READ_CONTACTS,
                    REQUEST_CODE_READ_CONTACTS,
                    getString(R.string.rational_cntcts)
                    )
            }
//            R.id.tvTermsAgree ->{
//                startPrivacyIntent()
//            }
            R.id.btnContinueAlert -> {
//                requestPermission()
            }
        }
    }

    private fun requestPermission(
        permission:String,
        requestCode:Int,
        rational:String
        ) {
        val request = PermissionRequest.Builder(this)
            .code(requestCode)
            .perms(arrayOf(
                permission
            ))
            .rationale(rational)
            .positiveButtonText("Continue")
            .negativeButtonText("Cancel")
            .build()
        EasyPermissions.requestPermissions(this, request)
    }


    private fun startPrivacyIntent() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hashcaller.com/privacy"))
        startActivity(browserIntent)
    }





    private fun setImageOnPermissionChange(backgroundLayout:ConstraintLayout,
                                           imgView:ImageView,
                                           backgroundResourceId:Int = R.drawable.contact_circular_background_green,
                                           imageId:Int = R.drawable.avd_done) {
        backgroundLayout.background = ContextCompat.getDrawable(this, backgroundResourceId)
        imgView.setImageDrawable(ContextCompat.getDrawable(this, imageId))

        if(imageId == R.drawable.avd_done){
        //show done animation
            val drawable:Drawable = imgView.drawable

            if(drawable is AnimatedVectorDrawableCompat){
                animatedCmpt = (drawable as AnimatedVectorDrawableCompat)
                animatedCmpt.start()
            }else if(drawable is AnimatedVectorDrawable){
                animatedVector = (drawable as AnimatedVectorDrawable)
                animatedVector.start()
            }
        }

    }


    private fun requestAllPermission() {
        val perms = arrayOf(
            READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.CALL_PHONE,
        )

        val permissionRequest =  PermissionRequest.Builder(this)
            .code(READ_CNCT_DISPLAY_OVER)
            .perms(perms)
            .rationale(getString(R.string.contact_and_phone_state_rational))
            .build()

        EasyPermissions.requestPermissions(
            this,
            permissionRequest)

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
        when(requestCode){
            REQUEST_CODE_READ_CONTACTS -> {
                isContactPermissionGivenFromView = true
                setImageOnPermissionChange(
                    binding.imgvCircleContactBackground,
                    binding.imgVContact
                )
                binding.btnContactAcces.beGone()

            }
            REQUEST_CODE_READ_PHONE_STATE -> {
                isStatePermissionGivenFromView = true
                setImageOnPermissionChange(
                    binding.layoutPhoneBackground,
                    binding.imgVPhoneState
                )
                binding.btnPhoneState.beGone()
            }
            READ_CNCT_DISPLAY_OVER -> {
            startMainActivityAndfinish()
        }
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if(!EasyPermissions.hasPermissions(this, READ_CONTACTS)){
            //read contacts permission not given
            binding.btnContactAcces.beVisible()
            setImageOnPermissionChange(
                binding.imgvCircleContactBackground,
                binding.imgVContact,
                R.drawable.contact_circular_background_danger,
                R.drawable.ic_contacts_book_2_line_white
            )
        }else {
            if(!isContactPermissionGivenFromView){
                binding.btnContactAcces.beGone()
                setImageOnPermissionChange(
                    binding.imgvCircleContactBackground,
                    binding.imgVContact
                )
            }

        }
        //phone state
        if(!EasyPermissions.hasPermissions(this, READ_PHONE_STATE)){
            //read contacts permission not given
            binding.btnPhoneState.beVisible()
            setImageOnPermissionChange(
                binding.layoutPhoneBackground,
                binding.imgVPhoneState,
                R.drawable.contact_circular_background_danger,
                R.drawable.ic_phone_line_white
            )
        }else {
            if(!isStatePermissionGivenFromView){
                binding.btnPhoneState.beGone()
                setImageOnPermissionChange(
                    binding.layoutPhoneBackground,
                    binding.imgVPhoneState,
                )
            }

        }

        if(Settings.canDrawOverlays(this)){
            //overlay not given
            binding.layoutDispalyOver.beGone()
        }else {
            binding.layoutDispalyOver.beVisible()
        }
    }

    private fun startMainActivityAndfinish() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

}

