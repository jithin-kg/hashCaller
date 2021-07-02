package com.nibble.hashcaller.view.ui.sms.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySearchBinding
import com.nibble.hashcaller.databinding.ContactSearchResultItemBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.HashCallerViewModel
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.onSMSItemItemClicked
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.search.AllSearchInjectorUtil
import com.nibble.hashcaller.view.ui.search.AllSearchViewmodel
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.util.*
import kotlinx.android.synthetic.main.activity_search.*

class SearchSMSActivity : AppCompatActivity(), SMSSearchAdapter.LongPressHandler,
    SMSListAdapter.NetworkHandler, ITextChangeListenerDelayed, View.OnClickListener {
    private lateinit var editTextListener: TextChangeListenerDelayed
//    private lateinit var viewmodel:SMSSearchViewModel
    private lateinit var allSearchViewmodel: AllSearchViewmodel
    private  var searchAdapter: DialerAdapter? = null
//    private lateinit var recyclerV: RecyclerView
    private var queryText = ""
    private var contactAddress:String? = ""
    private var isIntentFromIndividualSMS = false
    private var isInternetAvailable = false
    private var queryStr = ""

    private lateinit var binding: ActivitySearchBinding
    private lateinit var hashCallerViewModel: HashCallerViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contactAddress = intent.getStringExtra(CONTACT_ADDRES) // this intent extra is received when
                                                            //intented from individualsmsactivity
        if(!contactAddress.isNullOrEmpty()){
            isIntentFromIndividualSMS = true
        }
        initRecyclerView()
        initListeners()
        initHashCallerViewmodel()
        initViewModel()
        observeInternetLivedata()
        initContactsList()
        observeContactsList()

    }

    private fun initHashCallerViewmodel() {
        hashCallerViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(HashCallerViewModel::class.java)
        hashCallerViewModel.mapOfColorsForInt
        hashCallerViewModel.mapOfColorsForString
    }

    private fun observeContactsList() {
        allSearchViewmodel.contactsListOfLivedata.observe(this, Observer {
            searchAdapter?.setList(it)
            binding.pgBarSMSSearch.beGone()
            if(it.isNotEmpty()) {
                binding.reclrSmsSearchResult.beVisible()
                binding.tvNoResult.beGone()
            }else {
                binding.reclrSmsSearchResult.beInvisible()
                binding.tvNoResult.beVisible()
            }

        })
    }

    private fun initContactsList() {
        allSearchViewmodel.initContactsList()
    }
    override fun onTextChanged(newText: String) {
        runOnUiThread {
            queryStr = newText
            if(queryStr.isNotEmpty()) {
                binding.pgBarSMSSearch.beVisible()
                allSearchViewmodel.onQueryTextChanged(newText.toLowerCase(), true)
            }else {
                binding.pgBarSMSSearch.beInvisible()
                allSearchViewmodel.emptyAllLists()
            }
        }
    }

    override fun afterTextChanged(s: Editable) {
    }



    private fun observeInternetLivedata() {
        val cl = this?.let { ConnectionLiveData(it) }
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
    }

    private fun initRecyclerView() {


        this@SearchSMSActivity.searchAdapter = DialerAdapter(this) { binding: ContactSearchResultItemBinding, contact: Contact, clickType:Int ->onContactItemClicked(binding, contact, clickType)}

        binding.reclrSmsSearchResult.layoutManager = CustomLinearLayoutManager(this@SearchSMSActivity)
        binding.reclrSmsSearchResult.adapter = this@SearchSMSActivity.searchAdapter
    }

    private fun onContactItemClicked(
        binding: ContactSearchResultItemBinding,
        contactItem: Contact,
        clickType: Int
    ){


    }
    private fun onItemItemClicked(view: View, threadId: Long, pos: Int, pno: String, id:Long?) {
        onSMSItemItemClicked(view, threadId, pos, pno, id, queryText)
        saveSearchQueryToLocalDB()

    }

    private fun saveSearchQueryToLocalDB() {
//        this.viewmodel.saveSearchQueryToDB(queryText)
    }

    override fun onLongPressed(view: View, pos: Int, id: Long, address: String) {

    }


    private fun initViewModel() {
        allSearchViewmodel = ViewModelProvider(
            this, AllSearchInjectorUtil.provideViewModelFactory(
                this)
        ).get(AllSearchViewmodel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListenerDelayed(this)
        editTextListener.addListener( binding.edtTextSMSSearch)
        binding.imgBtnBack.setOnClickListener(this)

    }



    companion object{
        const val TAG = "__SearchSMSActivity"
    }

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnBack -> {
                finishAfterTransition()
            }
        }
    }


}