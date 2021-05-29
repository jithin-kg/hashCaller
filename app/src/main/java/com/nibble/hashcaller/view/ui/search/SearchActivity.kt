package com.nibble.hashcaller.view.ui.search

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.databinding.ActivitySearchMainBinding
import com.nibble.hashcaller.databinding.ContactListBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.*
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.search.SMSSearchAdapter
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration

class SearchActivity : AppCompatActivity(), ITextChangeListener, SMSSearchAdapter.LongPressHandler,
    SMSListAdapter.NetworkHandler {
    private lateinit var binding:ActivitySearchMainBinding
    private lateinit  var searchViewmodel: AllSearchViewmodel
    private lateinit var editTextListener: TextChangeListener
    private var queryStr = ""
    var contactsRecyclerAdapter: ContactAdapter? = null
    private  var smsAdapter: SMSSearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        initContactsRecyclerView()
        initSMSRecyclerView()
        initViewmodel()
        initAllLists()
        observeContactsList()
        observeSMSList()

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
    }
    private fun initViewmodel() {
        this.searchViewmodel = ViewModelProvider(
            this, AllSearchInjectorUtil.provideViewModelFactory(
                getAllSMSCursor(),
                getAllContactsCursor(),
                getAllCallLogsCursor()
                )).get(AllSearchViewmodel::class.java)
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
            contactsRecyclerAdapter = ContactAdapter(context) { binding: ContactListBinding, contact: Contact ->onContactItemClicked(binding, contact, this@SearchActivity)}
            adapter = contactsRecyclerAdapter
        }

    }

    private fun initSMSRecyclerView() {


        this.smsAdapter = SMSSearchAdapter(this, this, this)
        { view: View, threadId:Long, pos:Int,
          pno:String, id:Long?->onSMSclicked(view,threadId, pos, pno,id )  }

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
}