package com.nibble.hashcaller.view.ui.call.search

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
import com.nibble.hashcaller.databinding.ActivityCallLogSearchBinding
import com.nibble.hashcaller.databinding.ActivityMainBinding
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.contacts.startSettingsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.search.SMSSearchAdapter
import com.nibble.hashcaller.view.ui.sms.search.SMSSearchViewModel
import com.nibble.hashcaller.view.ui.sms.search.SearchSMSActivity
import com.nibble.hashcaller.view.ui.sms.search.SmsSearchInjectorUtil
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener
import kotlinx.android.synthetic.main.activity_search.*

class CallLogSearchActivity : AppCompatActivity(), CallSearchAdapter.ViewMarkHandler,
    ITextChangeListener, View.OnClickListener {
    private lateinit var binding: ActivityCallLogSearchBinding
    private lateinit var searchAdapter : CallSearchAdapter
    private lateinit var editTextListener: TextChangeListener
    private var queryText = ""
    private lateinit var viewmodel: CallLogSearchViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallLogSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initListeners()
        initViewModel()


    }

    private fun initViewModel() {
        viewmodel = ViewModelProvider(this, CalllogSearchInjectorUtil.provideDialerViewModelFactory(this)).get(
            CallLogSearchViewModel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListener(this)
        editTextListener.addListener(binding.searchVCallSearch)
        binding.searchVCallSearch.setOnClickListener(this)

    }
    private fun initRecyclerView() {


        searchAdapter = CallSearchAdapter(this,this) {

                id:Long, position:Int, view:View, btn:Int, callLog: CallLogTable, clickType:Int ->onCallItemClicked(id, position, view, btn, callLog,clickType)};

            binding.reclrVResultFull.layoutManager = LinearLayoutManager(this)

        binding.reclrVResultFull.layoutManager = LinearLayoutManager(this)
        binding.reclrVResultFull.adapter = this.searchAdapter
    }

    private fun onCallItemClicked(id: Long, position: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int): Int {
        return 0
    }

    override fun isMarked(id: Long?): Boolean {
        return false
    }

    override fun onTextChanged(text: String) {
        if(text.isNullOrEmpty()){
            val lst : MutableList<CallLogTable> = mutableListOf()
//            this.searchAdapter!!.submitCallLogs(lst) //if search query is empty empty recyclerview

        }else{
            //todo if the search query is a name like amma, ie 9512313
            //only number is existing in sms content provider in this case, no name, so I need to consider that
//            binding.shimmer.beVisible()
//            binding.shimmer.startShimmer()
            viewmodel.search(text).observe(this, Observer {
//                binding.shimmer.beInvisible()
                Log.d(TAG, "onTextChanged: size ${it.size}")

                this.searchAdapter!!.submitCallLogs(it) //set search result to recyclerview
//                binding.shimmer.stopShimmer()
                queryText = text
            })
        }
    }

    override fun afterTextChanged(s: Editable) {

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id. searchVCallSearch ->{
            }

        }
    }
companion object{
    const val TAG ="__CallLogSearchActivity"
}

}