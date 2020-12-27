package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactGlobalHelper
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_contact_list.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactListFragment  : Fragment()  {
    private val TAG = "ContactListFragment"

    private lateinit  var contactViewModel: ContactsViewModel
    private lateinit var contactListViewFragment: View

//    var contacts: List<Contact>? = null
    var contactsRecyclerAdapter: ContactAdapter? = null
    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
//
//    fun ContactListFragment() {
//        // Required empty public constructor
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {


        }
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        retainInstance = true
        // Inflate the layout for this fragment

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        val contextThemeWrapper: Context =
//            ContextThemeWrapper(activity, R.style.MyDarkTheme)
        // clone the inflater using the ContextThemeWrapper
        // clone the inflater using the ContextThemeWrapper
//        val localInflater = inflater.cloneInContext(contextThemeWrapper)


        contactListViewFragment = inflater.inflate(R.layout.fragment_contact_list, container, false)
//        initRecyclerView()

        contactViewModel = ViewModelProvider(this, ContacInjectorUtil.provideContactsViewModelFactory(context)).get(ContactsViewModel::class.java)
//        contactViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        observerContactList()
        observerIsLoading()
        return contactListViewFragment
    }



    private fun observerIsLoading() {
        ContactsViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                pgBarCntcList.visibility = View.VISIBLE
            }else{
                pgBarCntcList.visibility = View.GONE
            }
         })
    }

    private fun observerContactList() {
        contactViewModel.contacts.observe(viewLifecycleOwner, Observer{contacts->
            contacts.let {
                contactsRecyclerAdapter?.setContactList(it)
                ContactGlobalHelper.size = contacts.size // setting the size in ContactsGlobalHelper

//                //sync contact with local db
//                contactViewModel.getCountOfContactFromLocalDb()?.observe(viewLifecycleOwner,Observer{count->
//                    count.let{
//                        contactViewModel?.syncContactsWithLocalDb(contacts, count)
//                    }
//                })


            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroy View")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroyed")
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "onDetach")
    }



        private fun initRecyclerView() {

            rcrViewContactsList?.apply {
                layoutManager = LinearLayoutManager(activity)
                val topSpacingDecorator =
                    TopSpacingItemDecoration(
                        30
                    )
                addItemDecoration(topSpacingDecorator)
                contactsRecyclerAdapter = ContactAdapter(context) { id: Contact ->onContactItemClicked(id)}
                adapter = contactsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
            }

//            val horizontalDecoration =  DividerItemDecoration(rcrViewContactsList.context,
//                DividerItemDecoration.VERTICAL);
//            val horizontalDivider = ContextCompat.getDrawable(this!!.requireActivity(), R.drawable.horizontal_line);
//            horizontalDecoration.setDrawable(horizontalDivider!!);
//            rcrViewContactsList.addItemDecoration(horizontalDecoration)


        }
    private fun onContactItemClicked(contactItem: Contact){
        Log.d(TAG, "onContactItemClicked: ${contactItem.phoneNumber}")
        val intent = Intent(context, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
        intent.putExtra("name", contactItem.name )
        intent.putExtra("id", contactItem.id)
        intent.putExtra("photo", contactItem.photoURI)
        startActivity(intent)
    }



}
