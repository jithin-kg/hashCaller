package com.nibble.hashcaller.view.ui.smsview

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.smsview.list.SMSListFragment
import com.nibble.hashcaller.view.ui.smsview.util.SMSViewModel
import kotlinx.android.synthetic.main.fragment_message_container.*


class SMSContainerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var messagesView:View
    private lateinit var smsListVIewModel: SMSViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if(checkPermission()){
            messagesView =  inflater.inflate(R.layout.fragment_message_container, container, false)



            return messagesView
        }else{
            return inflater.inflate(R.layout.request_permission, container, false)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(checkPermission()){
            setupViewPager(viewPagerMessages)
            tabLayoutMessages?.setupWithViewPager(viewPagerMessages)

        }

    }

    private fun setupViewPager(viewPagerMessages: ViewPager?) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(SMSListFragment(), "Messages")
        viewPagerAdapter.addFragment(SMSFragment(), "Identified as spam")
//        viewPagerAdapter.addFragment(ContactsIdentifiedFragment(), "Identified")
        viewPagerMessages!!.adapter = viewPagerAdapter
    }

    private fun checkPermission(): Boolean {
        var permissionGiven = false
        //persmission
        Dexter.withContext(this.activity)
            .withPermissions(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
//
                    report.let {
                        if(report?.areAllPermissionsGranted()!!){
                            permissionGiven = true
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


    companion object {
        private const val TAG = "__SMSContainerFragment"
            }

}