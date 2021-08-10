package com.hashcaller.view.ui.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.R
import com.hashcaller.databinding.ContactListBinding
import com.hashcaller.databinding.FragmentContactsContainerBinding
import com.hashcaller.stubs.Contact
import com.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_CONTACTS
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.utils.extensions.startSearchActivity
import com.hashcaller.view.ui.MainActivity
import com.hashcaller.view.ui.MainActivityInjectorUtil
import com.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.hashcaller.view.ui.contacts.utils.ContactGlobalHelper
import com.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.hashcaller.view.ui.sms.individual.util.beGone
import com.hashcaller.view.ui.sms.individual.util.beInvisible
import com.hashcaller.view.ui.sms.individual.util.beVisible
import com.hashcaller.view.utils.IDefaultFragmentSelection
import com.hashcaller.view.utils.TopSpacingItemDecoration
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted

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

    private lateinit var contactViewModel: ContactsViewModel
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
        return binding.root
    }


    private fun observeUserInfo() {
//        sharedUserInfoViewmodel.userInfoLivedata.observe(viewLifecycleOwner, Observer {
//            if (it != null) {
////                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
////                binding.tvCntctPermissionInfo.text = fLetter
//            }
//        })
    }

    fun getData() {
        showRecyclerView()
        lifecycleScope.launchWhenStarted {
//            delay(2000L)
            initViewmodel()
            observerContactList()
            observeUserInfo()

        }
    }

    private fun showRecyclerView() {
        binding.btnGivecontactPermission.beInvisible()
        binding.rcrViewContactsList.beVisible()
    }

    private fun hideRecyevlerView() {
        binding.rcrViewContactsList.beInvisible()
        binding.btnGivecontactPermission.beVisible()
    }


    private fun initViewmodel() {
        contactViewModel = ViewModelProvider(
            this, ContacInjectorUtil.provideContactsViewModelFactory(
                context,
                lifecycleScope,
                TokenHelper(FirebaseAuth.getInstance().currentUser)
            )
        ).get(ContactsViewModel::class.java)
        sharedUserInfoViewmodel = ViewModelProvider(
            this, MainActivityInjectorUtil.provideUserInjectorUtil(
                context?.applicationContext!!,
                tokenHelper
            )
        ).get(
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
            contactsRecyclerAdapter =
                ContactAdapter(context) { binding: ContactListBinding, contact: Contact ->
                    context.onContactItemClicked(
                        binding,
                        contact,
                        activity
                    )
                }
            adapter = contactsRecyclerAdapter

        }

    }
//     fun onContactItemClicked(binding: ContactListBinding, contactItem: com.hashcaller.network.user.Contact){
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
//        binding.searchViewContacts.setOnClickListener(this)
        binding.imgBtnSearch.setOnClickListener(this)
        binding.imgBtnHamBergerCntct.setOnClickListener(this)
        binding.fabBtn.setOnClickListener(this)
        binding.tvContacts.setOnClickListener(this)

    }


    private fun observerContactList() {
        try {
            contactViewModel.contacts?.observe(viewLifecycleOwner, Observer { contacts ->
                contacts.let {
//                    this.contactsView.pgBarCntcList.visibility = View.GONE
                    binding.pgBarContacts.beGone()
                    contactsRecyclerAdapter?.setContactList(it)
                    contactViewModel.startWorker(context?.applicationContext)
                    ContactGlobalHelper.size =
                        contacts.size // setting the size in ContactsGlobalHelper
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "observerContactList: exception $e")
        }

    }

    private fun startSearchActivity() {

//        val intent = Intent(activity, ActivitySerchContacts::class.java)
//        intent.putExtra("animation", "explode")
//        Log.d(TAG, "startSearchActivity: $btnSampleTransition")
//        val p1 = android.util.Pair(binding.searchViewContacts as View, "editTextTransition")
//
//        val options = ActivityOptions.makeSceneTransitionAnimation(activity, p1)
//        startActivity(intent, options.toBundle())
        activity?.startSearchActivity()


    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: searchview")
        when (v?.id) {
            R.id.btnGivecontactPermission -> {
                requestCntcPermissions()
                Log.d(TAG, "onClick: request permission")
//                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())

            }
            R.id.fabBtn -> {
                Log.d(TAG, "onClick: delete")
                contactViewModel.delteContactsInformation()
               context?. startActivityIncommingCallView(
                   "+9180861762224",
                   "Missed Call",
                   -1
               )
            }
            R.id.imgBtnSearch -> {
                startSearchActivity()
            }
            R.id.imgBtnHamBergerCntct -> {
                (activity as MainActivity).showDrawer()
            }

            else -> {

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

        initListeners()
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper = TokenHelper(user)

//        ViewCompat.setTransitionName(
//            binding.searchViewContacts,
//            binding.searchViewContacts.transitionName
//        )
//        val contextThemeWrapper: Context =
//            ContextThemeWrapper(activity, R.style.Theme_MyDarkTheme)



//        binding.searchViewContacts.onFocusChangeListener =
//            View.OnFocusChangeListener { view, hasFocus ->
//
//                if (hasFocus) {
////                if((activity as MainActivity).searchFragment!=null)
////                    startSearchActivity()
//                }
//            }
//        binding.searchViewContacts.setOnClickListener(this)

    }

    @SuppressLint(  "HardwareIds")
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && !this::contactViewModel.isInitialized){
            if (context?.hasReadContactsPermission() == true) {

//               if (ActivityCompat.checkSelfPermission(
//                       requireContext(),
//                       Manifest.permission.READ_SMS
//                   ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                       requireContext(),
//                       Manifest.permission.READ_PHONE_NUMBERS
//                   ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                       requireContext(),
//                       Manifest.permission.READ_PHONE_STATE
//                   ) != PackageManager.PERMISSION_GRANTED
//               ) {
//                   val request = PermissionRequest.Builder(this.context)
//                       .code(PermisssionRequestCodes.REQUEST_CODE_CALL_LOG)
//                       .perms(arrayOf(
//                           Manifest.permission.READ_PHONE_NUMBERS,
//                       ))
//                       .rationale("HashCaller needs access to call logs to identify unknown callers in call log.")
//                       .positiveButtonText("Continue")
//                       .negativeButtonText("Cancel")
//                       .build()
//                  EasyPermissions.requestPermissions(requireActivity(), request)
//                   return
//               }else {
//                  val telManager =  (context?.getSystemService(Context.TELEPHONY_SERVICE)) as TelephonyManager
////                   (context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number
//                   Log.d(TAG, "onHiddenChanged:line1  ${ telManager.line1Number}")
//
//
//               }
                initRecyclerView()
                getData()




            } else {
                hideRecyevlerView()
            }
        }
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

    override fun onPause() {
        super.onPause()
        }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}
}