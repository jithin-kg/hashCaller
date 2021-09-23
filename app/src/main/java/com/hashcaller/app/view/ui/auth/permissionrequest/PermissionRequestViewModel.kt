package com.hashcaller.app.view.ui.auth.permissionrequest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class PermissionRequestViewModel(app: Application) : AndroidViewModel(app) {

    val currentDestination = MutableLiveData(Destinations.None)

    fun navigateToContactConsentScreen() {
        currentDestination.value = Destinations.ContactConsent
    }

    fun navigateToPendingPermissionScreen() {
        currentDestination.value = Destinations.OtherPendingPermissions
    }
    fun navigateToOverlayPermissionScreen() {
        currentDestination.value = Destinations.Overlay
    }

    fun navigateToEnd() {
        currentDestination.value = Destinations.End
    }

    fun resetDestination() {
        currentDestination.value = Destinations.None
    }

    lateinit var onContactPermissionGranted: () -> Unit
    lateinit var onContactPermissionDenied: () -> Unit

    lateinit var onReadPhoneStatePermissionGranted: () -> Unit
    lateinit var onReadPhoneStatePermissionDenied: () -> Unit

    lateinit var onOverlayPermissionGranted:()->Unit
    lateinit var onOverlayPermissionDenied:()->Unit

    lateinit var onScreeningPermissionGranted:()->Unit
    lateinit var onScreeningPermissionDenied:()->Unit
}

enum class Destinations {
    ContactConsent,
    OtherPendingPermissions,
    Overlay,
    None,
    End
}