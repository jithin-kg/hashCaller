package com.hashcaller.app.view.ui.call

import android.Manifest.permission.*
import android.app.ActivityOptions
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.databinding.CallContainerFragmentBinding
import com.hashcaller.app.databinding.FragmentCallBinding
import com.hashcaller.app.utils.Constants.Companion.SPAMMER_TYPE_BUSINESS
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_CALL_LOG
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.extensions.startSearchActivity
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_1_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_2_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_3_CALL_SERVICE_ID
import com.hashcaller.app.view.adapter.ViewPagerAdapter
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.MainActivityInjectorUtil
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListActivity
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.OLDER
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.YESTERDAY
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.dialer.CallLogAdapter
import com.hashcaller.app.view.ui.call.dialer.DialerFragment
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.call.floating.NOTIFICATION_CHANNEL_GENERAL
import com.hashcaller.app.view.ui.call.individualCallLog.IndividualCallLogActivity
import com.hashcaller.app.view.ui.call.spam.SpamCallFragment
import com.hashcaller.app.view.ui.call.utils.CallContainerInjectorUtil
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.extensions.isScreeningRoleHeld
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.utils.ConfirmDialogFragment
import com.hashcaller.app.view.utils.ConfirmationClickListener
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.getRelativeTime
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.call_list.*
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass.
 * Use the [CallContainerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallContainerFragment : Fragment(),IDefaultFragmentSelection{

    private  lateinit var binding: CallContainerFragmentBinding
    private var toolbar: Toolbar? = null
    private var isDflt = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var radioSales:RadioButton?= null
    private  var radioScam:RadioButton?= null
    private  var radioBusiness:RadioButton?= null
    private  var radioPerson:RadioButton?= null
    private  var selectedRadioButton: RadioButton? = null
    private var radioGroupOne: RadioGroup? = null
    private var radioGroupTwo: RadioGroup? = null
    private var btnBlock:Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    binding = CallContainerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        initListeners()
        setupBottomSheet()
    }

    private fun initListeners() {
        binding.imgBtnCallTbrBlock.setOnClickListener{
            showBottomSheetDialog()
        }
        binding.imgBtnCallSearch.setOnClickListener{
            activity?.startSearchActivity()
        }
        binding.imgBtnHamBrgerCalls.setOnClickListener{
            (activity as MainActivity).showDrawer()

        }

    }
    private suspend fun observeMarkedItems() {
        //todo add view model
//        viewmodel?.markeditemsHelper?.markedItems?.observe(viewLifecycleOwner, Observer {
//            when(it.size){
//                0 ->{
//                    showSearchView()
//                }
//                else ->{
//                    showBlockBtnInToolbar(it.size)
//                }
//
//            }
//        })
    }

    private fun blockMarkedCaller() {
//        this.viewmodel?.blockThisAddress(
//            this.spammerType,
//            context?.applicationContext)?.observe(viewLifecycleOwner, Observer {
//            when(it){
//                ON_COMPLETED -> {
//                    viewmodel?.clearMarkedItems()
//                    bottomSheetDialog.hide()
//                    bottomSheetDialog.dismiss()
//                    bottomSheetDialogfeedback.show()
//                    showSearchView()
//                }
//            }
//        })
    }

    fun showSearchView(){

        binding.imgBtnCallTbrBlock.beInvisible()
        binding. imgBtnCallTbrMuteCaller.beInvisible()
        binding.tvCallSelectedCount.beInvisible()
        binding.imgBtnCallUnMuteCaller.beInvisible()
        binding.pgBarDeleting.beInvisible()
        binding.tvVHashcaller.beVisible()
        binding.imgBtnHamBrgerCalls.beVisible()
    }
    private fun showBlockBtnInToolbar(count: Int) {
        updateSelectedItemCount(count)
        binding.imgBtnCallSearch.beInvisible()
        binding.imgBtnCallTbrBlock.beVisible()

//        if(isScreeningApp){
////            binding.imgBtnCallTbrMuteCaller.beVisible()
//        }
        binding.imgBtnCallSearch.beInvisible()
        binding.tvVHashcaller.beInvisible()
        binding.imgBtnHamBrgerCalls.beInvisible()

    }
    fun updateSelectedItemCount(count: Int) {
        binding.tvCallSelectedCount.text = "${count.toString()} Selected"
        binding.tvCallSelectedCount.beVisible()
    }
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity())
        bottomSheetDialogfeedback = BottomSheetDialog(this.requireActivity())
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness) as RadioButton
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson) as RadioButton
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales) as RadioButton
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam) as RadioButton

        radioGroupOne = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroupOne) as RadioGroup
        radioGroupTwo = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioPersonOrBusiness) as RadioGroup

        btnBlock = bottomSheetDialog.findViewById(R.id.btnBlock)
        selectedRadioButton = radioScam
    }
    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()

    }
    private fun setupViewPager(viewPager: ViewPager?) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(CallFragment(), "Calls")
        viewPagerAdapter.addFragment(SpamCallFragment(), "Spam calls")
//        viewPagerAdapter.addFragment(ContactsIdentifiedFragment(), "Identified")
        viewPager!!.adapter = viewPagerAdapter
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){

        }

    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
    /**
     * important to prevent memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
    

    companion object {
            private const val TAG ="__CallFragment"


    
    }

    override fun onPause() {
        super.onPause()
    }

    fun clearMarkeditems() {

    }

    fun activtyResultisDefaultScreening() {

    }

    fun getMarkedItemsSize(): Int {
        return 0
    }

}