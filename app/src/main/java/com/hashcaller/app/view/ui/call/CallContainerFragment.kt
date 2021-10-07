package com.hashcaller.app.view.ui.call

import android.Manifest.permission.*
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.hashcaller.app.R
import com.hashcaller.app.databinding.CallContainerFragmentBinding
import com.hashcaller.app.utils.extensions.startSearchActivity
import com.hashcaller.app.view.adapter.ViewPagerAdapter
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.call.spam.SpamCallFragment
import com.hashcaller.app.view.ui.contacts.startFloatingService
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.call_list.*
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*

import kotlinx.android.synthetic.main.call_container_fragment.*

import com.hashcaller.app.view.ui.utils.IMarkingHelper
import kotlinx.coroutines.delay


/**
 * A simple [Fragment] subclass.
 * Use the [CallContainerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallContainerFragment : Fragment(),IDefaultFragmentSelection, IMarkingHelper,
    View.OnClickListener {

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
    private lateinit var markhelperViewmodel: MarkhelperViewmodel

//    private val markViewmodel: MarkhelperViewmodel by viewModels()

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
        markhelperViewmodel = ViewModelProvider(requireActivity()).get(MarkhelperViewmodel::class.java)
        setupBottomSheet()
        initListeners()


    }

    private fun initListeners() {
        binding.imgBtnCallTbrBlock.setOnClickListener(this)
//        binding.imgBtnCallTbrBlock.setOnClickListener{
//            showBottomSheetDialog()
//        }
        binding.imgBtnCallSearch.setOnClickListener{
            activity?.startSearchActivity()
        }
        binding.imgBtnHamBrgerCalls.setOnClickListener{
            (activity as MainActivity).showDrawer()

        }
        binding.fabBtnShowDialpad.setOnClickListener{
            (activity as MainActivity).showDialerFragment()
        }



    }
    private fun blockMarkedCaller() {

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
    private fun setupViewPager(viewPager: ViewPager) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(CallFragment(this), "Calls")
        viewPagerAdapter.addFragment(SpamCallFragment(), "Spam calls")
//        viewPagerAdapter.addFragment(ContactsIdentifiedFragment(), "Identified")
        viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

//        val tv = binding.tabLayout.getChildAt(1).findViewById<TextView>(android.R.id.title)
//        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.spamText))
//
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(TAG, "onTabSelected: ${tab?.position}")
                if(tab?.position == 1){
//                    tabLayout.setTabTextColors(ContextCompat.getColorStateList(requireContext(), R.color.spamText));
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.red_varient_1));
                    markhelperViewmodel.onHddenStateChange(true)
//                    tabLayout.setSelectedTabT
                }else {
//                    tabLayout.setTabTextColors(ContextCompat.getColorStateList(requireContext(), R.color.colorWhite));
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "onTabUnselected: ${tab?.position}")

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "onTabReselected: ${tab?.position}")

            }

        })

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
            private const val TAG ="__CallContainerFragment"
    }

    override fun onPause() {
        super.onPause()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d(TAG, "onHiddenChanged: $hidden")
        markhelperViewmodel.onHddenStateChange(hidden)

    }
    fun clearMarkeditems() {
        markhelperViewmodel.onCallFragmentHidden()

    }

    fun activtyResultisDefaultScreening() {

    }

    fun getMarkedItemsSize(): Int {
        return markhelperViewmodel.getMakedItemsSize()
    }

    override fun showBlockBtnInToolbar(count: Int) {
        updateSelectedItemCount(count)
        binding.imgBtnCallSearch.beInvisible()
        binding.imgBtnCallTbrBlock.beVisible()
        binding.imgBtnCallSearch.beInvisible()
        binding.tvVHashcaller.beInvisible()
        binding.imgBtnHamBrgerCalls.beInvisible()
    }

    override fun showSearchView() {
        binding.imgBtnCallTbrBlock.beInvisible()
        binding. imgBtnCallTbrMuteCaller.beInvisible()
        binding.tvCallSelectedCount.beInvisible()
        binding.imgBtnCallUnMuteCaller.beInvisible()
        binding.pgBarDeleting.beInvisible()
        binding.tvVHashcaller.beVisible()
        binding.imgBtnHamBrgerCalls.beVisible()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnCallTbrBlock -> {
//                requireContext().startFloatingService(TelephonyManager.EXTRA_STATE_RINGING )
                Log.d(TAG, "initListeners: ")

//                (activity as MainActivity).toggleBotomSheet()
                markhelperViewmodel.toggleBottomSheet(true)
                lifecycleScope.launchWhenStarted {
                    delay(300L)
                    markhelperViewmodel.toggleBottomSheet(false)

                }
            }

        }
    }


}
