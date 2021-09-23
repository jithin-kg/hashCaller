package com.hashcaller.app.view.ui.auth.permissionrequest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ContactConsentFragmentBinding
import com.hashcaller.app.databinding.OverlayPermissionFragmentBinding
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.view.ui.contacts.hasReadContactsPermission
import com.hashcaller.app.view.ui.contacts.hasReadPhoneStatePermission
import com.hashcaller.app.view.ui.extensions.requestAlertWindowPermission
import com.hashcaller.app.view.utils.requestPermission
import kotlinx.coroutines.delay

class OverlayPermissionFragment : Fragment() {

    companion object {
        fun newInstance() = OverlayPermissionFragment()
        private const val TAG = "`ContactConsentFragment"
    }

    private val viewModel: PermissionRequestViewModel by activityViewModels()
    private lateinit var binding: OverlayPermissionFragmentBinding
    private var flagContinuePressed = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OverlayPermissionFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() = with(binding) {

        continueButton.setOnClickListener {
            flagContinuePressed = true
            (activity as AppCompatActivity).requestAlertWindowPermission()
        }
        btnSkip.setOnClickListener {
            viewModel.navigateToEnd()
        }
    }


    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(requireContext()))
            checkAndNavigateToPendingPermissionsScreen()
    }

    private fun checkAndNavigateToPendingPermissionsScreen() {
            lifecycleScope.launchWhenCreated {
                delay(400)
                viewModel.navigateToEnd()
            }
    }

}
