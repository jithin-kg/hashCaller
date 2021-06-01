package com.nibble.hashcaller.view.ui.search

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySearchMainBinding
import com.nibble.hashcaller.databinding.ContactListBinding
import com.nibble.hashcaller.databinding.ContactSearchResultItemBinding
import com.nibble.hashcaller.databinding.SearchFilterAlertCheckBoxBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.SHOW_SMS_IN_SEARCH_RESULT
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.*
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.search.SMSSearchAdapter
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.layout_blog_list_item.*


class SearchActivity : AppCompatActivity(), ITextChangeListener, SMSSearchAdapter.LongPressHandler,
    SMSListAdapter.NetworkHandler, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var binding:ActivitySearchMainBinding
    private lateinit  var searchViewmodel: AllSearchViewmodel
    private lateinit var editTextListener: TextChangeListener
    private lateinit var alertBuilder: AlertDialog.Builder
    private var showSMSEnabled = false
    private var isFoundInSMS = false


    private var queryStr = ""
    var contactsRecyclerAdapter: DialerAdapter? = null
    private  var smsAdapter: SMSSearchAdapter? = null
    private lateinit var searchFilterView:SearchFilterAlertCheckBoxBinding
    private lateinit var checkBoxIncludeSMS:CheckBox
    private lateinit var dataStoreViewmodel: DataStoreViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initAlertView()
        initListeners()
        initContactsRecyclerView()
        initSMSRecyclerView()
        initViewmodel()
        getUserPreferences()
        initAllLists()
        observeContactsList()
        observeSMSList()


    }






    private fun observeSMSList() {
        searchViewmodel.smsListOfLivedata.observe(this, Observer {
            this.smsAdapter?.setList(it)
            if (showSMSEnabled && it.size > 0) {
                isFoundInSMS = true
                binding.recyclerViewSMS.beVisible()
                binding.tvSMS.beVisible()
                binding.tvNotInSMS.beInvisible()
            } else {
                isFoundInSMS = false
                binding.recyclerViewSMS.beInvisible()
                binding.tvSMS.beVisible()
                binding.tvNotInSMS.beVisible()
            }
        })
    }

    private fun observeContactsList() {
        searchViewmodel.contactsListOfLivedata.observe(this, Observer {
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
            if(isshowSMSResult && isFoundInSMS)  {
                binding.recyclerViewSMS.beVisible()
                binding.tvSMS.beVisible()
            }else {
                binding.recyclerViewSMS.beInvisible()
                binding.tvSMS.beInvisible()

            }
        })
    }

    private fun initAllLists() {
        searchViewmodel.initAllLists()
    }

    private fun initListeners() {

        editTextListener = TextChangeListener(this)
        editTextListener.addListener(binding.searchVCallSearch)
        binding.imgBtnSearchFilter.setOnClickListener(this)
        searchFilterView.checkboxIncludeSMS.setOnCheckedChangeListener(this)

    }
    private fun initViews() {

//        searchFilterView = View.inflate(this, R.layout.search_filter_alert_check_box, null)
        searchFilterView =SearchFilterAlertCheckBoxBinding.inflate(layoutInflater, null, false)
//        checkBoxIncludeSMS = searchFilterView.findViewById(R.id.checkboxIncludeSMS)
    }
    private fun initViewmodel() {
        this.searchViewmodel = ViewModelProvider(
            this, AllSearchInjectorUtil.provideViewModelFactory(
                getAllSMSCursor(),
                getAllContactsCursor(),
                getAllCallLogsCursor()
            )
        ).get(AllSearchViewmodel::class.java)

        dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(this))
            .get(DataStoreViewmodel::class.java)

    }



    private fun createSMSCursor(): Cursor? {
        var smsCursor: Cursor? = null
            smsCursor = getAllSMSCursor()
        return smsCursor
    }

    override fun onTextChanged(newText: String) {
//        binding.tvQueryItem.text = ""
            queryStr = newText
//            this.searchViewmodel.searc
            if(queryStr.isNotEmpty()){
                searchViewmodel.onQueryTextChanged(newText.toLowerCase())
                binding.linearLayoutSearch.beVisible()
            }else {
                searchViewmodel.emptyAllLists()
                binding.linearLayoutSearch.beGone()
            }
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

    private fun onContactItemClicked(
        binding: ContactSearchResultItemBinding,
        contactItem: Contact,
        clickType: Int
    ){
        when(clickType){
            TYPE_MAKE_CALL ->{
                makeCall(contactItem.phoneNumber)
            }
            else ->{
                Log.d(TAG, "onContactItemClicked: ${contactItem.phoneNumber}")
                val intent = Intent(this, IndividualContactViewActivity::class.java )
                intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
                intent.putExtra("name", contactItem.name )
//        intent.putExtra("id", contactItem.id)
                intent.putExtra("photo", contactItem.photoURI)
                intent.putExtra("color", contactItem.drawable)
                Log.d(TAG, "onContactItemClicked: ${contactItem.photoURI}")
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
        }
    }
     private fun initAlertView() {
         alertBuilder = AlertDialog.Builder(this)
         alertBuilder.setTitle("Search filter")
              //        builder.setMessage(" MY_TEXT ")
                     .setView(searchFilterView.root)
                    .setCancelable(true)
//                    .setPositiveButton("", DialogInterface.OnClickListener { dialog, id ->
//                         Log.d(TAG, "showSearchFilterAlert: onyes clicked")
//                     })

           .setNegativeButton("Cancel",
             DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
    }
    private fun showSearchFilterAlert() {
        if(searchFilterView.root.parent!=null) {
               (searchFilterView.root.parent as ViewGroup).removeView(searchFilterView.root)
        }
        alertBuilder.setView(searchFilterView.root)
        alertBuilder.show()
    }


    companion object {
        const val TAG = "__SearchActivity"
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        Log.d(TAG, "onCheckedChanged: ${buttonView?.id}")
        when(buttonView?.id){
            R.id.checkboxIncludeSMS -> {
                updateSearchPreferences(isChecked)
            }
        }
    }

    private fun updateSearchPreferences(checked: Boolean) {
        dataStoreViewmodel.setBoolean(SHOW_SMS_IN_SEARCH_RESULT, checked)
    }
}