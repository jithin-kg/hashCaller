package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.search.ActivitySearchPhone
import com.nibble.hashcaller.view.ui.contacts.search.DetailsTransition
import com.nibble.hashcaller.view.ui.contacts.search.SearchFragment
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_search.*


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
    private var contactListFragment: ContactListFragment? = null

    private lateinit var scene1:Scene
    private lateinit var scene2:Scene
    private lateinit var currentScene:Scene
    private lateinit var transition:Transition

    var ContactViewFragment: View? = null
//    private val contactViewModel: ContactViewModel? = null

//        private RecyclerView contactsList;
    var recyclerView: RecyclerView? = null

//    var permissionsUtil: PermissionsUtil? = null

    fun ContactFragment() {}


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(savedInstanceState!= null){
            if(childFragmentManager.getFragment(savedInstanceState, "contactListFragment") != null)
                this.contactListFragment = childFragmentManager.getFragment(savedInstanceState, "contactListFragment") as ContactListFragment
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this.contactListFragment!=null){
            if(this.contactListFragment!!.isAdded){
                childFragmentManager.putFragment(outState,"contactListFragment",
                    this.contactListFragment!!
                )
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedElementEnterTransition=  TransitionInflater.from(activity).inflateTransition(R.transition.fragment_transition)
//        sharedElementEnterTransition=  TransitionInflater.from(activity).inflateTransition(R.transition.fragment_transition)

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
                if((activity as MainActivity).searchFragment!=null)
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
//        val intent = Intent(activity, ActivitySearchPhone::class.java)
//        intent.putExtra("animation", "explode")
//        Log.d(TAG, "startSearchActivity: $btnSampleTransition")
//        val options  = ActivityOptionsCompat.makeSceneTransitionAnimation(
//            this!!.requireActivity(), btnSampleTransition,
//            ViewCompat.getTransitionName(btnSampleTransition)!!
//        )
//        startActivity(intent, options.toBundle())

        val kittenDetails = (activity as MainActivity).searchFragment

        kittenDetails?.setSharedElementEnterTransition(DetailsTransition())
        kittenDetails?.setEnterTransition(Fade())
        //Todo add exit transition other than fade, fade is laggy in view for exit
//        exitTransition = Fade()

        kittenDetails?.setSharedElementReturnTransition(DetailsTransition())

        (activity as MainActivity).bottomNavigationView.visibility = View.GONE

        requireActivity().supportFragmentManager
            .beginTransaction()
            .addSharedElement(searchViewContacts, searchViewContacts.transitionName)
            .replace(R.id.frame_fragmentholder, kittenDetails!!)
            .addToBackStack(null)
            .commit()

    }


    private fun initialize() {
        toolbar = ContactViewFragment?.findViewById(R.id.toolbar)
        tabLayout = ContactViewFragment?.findViewById(R.id.tabLayout)
        viewPager = ContactViewFragment?.findViewById(R.id.viewPager)
        searchViewContacts = ContactViewFragment?.findViewById(R.id.searchViewContacts)!!


    }


    //nested
    private fun setupViewPager(viewPager: ViewPager?) {
        if(this.contactListFragment == null){
            this.contactListFragment = ContactListFragment()
        }
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(this.contactListFragment!!, "Contacts")
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
        if((activity as MainActivity).searchFragment!=null){
            startSearchActivity()

        }else{
            Log.d(TAG, "onClick: searchfragment is null")
        }
      }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        btnTest.setOnClickListener(this)
        ViewCompat.setTransitionName(searchViewContacts, searchViewContacts.transitionName)

    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}