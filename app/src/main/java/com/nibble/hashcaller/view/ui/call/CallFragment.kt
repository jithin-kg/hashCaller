package com.nibble.hashcaller.view.ui.call

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.contacts.ContactListFragment
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
class CallFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    var callFragment: View? = null
    private var tabLayout: TabLayout? = null
    private lateinit var searchViewCall: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callFragment =  inflater.inflate(R.layout.fragment_call, container, false)
        initialize()
        setupViewPager(viewPager)
        tabLayout!!.setupWithViewPager(viewPager)
        searchViewCall.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->

            if (hasFocus) {
                //TODO inflate search activity
//                startSearchActivity()
            }
        }

        return callFragment

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


    }
}