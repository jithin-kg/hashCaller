package com.nibble.hashcaller.view.ui.search

import android.content.DialogInterface
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
import com.nibble.hashcaller.databinding.SearchFilterAlertCheckBoxBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.SHOW_SMS_IN_SEARCH_RESULT
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.*
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
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

    private var queryStr = ""
    var contactsRecyclerAdapter: ContactAdapter? = null
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



    private fun getUserPreferences() {

        dataStoreViewmodel.searchFilterLiveData.asLiveData().observe(this, Observer { isshowSMSResult ->
          if(isshowSMSResult)  {
              binding.recyclerViewSMS.beVisible()
          }else {
              binding.recyclerViewSMS.beGone()
          }
        })
    }


    private fun observeSMSList() {
        searchViewmodel.smsListOfLivedata.observe(this, Observer {
            this.smsAdapter?.setList(it)
        })
    }

    private fun observeContactsList() {
        searchViewmodel.contactsListOfLivedata.observe(this, Observer {
            contactsRecyclerAdapter?.setContactList(it)
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
        binding.tvQueryItem.text = ""
            queryStr = newText
//            this.searchViewmodel.searc
            if(queryStr.isNotEmpty()){
                searchViewmodel.onQueryTextChanged(newText.toLowerCase())
            }else {
                searchViewmodel.emptyAllLists()
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
//                addItemDecoration(topSpacingDecorator)
            contactsRecyclerAdapter = ContactAdapter(context) { binding: ContactListBinding, contact: Contact ->onContactItemClicked(
                binding,
                contact,
                this@SearchActivity
            )}
            adapter = contactsRecyclerAdapter
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