package com.nibble.hashcaller.view.ui.sms.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.nibble.hashcaller.databinding.ActivitySearchBinding
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.QUERY_STRING
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*

class SearchSMSActivity : AppCompatActivity(), ITextChangeListener, SMSSearchAdapter.LongPressHandler,
    SMSListAdapter.NetworkHandler {
    private lateinit var editTextListener: TextChangeListener
    private lateinit var viewmodel:SMSSearchViewModel
    private  var searchAdapter: SMSSearchAdapter? = null
//    private lateinit var recyclerV: RecyclerView
    private var queryText = ""
    private var contactAddress:String? = ""
    private var isIntentFromIndividualSMS = false
    private var isInternetAvailable = false

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactAddress = intent.getStringExtra(CONTACT_ADDRES) // this intent extra is received when
                                                            //intented from individualsmsactivity
        Log.d(TAG, "onCreate: contactaddresvia intent $contactAddress")
        if(!contactAddress.isNullOrEmpty()){
            isIntentFromIndividualSMS = true
        }
        initRecyclerView()
        initListeners()
        initViewModel()
        observeInternetLivedata()
        observeSearchResult()

        if(!isIntentFromIndividualSMS)
         getSearchHistory()
    }

    private fun observeSearchResult() {
        viewmodel.searchResultLivedata.observe(this, Observer {
            if(it.searchTerm == binding.searchVSms.text.toString()){
                    binding.progressBar.beInvisible()

                this.searchAdapter!!.setList(it.searchResult)

            }
        })
    }

    private fun observeInternetLivedata() {
        val cl = this?.let { ConnectionLiveData(it) }
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
    }
    private fun getSearchHistory() {
        this.viewmodel.getAllSearchHistory().observe(this, Observer {
            Log.d(TAG, "getSearchHistory: size ${it.size}")
        })
    }

    private fun initRecyclerView() {


        this@SearchSMSActivity.searchAdapter = SMSSearchAdapter(this, this@SearchSMSActivity, this)
        { view: View, threadId:Long, pos:Int,
          pno:String, id:Long?->onContactItemClicked(view,threadId, pos, pno,id )  }

        binding.reclrSmsSearchResult.layoutManager = LinearLayoutManager(this@SearchSMSActivity)
        binding.reclrSmsSearchResult.adapter = this@SearchSMSActivity.searchAdapter
    }

    private fun onContactItemClicked(view: View, threadId: Long, pos: Int, pno: String, id:Long?) {
        saveSearchQueryToLocalDB()
        val intent = Intent(this, IndividualSMSActivity::class.java )
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        var bundle = Bundle()
        Log.d(TAG, "onContactItemClicked: chatId is $id")
        bundle.putString(CONTACT_ADDRES, pno)
        bundle.putString(SMS_CHAT_ID, id.toString())
        bundle.putString(QUERY_STRING,queryText)

        intent.putExtras(bundle)

        startActivity(intent)
    }

    private fun saveSearchQueryToLocalDB() {
        this.viewmodel.saveSearchQueryToDB(queryText)
    }

    override fun onLongPressed(view: View, pos: Int, id: Long, address: String) {

    }


    private fun initViewModel() {
        viewmodel = ViewModelProvider(this, SmsSearchInjectorUtil.provideDialerViewModelFactory(
            applicationContext,
            TokenHelper( FirebaseAuth.getInstance().currentUser)
            )).get(
            SMSSearchViewModel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListener(this)
        editTextListener.addListener( binding.searchVSms)

    }

    override fun onTextChanged(text: String) {
        if(text.isNullOrEmpty()){
            binding.progressBar.beInvisible()
            val lst : List<SMS> = emptyList()
            this.searchAdapter!!.setList(lst) //if search query is empty empty recyclerview

        }
        if(isIntentFromIndividualSMS){
            //searching for individual sms
//            viewmodel.searchForIndividualSMS(text, contactAddress).observe(this, Observer {
//                Log.d(TAG, "onSearchIndividualSMS: $it")
//            })
        }else{
            //get sms of all chats
            if(text.isNullOrEmpty()){
                val lst : List<SMS> = emptyList()
                this.searchAdapter!!.setList(lst) //if search query is empty empty recyclerview

            }else{
                //todo if the search query is a name like amma, ie 9512313
                //only number is existing in sms content provider in this case, no name, so I need to consider that
                val lst : List<SMS> = emptyList()
                this.searchAdapter!!.setList(lst)
                binding.progressBar.beVisible()
                viewmodel.search(text)


                queryText = text

//                    .observe(this, Observer {
//                    it.let {
//                        this.searchAdapter!!.setList(it!!) //set search result to recyclerview
//                        queryText = text
//                    }
//
//                })
            }
        }

    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: $s")
    }
    companion object{
        const val TAG = "__SearchActivity"
    }

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable
    }


}