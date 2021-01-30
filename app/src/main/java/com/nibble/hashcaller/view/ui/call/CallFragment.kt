package com.nibble.hashcaller.view.ui.call

import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import com.nibble.hashcaller.view.ui.call.utils.CallContainerInjectorUtil
import com.nibble.hashcaller.view.ui.call.work.CallContainerViewModel
import com.nibble.hashcaller.view.ui.sms.SMSContainerInjectorUtil
import com.nibble.hashcaller.view.ui.sms.SmsContainerViewModel
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import kotlinx.android.synthetic.main.fragment_call.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallFragment : Fragment(),View.OnClickListener , IDefaultFragmentSelection {
    private var isDflt = false
    private  var callHistoryFragment:CallHistoryFragment? = null
    private  var spamCallFragment: SpamCallFragment? = null
    private var param1: String? = null
    private var param2: String? = null
    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    var callFragment: View? = null
    private var tabLayout: TabLayout? = null
    private lateinit var searchViewCall: EditText
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: ConstraintLayout? = null
    private lateinit var dialerFragment: DialerFragment
    private lateinit var viewmodel: CallContainerViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        callFragment =  inflater.inflate(R.layout.fragment_call, container, false)
        initViewModel()

//        addFragmentDialer()
        return callFragment

    }

    private fun initViewModel() {
        this.viewmodel = ViewModelProvider(this, CallContainerInjectorUtil.provideViewModelFactory(context)).get(
            CallContainerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){

        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)
//        intialize()
        setupViewPager(viewPagerCall)
        tabLayoutCall?.setupWithViewPager(viewPagerCall)



    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this.callHistoryFragment!=null){
            if(this.callHistoryFragment!!.isAdded){
                childFragmentManager.putFragment(outState,"callHistoryFragment", this.callHistoryFragment!!)
                childFragmentManager.putFragment(outState,"spamCallFragment", this.spamCallFragment!!)
            }
        }


    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: ")
        super.onActivityCreated(savedInstanceState)
        if(savedInstanceState!= null){
            if(childFragmentManager.getFragment(savedInstanceState, "callHistoryFragment")!=null){
                this.callHistoryFragment = childFragmentManager.getFragment(savedInstanceState, "callHistoryFragment") as CallHistoryFragment?
                this.spamCallFragment = childFragmentManager.getFragment(savedInstanceState, "spamCallFragment") as SpamCallFragment?
            }

        }
    }

    private fun addFragmentDialer() {

       val  ft = childFragmentManager.beginTransaction()
        ft?.add(R.id.frameFragmentDialer, dialerFragment)
        ft.replace(R.id.frameFragmentDialer, dialerFragment).commit();

    }



    private fun intialize() {
//        fabBtnShowDialpad?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_add_24))
//        fabBtnShowDialpad?.setOnClickListener(this as View.OnClickListener)
//        layoutBottomSheet = callFragment!!.findViewById(R.id.bottom_sheet)
    }

    private fun toggleBottomNavView() {
        val bottomNavigation =
            requireActivity().findViewById<View>(R.id.bottomNavigationView)

        if (bottomNavigation.visibility == View.VISIBLE) {
            bottomNavigation.visibility = View.INVISIBLE
//            fabBtnShowDialpad.hide()
            tabLayout!!.visibility = View.INVISIBLE
            //            bottomSheetBehavior.setPeekHeight((Resources.getSystem().getDisplayMetrics().heightPixels)/2);
            return
        }
        bottomNavigation.visibility = View.VISIBLE
//        fabBtnShowDialpad.show()
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        Log.d(TAG, "setupViewPager: ")
        if(this.callHistoryFragment == null){
            this.callHistoryFragment = CallHistoryFragment()
            this.spamCallFragment = SpamCallFragment()
        }

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(this.callHistoryFragment!!, "Call History")
        viewPagerAdapter.addFragment(this.spamCallFragment!!, "Spam call History")
//        viewPagerAdapter.addFragment(ContactsIdentifiedFragment(), "Identified")
        viewPager!!.adapter = viewPagerAdapter

    }

    private fun initialize() {
        toolbar = callFragment?.findViewById(R.id.toolbar)
        tabLayout = callFragment?.findViewById(R.id.tabLayout)
        viewPager = callFragment?.findViewById(R.id.viewPager)
        searchViewCall = callFragment?.findViewById(R.id.searchViewCall)!!
    }

    companion object {
            private const val TAG ="__CallFragment"
    
    }


    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when (v?.id) {
//            R.id.fabBtnShowDialpad-> {
//                Log.d(TAG, "onClick: show dialpad button clicked")
////                if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
//////                    toggleBottomNavView()
////                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
////                    Log.d(TAG, "onClick:  hiding")
////                }
//                showDialerFragment()
////                else{
////                    Log.d(TAG, "onClick: expanding")
////                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
////                }
//
//            }else->{
//            Log.d(TAG, "onClick: clicked ")
//        }
        }
    }

    private fun showDialerFragment() {
        val ft = childFragmentManager.beginTransaction()
        if (dialerFragment.isAdded) { // if the fragment is already in container
            ft?.show(dialerFragment)
        }else{
            Log.d(TAG, "showDialerFragment:  fragment is not added")
        }
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}

}