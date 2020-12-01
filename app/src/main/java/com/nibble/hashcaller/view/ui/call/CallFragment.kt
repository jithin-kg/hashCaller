package com.nibble.hashcaller.view.ui.call

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import kotlinx.android.synthetic.main.activity_main.*
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
class CallFragment : Fragment(),View.OnClickListener {
    // TODO: Rename and change types of parameters
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callFragment =  inflater.inflate(R.layout.fragment_call, container, false)


//        addFragmentDialer()
        return callFragment

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){
//            dialerFragment = DialerFragment()
        }


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        intialize()
        setupViewPager(viewPagerCall)
        tabLayoutCall?.setupWithViewPager(viewPagerCall)




//        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(this!!.layoutBottomSheet!!)
//        (bottomSheetBehavior as BottomSheetBehavior<*>).addBottomSheetCallback(object : BottomSheetCallback() {
//            override fun onStateChanged(
//                bottomSheet: View,
//                newState: Int
//            ) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_HIDDEN -> Toast.makeText(
//                        context,
//                        "hidden",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    BottomSheetBehavior.STATE_EXPANDED -> Toast.makeText(
//                        context,
//                        "expanded",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        Toast.makeText(context, "collapsed", Toast.LENGTH_SHORT).show()
////                        toggleBottomNavView()
//                    }
//                }
//            }
//
//            override fun onSlide(
//                bottomSheet: View,
//                slideOffset: Float
//            ) {
//                Log.d(TAG, "onSlide: ")
//            }
//        })


//    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
//    bottomSheetBehavior?.peekHeight = 0


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
            activity!!.findViewById<View>(R.id.bottomNavigationView)

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
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(CallHistoryFragment(), "Call History")
        viewPagerAdapter.addFragment(SpamCallFragment(), "Spam call History")
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


}