package com.hashcaller.app.view.ui.search

import android.Manifest
import android.app.ActivityOptions
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivitySearchMainBinding
import com.hashcaller.app.databinding.ContactSearchResultItemBinding
import com.hashcaller.app.databinding.SearchFilterAlertCheckBoxBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SHOW_SMS_IN_SEARCH_RESULT
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CONTACTS
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.internet.CheckNetwork
import com.hashcaller.app.view.ui.call.dialer.DialerAdapter
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.contacts.*
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.ui.sms.search.SMSSearchAdapter
import com.hashcaller.app.view.ui.sms.util.ITextChangeListenerDelayed
import com.hashcaller.app.view.ui.sms.util.TextChangeListenerDelayed
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.TopSpacingItemDecoration
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext


class SearchActivity : AppCompatActivity(), ITextChangeListenerDelayed, SMSSearchAdapter.LongPressHandler,
    SMSListAdapter.NetworkHandler, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var binding:ActivitySearchMainBinding
    private lateinit  var searchViewmodel: AllSearchViewmodel
    private lateinit var editTextListener: TextChangeListenerDelayed
    private lateinit var alertBuilder: AlertDialog.Builder
    private var showSMSEnabled = false
    private var isFoundInSMS = false
    private lateinit var internetChecker:CheckNetwork
    private lateinit var countryCodeHelper: CountrycodeHelper
    private var countryCodeIso:String = ""


    private var queryStr = ""
    var contactsRecyclerAdapter: DialerAdapter? = null
    var serverSearchResultAdapter: ServerSearchResultAdapter? = null
    private  var smsAdapter: SMSSearchAdapter? = null
    private lateinit var searchFilterViewBinding:SearchFilterAlertCheckBoxBinding
    private lateinit var checkBoxIncludeSMS:CheckBox
    private lateinit var dataStoreViewmodel: DataStoreViewmodel
    private lateinit var serverSearchViewmodel: ServerSearchViewModel
    private var searchJob:Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initMembers()
        initAlertView()
        initListeners()
        initContactsRecyclerView()
        initServerResultAdapter()
        initSMSRecyclerView()
        initViewmodel()
        getUserPreferences()
        initAllLists()
        observeContactsList()
        observeSMSList()
        observeSererSearchResult()
        getDefaultContry()



    }

    private fun getDefaultContry() {
        searchViewmodel.getDefaultCountry(countryCodeHelper).observe(this, Observer {
            countryCodeIso = it
//            if(countryCodeIso.isNullOrEmpty()) {
//                countryCodeIso = "IN"
//            }
            runOnUiThread {
                binding.coutryCodePicker.setDefaultCountryUsingNameCode(countryCodeIso)
            }

        })
    }

    private fun initMembers() {
        internetChecker = CheckNetwork(this)
        internetChecker.registerNetworkCallback()
        countryCodeHelper = CountrycodeHelper(this)



    }

    private fun observeSererSearchResult() {
        serverSearchViewmodel.serverSearchResultLiveData.observe(this, Observer {
            if(it!=null){
                Log.d(TAG, "observeSererSearchResult: $it")
                serverSearchResultAdapter?.setList(it)
                if(it.isNotEmpty()){
                    binding.tvNoResultshashCaller.beGone()
                }else {
                    binding.tvNoResultshashCaller.beVisible()
                    binding.tvNoResultshashCaller.text = getString(R.string.no_results)
                }
            }
        })
    }


    private fun observeSMSList() {
        searchViewmodel.smsListOfLivedata.observe(this, Observer {
            this.smsAdapter?.setList(it)
            isFoundInSMS = showSMSEnabled && it.size > 0
        })
    }

    private fun observeContactsList() {
        searchViewmodel.contactsSearchListLivedata.observe(this, Observer {
            contactsRecyclerAdapter?.setList(it)
            if(it.isNotEmpty()) {
                  binding.recyclerViewContacts.beVisible()
                  binding.tvContacts.beVisible()
                  binding.tvNotContacts.beInvisible()
            }else {
                binding.recyclerViewContacts.beInvisible()
                binding.tvContacts.beVisible()
                binding.tvNotContacts.beVisible()
            }

        })
    }


    private fun getUserPreferences() {

        dataStoreViewmodel.searchFilterLiveData.asLiveData().observe(this, Observer { isshowSMSResult ->
            showSMSEnabled = isshowSMSResult
//            searchFilterViewBinding.checkboxIncludeSMS.isChecked = isshowSMSResult
            if(isshowSMSResult && isFoundInSMS)  {
//                binding.recyclerViewSMS.beVisible()
//                binding.tvSMS.beVisible()
            }else {
//                binding.recyclerViewSMS.beInvisible()
//                binding.tvSMS.beInvisible()

            }
        })
    }

    private fun initAllLists() {
        searchViewmodel.initAllLists()
    }

    private fun initListeners() {

        editTextListener = TextChangeListenerDelayed(this)
        editTextListener.addListener(binding.searchVCallSearch)
        binding.imgBtnSearchFilter.setOnClickListener(this)
        searchFilterViewBinding.checkboxIncludeSMS.setOnCheckedChangeListener(this)
        binding.imgBtnBackCallhistory.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)


    }
    private fun initViews() {

//        searchFilterView = View.inflate(this, R.layout.search_filter_alert_check_box, null)
        searchFilterViewBinding =SearchFilterAlertCheckBoxBinding.inflate(layoutInflater, null, false)
//        checkBoxIncludeSMS = searchFilterView.findViewById(R.id.checkboxIncludeSMS)
    }
    private fun initViewmodel() {
        //todo pass only context, then create cursor from that funnction, because cursor is always closer after each function call
        this.searchViewmodel = ViewModelProvider(
            this, AllSearchInjectorUtil.provideViewModelFactory(
                this)
        ).get(AllSearchViewmodel::class.java)

        dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(this))
            .get(DataStoreViewmodel::class.java)

        serverSearchViewmodel = ViewModelProvider(this, ServerSearchInjectorUtil.provideViewModelFactory(
            FirebaseAuth.getInstance().currentUser, this))
            .get(ServerSearchViewModel::class.java)

    }



    private fun createSMSCursor(): Cursor? {
        var smsCursor: Cursor? = null
            smsCursor = getAllSMSCursor()
        return smsCursor
    }

    override fun onTextChanged(newText: String) {
//        binding.tvQueryItem.text = ""
//        runOnUiThread {
            if(!CheckNetwork.isetworkConnected()){
                toast("No internet")
            }else {
                binding.tvNoResultshashCaller.text = "Searching..."
            }
            queryStr = newText
//            this.searchViewmodel.searc
            if(queryStr.isNotEmpty()){
                //we have different layout for server search and local search
                searchViewmodel.onQueryTextChanged(newText.toLowerCase(), false)
                binding.linearLayoutSearch.beVisible()
                //this is important to cancel job, or request will be made frequently
//                searchViewmodel.cancelPrevJob(searchJob).observe(this, Observer {

                searchJob = serverSearchViewmodel.searchInServer(
                    newText,
                    packageName,
                    binding.coutryCodePicker.selectedCountryCode,
                    binding.coutryCodePicker.selectedCountryNameCode)
//                })

            }else {
                searchViewmodel.emptyAllLists()
                serverSearchViewmodel.clearResult()
                binding.linearLayoutSearch.beGone()
            }
//        }

    }

    override fun afterTextChanged(s: Editable) {

    }
    private fun initContactsRecyclerView() {

        binding.recyclerViewContacts?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            contactsRecyclerAdapter = DialerAdapter(context) { binding: ContactSearchResultItemBinding, contact: Contact, clickType:Int ->onContactItemClicked(binding, contact, clickType)}
            adapter = contactsRecyclerAdapter
            itemAnimator = null
        }


    }
    private fun initServerResultAdapter() {

        binding.recyclerViewServerResult?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            serverSearchResultAdapter = ServerSearchResultAdapter(context) { binding: ContactSearchResultItemBinding, contact: Contact, clickType:Int ->onContactItemClicked(binding, contact, clickType)}
            adapter = serverSearchResultAdapter
            itemAnimator = null
        }


    }

    private fun onContactItemClicked(
        binding: ContactSearchResultItemBinding,
        contactItem: Contact,
        clickType: Int
    ){
        when(clickType){
            TYPE_MAKE_CALL ->{
                if(EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)){
                    makeCall(contactItem.phoneNumber)
                }else {
                    requestCallPhonePermission()
                }


            }
            else ->{
                val intent = Intent(this, IndividualContactViewActivity::class.java )
                var nameOfContact = ""
                if(contactItem.hUid.isNotEmpty() && (contactItem.firstName.isNotEmpty() || contactItem.lastName.isNotEmpty())){
                    nameOfContact += contactItem.firstName
                    if(contactItem.lastName.isNotEmpty()){
                        nameOfContact += " "+ contactItem.lastName
                    }
                }else if(contactItem.nameInLocalPhoneBook.isNotEmpty()){
                    nameOfContact = contactItem.nameInLocalPhoneBook
                }else if(contactItem.nameInPhoneBook.isNotEmpty()){
                    nameOfContact = contactItem.nameInLocalPhoneBook
                }else {
                    nameOfContact = contactItem.phoneNumber
                }
                intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
                intent.putExtra("name", nameOfContact)
//        intent.putExtra("id", contactItem.id)
                intent.putExtra("photo", contactItem.photoURI)
                intent.putExtra("color", contactItem.drawable)
                intent.putExtra(IntentKeys.INTENT_SOURCE, BLOCK_TYPE_FROM_CONTACTS)
                val pairList = ArrayList<android.util.Pair<View, String>>()
//        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
                var pair:android.util.Pair<View, String>? = null
                if(contactItem.photoURI.isEmpty()){
                    pair = android.util.Pair(binding.textViewcontactCrclr as View, "firstLetterTransition")
                }else{
                    pair = android.util.Pair(binding.imgViewCntct as View,"contactImageTransition")
                }
                pairList.add(pair)
                val options = ActivityOptions.makeSceneTransitionAnimation(this,pairList[0])
                startActivity(intent, options.toBundle())
            }
        }

    }

    private fun initSMSRecyclerView() {


        this.smsAdapter = SMSSearchAdapter(this, this, this)
        { view: View, threadId: Long, pos: Int,
          pno: String, id: Long?->onSMSclicked(view, threadId, pos, pno, id)  }

        binding.recyclerViewSMS.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSMS.adapter = this.smsAdapter
    }

    private fun onSMSclicked(view: View, threadId: Long, pos: Int, pno: String, id: Long?) {
        onSMSItemItemClicked(view, threadId, pos, pno, id, queryStr)
        //todo saveSearchQueryToDB(queryText)
    }

    override fun onLongPressed(view: View, pos: Int, id: Long, address: String) {

    }

    override fun isInternetAvailable(): Boolean {
        return false
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnSearchFilter -> {
             showSearchFilterAlert()
            }
            R.id.imgBtnBackCallhistory -> {
                finishAfterTransition()
            }
            R.id.btnClear -> {
                lifecycleScope.launchWhenCreated {
                    withContext(Dispatchers.IO){
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).callersInfoFromServerDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).spamListDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).blocklistDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).spamListDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).smsDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).contactInformationDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).contactLastSyncedDateDAO().delteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).mutedSendersDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).blockedOrSpamSendersDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).smsSearchQueriesDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).contactAddressesDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).callLogDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).smsThreadsDAO().deleteAll()
//                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).userHashedNumDAO().deleteAll()
                        HashCallerDatabase.getDatabaseInstance(this@SearchActivity).hashedContactsDAO().deleteAll()
                    }
                }
            }
        }
    }
     private fun initAlertView() {
         alertBuilder = AlertDialog.Builder(this)
         alertBuilder.setTitle("Search filter")
              //        builder.setMessage(" MY_TEXT ")
                     .setView(searchFilterViewBinding.root)
                    .setCancelable(true)
//                    .setPositiveButton("", DialogInterface.OnClickListener { dialog, id ->
//                         Log.d(TAG, "showSearchFilterAlert: onyes clicked")
//                     })

           .setNegativeButton("Cancel",
             DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
    }
    private fun showSearchFilterAlert() {
        if(searchFilterViewBinding.root.parent!=null) {
               (searchFilterViewBinding.root.parent as ViewGroup).removeView(searchFilterViewBinding.root)
        }
        alertBuilder.setView(searchFilterViewBinding.root)
        alertBuilder.show()

    }


    companion object {
        const val TAG = "__SearchActivity"
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.checkboxIncludeSMS -> {
                updateSearchPreferences(isChecked)
            }
        }
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }

    private fun updateSearchPreferences(checked: Boolean) {
        dataStoreViewmodel.setBoolean(SHOW_SMS_IN_SEARCH_RESULT, checked)
    }
}