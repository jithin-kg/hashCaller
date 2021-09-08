package com.hashcaller.app.view.ui.auth.permissionrequest

import android.Manifest
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hashcaller.app.R
import com.hashcaller.app.databinding.OtherPendingPermissionsFragmentBinding
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.view.ui.auth.permissionrequest.permissionitem.PermissionItemView
import com.hashcaller.app.view.ui.contacts.hasReadPhoneStatePermission
import com.hashcaller.app.view.ui.extensions.requestAlertWindowPermission
import com.hashcaller.app.view.utils.requestPermissionsActivity
import com.vmadalin.easypermissions.EasyPermissions

class OtherPendingPermissionsFragment : Fragment() {

    companion object {
        fun newInstance() = OtherPendingPermissionsFragment()
        private const val TAG = "OtherPendingPermissions"
    }
    private val viewModel: PermissionRequestViewModel by activityViewModels()
    private lateinit var binding: OtherPendingPermissionsFragmentBinding

    private lateinit var phoneStatePermissionItem: PermissionItemView
    private lateinit var overlayPermissionItem: PermissionItemView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OtherPendingPermissionsFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        observe()
    }

    private fun observe() {

        viewModel.onReadPhoneStatePermissionGranted = {
            phoneStatePermissionItem.updateStatusSuccess()
            binding.motionLayout.transitionToEnd()
        }
    }

    private fun setupUi() = with(binding) {

        if (!requireContext().hasReadPhoneStatePermission()) {

            phoneStatePermissionItem = PermissionItemView(
                requireContext(),
                R.drawable.ic_phone_line_white,
                "Phone Access",
                getString(R.string.to_identify_and_block),
                true,
            )
            {
                requireActivity().requestPermissionsActivity(
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ANSWER_PHONE_CALLS
                    ),
                    PermisssionRequestCodes.REQUEST_CODE_READ_PHONE_STATE,
                    getString(R.string.to_identify_and_block)
                )
            }

            container.addView(phoneStatePermissionItem, MATCH_PARENT, WRAP_CONTENT)
        }

        if (!Settings.canDrawOverlays(this@OtherPendingPermissionsFragment.requireContext())) {
            overlayPermissionItem = PermissionItemView(
                requireContext(),
                R.drawable.ic_contacts_book_2_line_white,
                "Overlay Permission",
                getString(R.string.overly_description),
                false,
            )
            {
                (requireActivity() as AppCompatActivity).requestAlertWindowPermission()

            }

            container.addView(overlayPermissionItem, MATCH_PARENT, WRAP_CONTENT)
        }

        /**
         * Normally the continue button is hidden and is displayed once phonestate permission uis given.
         * but if phone state is already given, we have to manually set animation to end state to show continue button.
         */
        if (requireContext().hasReadPhoneStatePermission())
            binding.motionLayout.transitionToEnd()


        continueButton.setOnClickListener {
            viewModel.navigateToEnd()
        }

    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this@OtherPendingPermissionsFragment.requireContext())) {
            if (::overlayPermissionItem.isInitialized) {
                overlayPermissionItem.updateStatusSuccess()
            }
            if (requireContext().hasReadPhoneStatePermission() && ::phoneStatePermissionItem.isInitialized) {
                phoneStatePermissionItem.updateStatusSuccess()
                binding.motionLayout.transitionToEnd()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}