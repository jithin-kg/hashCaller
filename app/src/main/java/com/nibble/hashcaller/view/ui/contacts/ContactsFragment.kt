package com.nibble.hashcaller.view.ui.contacts

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.search.ActivitySearchPhone
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactGlobalHelper
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_contact_list.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*
import kotlinx.android.synthetic.main.fragment_search.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ContactsFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection {
    private var isDflt = false
    private val TAG = "__ContactFragment"
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var toolbar: Toolbar? = null
    private lateinit var searchViewContacts:EditText
    private var contactListFragment: ContactListFragment? = null

    private lateinit  var contactViewModel: ContactsViewModel
    private lateinit var contactsView: View
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var contactsRecyclerAdapter: ContactAdapter? = null

//    private val contactViewModel: ContactViewModel? = null

//        private RecyclerView contactsList;
    var recyclerView: RecyclerView? = null

//    var permissionsUtil: PermissionsUtil? = null

    fun ContactFragment() {}


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

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
        contactsView = inflater.inflate(R.layout.fragment_contact_list, container, false)

        contactViewModel = ViewModelProvider(this, ContacInjectorUtil.provideContactsViewModelFactory(context)).get(ContactsViewModel::class.java)
        if(checkContactPermission()){
            observerContactList()
        }

        observePermissionLiveData()



        return contactsView
    }
    private fun initListeners() {
        this.contactsView.btnGivecontactPermission.setOnClickListener(this)
        toolbar = contactsView?.findViewById(R.id.toolbar)

        searchViewContacts = contactsView?.findViewById(R.id.searchViewContacts)!!
        contactsView.searchViewContacts.setOnClickListener(this)

    }
    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { value->
            if(value == true){
                this.contactsView.btnGivecontactPermission.visibility = View.GONE
                this.contactsView.tvCntctPermissionInfo.visibility = View.GONE
//                this.contactsView.pgBarCntcList.visibility = View.VISIBLE
                observerContactList()
            }else{
                this.contactsView.btnGivecontactPermission.visibility = View.VISIBLE
                this.contactsView.tvCntctPermissionInfo.visibility = View.VISIBLE
//                this.contactsView.pgBarCntcList.visibility = View.GONE

                if (this.contactViewModel!! != null  ) {
                    if(this.contactViewModel?.contacts != null)
                        if(this.contactViewModel.contacts!!.hasObservers())
                            this.contactViewModel?.contacts?.removeObservers(this);
                }


            }
        })
    }

    private fun observerContactList() {
        try {
            contactViewModel.contacts?.observe(viewLifecycleOwner, Observer{contacts->
                contacts.let {
//                    this.contactsView.pgBarCntcList.visibility = View.GONE
                    contactsRecyclerAdapter?.setContactList(it)
                    ContactGlobalHelper.size = contacts.size // setting the size in ContactsGlobalHelper
                }
            })
        }catch (e:Exception){
            Log.d(TAG, "observerContactList: exception $e")
        }

    }



    private fun startSearchActivity() {
        val intent = Intent(activity, ActivitySearchPhone::class.java)
        intent.putExtra("animation", "explode")
        Log.d(TAG, "startSearchActivity: $btnSampleTransition")
        val p1 = android.util.Pair(searchViewContacts as View,"editTextTransition")

        val options = ActivityOptions.makeSceneTransitionAnimation(activity,p1 )
        startActivity(intent, options.toBundle())


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
        when(v?.id){
            R.id.btnGivecontactPermission -> {
                Log.d(TAG, "onClick: request permission")
                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())

            }
            R.id.searchViewContacts->{
            startSearchActivity()
        }

            else->{

//            if((activity as MainActivity).searchFragment!=null){
//                startSearchActivity()
//
//            }else{
//                Log.d(TAG, "onClick: searchfragment is null")
//            }
        }
        }

      }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        ViewCompat.setTransitionName(searchViewContacts, searchViewContacts.transitionName)
        initRecyclerView()
        searchViewContacts.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->

            if (hasFocus) {
//                if((activity as MainActivity).searchFragment!=null)
//                    startSearchActivity()
            }
        }
        searchViewContacts.setOnClickListener(this)


    }

    private fun initRecyclerView() {

        rcrViewContactsList?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
//                addItemDecoration(topSpacingDecorator)
            contactsRecyclerAdapter = ContactAdapter(context) { id: Contact ->onContactItemClicked(id)}
            adapter = contactsRecyclerAdapter

        }




    }
    private fun onContactItemClicked(contactItem: Contact){
        Log.d(TAG, "onContactItemClicked: ${contactItem.phoneNumber}")
        val intent = Intent(context, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
        intent.putExtra("name", contactItem.name )
        intent.putExtra("id", contactItem.id)
        intent.putExtra("photo", contactItem.photoURI)

        val pairList = ArrayList<android.util.Pair<View, String>>()
        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
        val p2 = android.util.Pair(textVContactName as View, "contactNameTransition")
        pairList.add(p1)
        pairList.add(p2)
        val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0], pairList[1]  )


        startActivity(intent, options.toBundle())
    }

    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkContactPermission()

    }
    private fun checkContactPermission(): Boolean {
        val permissionContact =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_CALL_LOG)
        if(permissionContact!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }



    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}