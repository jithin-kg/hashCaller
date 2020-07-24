package com.nibble.hashcaller.view.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        retainInstance = true
        // Inflate the layout for this fragment
        contactListViewFragment = inflater.inflate(R.layout.fragment_contact_list, container, false)
//        initRecyclerView()

        contactViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        contactViewModel.contacts.observe(viewLifecycleOwner, Observer{contacts->
            contacts.let {
                contactsRecyclerAdapter?.setContactList(it)
            }
        })

        return contactListViewFragment
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
                contactsRecyclerAdapter = ContactAdapter(context) { id:Long->onContactItemClicked(id)}
                adapter = contactsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
            }


        }
    private fun onContactItemClicked(id:Long){
        Log.d(TAG, "onContactItemClicked: $id")
        val intent = Intent(context, IndividualCotactViewActivity::class.java )
        intent.putExtra(CONTACT_ID, id)
        startActivity(intent)
    }



}
