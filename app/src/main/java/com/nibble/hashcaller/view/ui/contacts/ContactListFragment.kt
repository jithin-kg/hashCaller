package com.nibble.hashcaller.view.ui.contacts

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactGlobalHelper
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_contact_list.*
import kotlinx.android.synthetic.main.fragment_contact_list.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactListFragment  : Fragment(), View.OnClickListener {
    private val TAG = "ContactListFragment"

    private lateinit  var contactViewModel: ContactsViewModel
    private lateinit var contactsView: View
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

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

        contactsView = inflater.inflate(R.layout.fragment_contact_list, container, false)
            contactViewModel = ViewModelProvider(this, ContacInjectorUtil.provideContactsViewModelFactory(
                context,
                lifecycleScope
            )).get(ContactsViewModel::class.java)
           if(checkContactPermission()){
               observerContactList()
           }
            observerIsLoading()
            observePermissionLiveData()
            initListeners()


        return contactsView
    }

    private fun initListeners() {
        this.contactsView.btnGivecontactPermission.setOnClickListener(this)
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


    private fun observerIsLoading() {
        ContactsViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
//                pgBarCntcList.visibility = View.VISIBLE

            }else{
//                pgBarCntcList.visibility = View.GONE
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if(PermissionUtil.requesetPermission(this.requireActivity()))
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
//                addItemDecoration(topSpacingDecorator)
//                contactsRecyclerAdapter = ContactAdapter(context) { id: Contact ->onContactItemClicked(id)}
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

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.btnGivecontactPermission ->{
                Log.d(TAG, "onClick: request permission")
//                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())
            }
        }
    }
}
