package com.hashcaller.app.view.ui.auth.permissionrequest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ContactConsentFragmentBinding
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.view.ui.contacts.hasReadContactsPermission
import com.hashcaller.app.view.ui.contacts.hasReadPhoneStatePermission
import com.hashcaller.app.view.utils.requestPermission

class ContactConsentFragment : Fragment() {

    companion object {
        fun newInstance() = ContactConsentFragment()
        private const val TAG = "`ContactConsentFragment"
    }

    private val viewModel: PermissionRequestViewModel by activityViewModels()
    private lateinit var binding: ContactConsentFragmentBinding
    private var flagContinuePressed = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContactConsentFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() = with(binding) {
        materialCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                motionLayout.transitionToEnd()
            } else {
                motionLayout.transitionToStart()
            }
        }

        continueButton.setOnClickListener {
            flagContinuePressed = true
            if (materialCheckBox.isChecked) {
                requireActivity().requestPermission(
                    Manifest.permission.READ_CONTACTS,
                    PermisssionRequestCodes.REQUEST_CODE_READ_CONTACTS,
                    getString(R.string.rational_cntcts)
                )
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (requireContext().hasReadContactsPermission() && flagContinuePressed)
            checkAndNavigateToPendingPermissionsScreen()
    }

    private fun checkAndNavigateToPendingPermissionsScreen() {
        val pendingPermissionsLeft =
            !requireActivity().hasReadPhoneStatePermission() ||
                    !Settings.canDrawOverlays(requireContext())

        if (pendingPermissionsLeft)
            viewModel.navigateToPendingPermissionScreen()
        else
            viewModel.navigateToEnd()
    }

    private fun resetAnimationState() {
        binding.materialCheckBox.isChecked = false
    }

}
