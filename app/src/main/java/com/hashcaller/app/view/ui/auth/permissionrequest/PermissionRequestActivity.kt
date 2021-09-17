package com.hashcaller.app.view.ui.auth.permissionrequest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityPermissionRequestSwipeBinding
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.contacts.hasReadContactsPermission
import com.hashcaller.app.view.ui.contacts.hasReadPhoneStatePermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PermissionRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionRequestSwipeBinding
    private val viewModel: PermissionRequestViewModel by viewModels()

    private val permissionCallback = object : EasyPermissions.PermissionCallbacks {
        override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
            if (EasyPermissions.somePermissionPermanentlyDenied(
                    this@PermissionRequestActivity,
                    perms
                )
            ) {
                SettingsDialog.Builder(this@PermissionRequestActivity).build().show()
            }
        }

        override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
            when (requestCode) {
                PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS -> {
                    checkAndNavigateToPendingPermissionsScreen()
                }
                PermisssionRequestCodes.REQUEST_CODE_READ_PHONE_STATE -> {
                    viewModel.onReadPhoneStatePermissionGranted()
                }
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            when (requestCode) {
                PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS -> {
                    if (grantResults[0] != PackageManager.PERMISSION_DENIED) {
                    }

                }
            }

        }
    }

    fun checkAndNavigateToPendingPermissionsScreen() {
        val pendingPermissions =
           !hasReadPhoneStatePermission() &&
                   ! Settings.canDrawOverlays(this)

        if (pendingPermissions)
            viewModel.navigateToPendingPermissionScreen()
//        else
//            viewModel.navigateToEnd()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            permissionCallback
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission_request_swipe)
        binding.lifecycleOwner = this

        observe()
        setupUi()
    }
//androidx.constraintlayout.motion.widget.MotionLayout
    private fun observe() = with(viewModel) {

        currentDestination.observe(this@PermissionRequestActivity) {
            when (it) {
                Destinations.ContactConsent -> {
                    supportFragmentManager.commit {
                        replace(R.id.container, ContactConsentFragment())
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }

                Destinations.OtherPendingPermissions -> {
                    supportFragmentManager.commit {
                        replace(R.id.container, OtherPendingPermissionsFragment())
                        setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }

                Destinations.End -> {
                    startMainActivityAndfinish()
                }

                else -> {
                }
            }
        }


    }


    private fun startMainActivityAndfinish() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun setupUi() {

        val contactPermission = hasReadContactsPermission()
        val otherPermissions =
            hasReadPhoneStatePermission() &&
                    Settings.canDrawOverlays(this)

        if (!contactPermission) {
            viewModel.navigateToContactConsentScreen()
        } else if (!otherPermissions) {
            viewModel.navigateToPendingPermissionScreen()
        } else {
            viewModel.navigateToEnd()
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

}
