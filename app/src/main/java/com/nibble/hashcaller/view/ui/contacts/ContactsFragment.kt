package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat.getActionView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.contacts.search.ActivitySearchPhone
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactsFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection {
    private var isDflt = false
    private val TAG = "__ContactFragment"
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    private lateinit var searchViewContacts:EditText


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
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
       //dark theme
        val contextThemeWrapper: Context =
            ContextThemeWrapper(activity, R.style.Theme_MyDarkTheme)
        // clone the inflater using the ContextThemeWrapper
        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        // Inflate the layout for this fragment
        ContactViewFragment = localInflater.inflate(R.layout.fragment_contacts, container, false)
//        if (!checkPermission()) {
//            return null
//        }
        initialize()
        setupViewPager(viewPager)
        tabLayout!!.setupWithViewPager(viewPager)



        searchViewContacts.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->

            if (hasFocus) {
                startSearchActivity()
            }
        }

//        searchViewContacts.setOnQueryTextListener(object :
//            SearchView.OnQueryTextListener, android.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                Log.d(TAG, "onQueryTextSubmit: ")
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                //    adapter.getFilter().filter(newText);
//                Log.d(TAG, "onQueryTextChange: ")
//                return false
//            }
//        })
        searchViewContacts.setOnClickListener(this)
        return ContactViewFragment
    }



    private fun startSearchActivity() {
        val intent = Intent(activity, ActivitySearchPhone::class.java)
        startActivity(intent)
    }


    private fun initialize() {
        toolbar = ContactViewFragment?.findViewById(R.id.toolbar)
        tabLayout = ContactViewFragment?.findViewById(R.id.tabLayout)
        viewPager = ContactViewFragment?.findViewById(R.id.viewPager)
        searchViewContacts = ContactViewFragment?.findViewById(R.id.searchViewContacts)!!

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

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: searchview")
       startSearchActivity()
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}