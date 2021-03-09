package com.nibble.hashcaller.view.ui.sms.search

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.list.SMSListInjectorUtil
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.fragment_messages_list.*

class SearchActivity : AppCompatActivity(), ITextChangeListener, SMSSearchAdapter.LongPressHandler {
    private lateinit var editTextListener: TextChangeListener
    private lateinit var viewmodel:SMSSearchViewModel
    private  var searchAdapter: SMSSearchAdapter? = null
    private lateinit var recyclerV: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initRecyclerView()
        initListeners()
        initViewModel()
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
        Log.d(TAG, "onContactItemClicked: ")
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
        viewmodel.search(text).observe(this, Observer {
            Log.d(TAG, "onTextChanged: $it")
            Log.d(TAG, "onTextChanged:size ${it.size}")
           this.searchAdapter!!.setList(it)
        })
    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged: $s")
    }
    companion object{
        const val TAG = "__SearchActivity"
    }


}