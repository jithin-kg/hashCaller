package com.hashcaller.app.view.ui.auth.permissionrequest

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hashcaller.app.R
import com.hashcaller.app.databinding.OtherPendingPermissionsFragmentBinding
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.auth.permissionrequest.permissionitem.PermissionItemView
import com.hashcaller.app.view.ui.contacts.hasReadPhoneStatePermission
import com.hashcaller.app.view.ui.extensions.isScreeningRoleHeld
import com.hashcaller.app.view.ui.extensions.requestAlertWindowPermission
import com.hashcaller.app.view.ui.sms.individual.util.shouldReqstScreeningRole
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
//    private lateinit var overlayPermissionItem: PermissionItemView
    private lateinit var screeningPermissionItems: PermissionItemView
    private lateinit var scrnRoleCallback: ActivityResultLauncher<Intent>

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
        regstrScreeningRoleResultCb()
    }

    private fun observe() {

        viewModel.onReadPhoneStatePermissionGranted = {
            phoneStatePermissionItem.updateStatusSuccess()
            binding.motionLayout.transitionToEnd()
        }
        viewModel.onScreeningPermissionGranted = {
            screeningPermissionItems.updateStatusSuccess()

        }
    }

    fun regstrScreeningRoleResultCb() {
        scrnRoleCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
               screeningPermissionItems.updateStatusSuccess()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
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
        if(requireContext().shouldReqstScreeningRole().first){
            screeningPermissionItems = PermissionItemView(
                requireContext(),
                R.drawable.ic_phone_line_white,
                "Default Caller ID",
                "For Call blocking and caller Id to work properly enable HashCaller as your default Caller ID",
                false
            ){
                val res = requireContext().shouldReqstScreeningRole()
                if(res.first){
                    //we should request screening role
                    val intent = res.second?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    scrnRoleCallback.launch(intent)
                }
            }
            container.addView(screeningPermissionItems, MATCH_PARENT, WRAP_CONTENT)
        }


        /**
         * Normally the continue button is hidden and is displayed once phonestate permission uis given.
         * but if phone state is already given, we have to manually set animation to end state to show continue button.
         */
        if (requireContext().hasReadPhoneStatePermission())
            binding.motionLayout.transitionToEnd()


        continueButton.setOnClickListener {
//            viewModel.navigateToEnd()
            if(!Settings.canDrawOverlays(requireContext())){
                viewModel.navigateToOverlayPermissionScreen()
            }else {
                viewModel.navigateToEnd()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if((activity as AppCompatActivity).isScreeningRoleHeld() && ::screeningPermissionItems.isInitialized){
                screeningPermissionItems.updateStatusSuccess()
            }
        }
            if (requireContext().hasReadPhoneStatePermission() && ::phoneStatePermissionItem.isInitialized) {
                phoneStatePermissionItem.updateStatusSuccess()
                binding.motionLayout.transitionToEnd()
            }

    }


}