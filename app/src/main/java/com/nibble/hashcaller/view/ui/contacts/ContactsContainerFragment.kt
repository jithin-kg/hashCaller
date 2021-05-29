package com.nibble.hashcaller.view.ui.contacts

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ContactListBinding
import com.nibble.hashcaller.databinding.FragmentContactsContainerBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_CONTACTS
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.MainActivityInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.nibble.hashcaller.view.ui.contacts.search.ActivitySerchContacts
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactGlobalHelper
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import com.nibble.hashcaller.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.delay

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsContainerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactsContainerFragment : Fragment() , View.OnClickListener, IDefaultFragmentSelection {
    private var _binding: FragmentContactsContainerBinding? = null

    private val binding get() = _binding!!
    private var isDflt = false
    private val TAG = "__ContactFragment"
    private lateinit var sharedUserInfoViewmodel: UserInfoViewModel


    private var toolbar: Toolbar? = null
    private var contactListFragment: ContactListFragment? = null

    private lateinit  var contactViewModel: ContactsViewModel
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    var contactsRecyclerAdapter: ContactAdapter? = null

//    private val contactViewModel: ContactViewModel? = null

    //        private RecyclerView contactsList;
    var recyclerView: RecyclerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }
    /**
     * important to prevent memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentContactsContainerBinding.inflate(inflater, container, false)

        initListeners()
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper =  TokenHelper(user)

        ViewCompat.setTransitionName(binding.searchViewContacts, binding.searchViewContacts.transitionName)
        val contextThemeWrapper: Context =
            ContextThemeWrapper(activity, R.style.Theme_MyDarkTheme)

        if(context?.hasReadContactsPermission() == true){
            initRecyclerView()
            getData()
        }else{
            hideRecyevlerView()
        }

        binding.searchViewContacts.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->

            if (hasFocus) {
//                if((activity as MainActivity).searchFragment!=null)
//                    startSearchActivity()
            }
        }
        binding.searchViewContacts.setOnClickListener(this)

    return binding.root
    }


    private fun observeUserInfo() {
        sharedUserInfoViewmodel.userInfoLivedata.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
//                binding.tvCntctPermissionInfo.text = fLetter
            }
        })
    }

     fun getData() {
        showRecyclerView()
        lifecycleScope.launchWhenStarted {
            delay(2000L)
            initViewmodel()
            observerContactList()
            observeUserInfo()

        }
    }

    private fun showRecyclerView() {
        binding.btnGivecontactPermission.beInvisible()
        binding.rcrViewContactsList.beVisible()
        binding.shimmerViewContainer.beInvisible()
    }

    private fun hideRecyevlerView() {
        binding.rcrViewContactsList.beInvisible()
        binding.shimmerViewContainer.beInvisible()
        binding.btnGivecontactPermission.beVisible()
    }


    private fun initViewmodel() {
        contactViewModel = ViewModelProvider(this, ContacInjectorUtil.provideContactsViewModelFactory(
            context?.applicationContext,
            lifecycleScope,
            TokenHelper( FirebaseAuth.getInstance().currentUser)
        )).get(ContactsViewModel::class.java)
        sharedUserInfoViewmodel = ViewModelProvider(this, MainActivityInjectorUtil.provideUserInjectorUtil(
            context?.applicationContext!!,
            tokenHelper
        )).get(
            UserInfoViewModel::class.java
        )

    }

    private fun initRecyclerView() {

       binding.rcrViewContactsList?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
//                addItemDecoration(topSpacingDecorator)
            contactsRecyclerAdapter = ContactAdapter(context) { binding: ContactListBinding, contact: Contact ->context.onContactItemClicked(binding, contact, activity)}
            adapter = contactsRecyclerAdapter

        }

    }
//     fun onContactItemClicked(binding: ContactListBinding, contactItem: Contact){
//        Log.d(TAG, "onContactItemClicked: ${contactItem.phoneNumber}")
//        val intent = Intent(context, IndividualContactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
//        intent.putExtra("name", contactItem.name )
////        intent.putExtra("id", contactItem.id)
//        intent.putExtra("photo", contactItem.photoURI)
//        intent.putExtra("color", contactItem.drawable)
//        Log.d(TAG, "onContactItemClicked: ${contactItem.photoURI}")
//        val pairList = ArrayList<android.util.Pair<View, String>>()
////        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
//        var pair:android.util.Pair<View, String>? = null
//        if(contactItem.photoURI.isEmpty()){
//            pair = android.util.Pair(binding.textViewcontactCrclr as View, "firstLetterTransition")
//        }else{
//            pair = android.util.Pair(binding.imgViewCntct as View,"contactImageTransition")
//        }
//        pairList.add(pair)
//
//        val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0])
//        startActivity(intent, options.toBundle())
//    }

    private fun initListeners() {
        binding.btnGivecontactPermission.setOnClickListener(this)
        binding.searchViewContacts.setOnClickListener(this)
        binding.imgBtnHamBergerCntct.setOnClickListener(this)
        binding.fabBtn.setOnClickListener(this)

    }


    private fun observerContactList() {
        try {
            contactViewModel.contacts?.observe(viewLifecycleOwner, Observer{contacts->
                contacts.let {
//                    this.contactsView.pgBarCntcList.visibility = View.GONE
                    contactsRecyclerAdapter?.setContactList(it)
                    contactViewModel.startWorker(context?.applicationContext)
                    ContactGlobalHelper.size = contacts.size // setting the size in ContactsGlobalHelper
                }
            })
        }catch (e:Exception){
            Log.d(TAG, "observerContactList: exception $e")
        }

    }
    private fun startSearchActivity() {

        val intent = Intent(activity, ActivitySerchContacts::class.java)
        intent.putExtra("animation", "explode")
        Log.d(TAG, "startSearchActivity: $btnSampleTransition")
        val p1 = android.util.Pair(binding.searchViewContacts as View,"editTextTransition")

        val options = ActivityOptions.makeSceneTransitionAnimation(activity,p1 )
        startActivity(intent, options.toBundle())


    }
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: searchview")
        when(v?.id){
            R.id.btnGivecontactPermission -> {
                requestCntcPermissions()
                Log.d(TAG, "onClick: request permission")
//                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())

            }
            R.id.fabBtn -> {
                Log.d(TAG, "onClick: delete")
                contactViewModel.delteContactsInformation()
            }
            R.id.searchViewContacts->{
                startSearchActivity()
            }
            R.id.imgBtnHamBergerCntct ->{
                (activity as MainActivity).showDrawer()
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

    @AfterPermissionGranted(REQUEST_CODE_READ_CONTACTS)
    private fun requestCntcPermissions() {
        Log.d(TAG, "methodRequiresTwoPermission: ")
        if (EasyPermissions.hasPermissions(context, Manifest.permission.READ_CALL_LOG)) {
            // Already have permission, do the thing
            Log.d(TAG, "methodRequiresTwoPermission: already permission")
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                host = this,
                "read contacts ",
                requestCode = REQUEST_CODE_READ_CONTACTS,
                perms = arrayOf(
                    Manifest.permission.READ_CONTACTS
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContactsContainerFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactsContainerFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }
    override fun onResume() {
        super.onResume()
//        checkContactPermission()

    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}