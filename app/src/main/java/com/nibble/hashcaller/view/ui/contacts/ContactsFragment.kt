package com.nibble.hashcaller.view.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactsFragment : Fragment() {
    private val TAG = "ContactFragment"
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null


    var ContactViewFragment: View? = null
//    private val contactViewModel: ContactViewModel? = null

    //    private RecyclerView contactsList;
    var recyclerView: RecyclerView? = null

//    var permissionsUtil: PermissionsUtil? = null

    fun ContactFragment() {}


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
        ContactViewFragment = inflater.inflate(R.layout.fragment_contacts, container, false)
//        if (!checkPermission()) {
//            return null
//        }
        initialize()
        setupViewPager(viewPager)
        tabLayout!!.setupWithViewPager(viewPager)
        return ContactViewFragment
    }

    private fun initialize() {
        toolbar = ContactViewFragment!!.findViewById(R.id.toolbar)
        tabLayout = ContactViewFragment!!.findViewById(R.id.tabLayout)
        viewPager = ContactViewFragment!!.findViewById(R.id.viewPager)
    }


//    private fun checkPermission(): Boolean {
//        val permissionsUtil = PermissionsUtil(activity)
//        if (!permissionsUtil.checkPermissions()) {
//            startActivity(Intent(activity, ActivityRequestPermission::class.java))
//            return false
//        }
//        return true
//    }

    //nested
    private fun setupViewPager(viewPager: ViewPager?) {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(ContactListFragment(), "Contacts")
//        viewPagerAdapter.addFragment(ContactsIdentifiedFragment(), "Identified")
        viewPager!!.adapter = viewPagerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroyed")
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "onDetach")
    }
}