package com.nibble.hashcaller.view.ui.sms

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.sms.identifiedspam.SMSIdentifiedAsSpamFragment
import com.nibble.hashcaller.view.ui.sms.list.SMSListFragment
import com.nibble.hashcaller.view.ui.sms.schedule.ScheduleActivity
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_message_container.view.*


class SMSContainerFragment : Fragment(), IDefaultFragmentSelection,
    TabLayout.OnTabSelectedListener, View.OnClickListener {
    private var isDflt = false

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
            viewSms = messagesView


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
//            tabLayoutMessages.addOnTabSelectedListener(this)
            setListeners()

        }

    }



    private fun setListeners() {
        tabLayoutMessages.addOnTabSelectedListener(this)
        this.messagesView.fabBtnDeleteSMS.setOnClickListener(this)
    }

    private fun setupViewPager(viewPagerMessages: ViewPager?) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(SMSListFragment(), "Messages")
        viewPagerAdapter.addFragment(SMSIdentifiedAsSpamFragment(), "Identified as spam")
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



    fun toggleDeleteFab(){

        val visibility = this.messagesView.fabBtnDeleteSMS.visibility
        if(visibility == View.VISIBLE){
            this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE

        }else{
            this.messagesView.fabBtnDeleteSMS.visibility = View.VISIBLE

        }
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}



    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabReselected: ${tab?.position}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabUnselected: ${tab?.position}")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabSelected: ${tab?.position} ")
        if (tab != null) {
            when(tab.position){
                    0->{
                        this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE
                        this.messagesView.fabBtnDeleteSMSExpanded.visibility = View.INVISIBLE
                        this.messagesView.fabSendNewSMS.visibility = View.VISIBLE
                    }
                1->{
                    this.messagesView.fabSendNewSMS.visibility = View.INVISIBLE
                    this.messagesView.fabBtnDeleteSMSExpanded.visibility = View.VISIBLE
                    this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE


                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabBtnDeleteSMS, R.id.fabBtnDeleteSMSExpanded ->{
                val i = Intent(activity, ScheduleActivity::class.java)
                startActivity(i)
            }
        }
    }
    companion object {
        private const val TAG = "__SMSContainerFragment"
        var recyclerViewSpamSms:RecyclerView? = null
        var viewSms:View? = null
        fun show(){
            viewSms?.fabBtnDeleteSMS?.visibility = View.INVISIBLE

            viewSms?.fabBtnDeleteSMSExpanded?.animate()?.
            scaleX(1f)?.
            scaleY(1f)?.
            setDuration(10)?.
            start();
            viewSms?.fabBtnDeleteSMSExpanded?.visibility = View.VISIBLE

        }
        fun hide(){

            viewSms?.fabBtnDeleteSMS?.visibility = View.VISIBLE
            var width:Float = viewSms?.fabBtnDeleteSMS?.width?.toFloat()!!
            val animate = TranslateAnimation(0f, width, 0f,  0f )
            animate.duration = 500;
            animate.fillAfter = true;
//            viewSms?.fabBtnDeleteSMSExpanded?.startAnimation(animate)
            viewSms?.fabBtnDeleteSMSExpanded?.animate()?.
            scaleX(0.3f)?.
            scaleY(0.3f)?.
            setDuration(10)?.
            start();
            viewSms?.fabBtnDeleteSMSExpanded?.visibility = View.INVISIBLE
        }
    }


}