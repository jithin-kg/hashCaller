package com.nibble.hashcaller.utils.callHandlers.base.utils

import android.Manifest
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.callHandlers.base.BaseActivity
import com.nibble.hashcaller.utils.callHandlers.base.events.PermissionDenied
import com.nibble.hashcaller.utils.callHandlers.base.events.PermissionGranted
import com.nibble.hashcaller.utils.callHandlers.base.events.PhoneManifestPermissionsEnabled
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference


/**
 * Specifies methods that will contain manifest permission requestor, that will be responsible
 * for displaying manifest permission dialog.
 */
interface ManifestPermissionRequester {
    /**
     * Invokes displaying enable manifest permissions dialogs.
     */
    fun getPermissions()
}

/**
 * Class that invokes request of all app-necessary permissions.
 *
 * @author Zoran Sasko
 * @version 1.0.0
 */
class ManifestPermissionRequesterImpl : ManifestPermissionRequester,
        EasyPermissions.PermissionCallbacks {

    var activity: WeakReference<BaseActivity>? = null

    /**
     * List of permissions to be permitted.
     */
    private val permissions =
            arrayOf(
                    Manifest.permission.READ_PHONE_STATE
            )

    /**
     * Invokes permissions enabling request.
     */
    @AfterPermissionGranted(RC_PERMISSIONS)
    override fun getPermissions() {
        activity?.get()?.let {
            if (EasyPermissions.hasPermissions(it, *permissions)) {
                it.uiEvent.postValue(PhoneManifestPermissionsEnabled)
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                        it, it.getString(R.string.app_name),
                        RC_PERMISSIONS, *permissions
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        activity?.get()?.let {
            it.uiEvent.postValue(PermissionGranted(requestCode, perms))
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        activity?.get()?.let {
            it.uiEvent.postValue(PermissionDenied(requestCode, perms))
        }
    }

    companion object {
        private const val RC_PERMISSIONS = 1212
    }

}