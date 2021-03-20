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
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.QUERY_STRING
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), ITextChangeListener, SMSSearchAdapter.LongPressHandler {
    private lateinit var editTextListener: TextChangeListener
    private lateinit var viewmodel:SMSSearchViewModel
    private  var searchAdapter: SMSSearchAdapter? = null
    private lateinit var recyclerV: RecyclerView
    private var queryText = ""
    private var contactAddress:String? = ""
    private var isIntentFromIndividualSMS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        contactAddress = intent.getStringExtra(CONTACT_ADDRES) // this intent extra is received when
                                                            //intented from individualsmsactivity
        Log.d(TAG, "onCreate: contactaddresvia intent $contactAddress")
        if(contactAddress!!.isNotEmpty()){
            isIntentFromIndividualSMS = true
        }
        initRecyclerView()
        initListeners()
        initViewModel()
        if(!isIntentFromIndividualSMS)
         getSearchHistory()
    }

    private fun getSearchHistory() {
        this.viewmodel.getAllSearchHistory().observe(this, Observer {
            Log.d(TAG, "getSearchHistory: size ${it.size}")
        })
    }

    private fun initRecyclerView() {

        this.recyclerV = findViewById<RecyclerView>(R.id.reclrSmsSearchResult)

        this@SearchActivity.searchAdapter = SMSSearchAdapter(this, this@SearchActivity)
        { view: View, threadId:Long, pos:Int,
          pno:String, id:Long?->onContactItemClicked(view,threadId, pos, pno,id )  }

        recyclerV.layoutManager = LinearLayoutManager(this@SearchActivity)
        recyclerV.adapter = this@SearchActivity.searchAdapter
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
        viewmodel = ViewModelProvider(this, SmsSearchInjectorUtil.provideDialerViewModelFactory(this)).get(
            SMSSearchViewModel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListener(this)
        editTextListener.addListener(searchVSms)

    }

    override fun onTextChanged(text: String) {
        if(isIntentFromIndividualSMS){
            //searching for individual sms
            viewmodel.searchForIndividualSMS(text)
        }else{
            //get sms of all chats
            if(text.isNullOrEmpty()){
                val lst : List<SMS> = emptyList()
                this.searchAdapter!!.setList(lst) //if search query is empty empty recyclerview

            }else{
                viewmodel.search(text).observe(this, Observer {
                    this.searchAdapter!!.setList(it) //set search result to recyclerview
                    queryText = text
                })
            }
        }

    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: $s")
    }
    companion object{
        const val TAG = "__SearchActivity"
    }


}