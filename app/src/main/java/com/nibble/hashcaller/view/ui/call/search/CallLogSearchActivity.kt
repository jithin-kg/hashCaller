package com.nibble.hashcaller.view.ui.call.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityCallLogSearchBinding
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener

class CallLogSearchActivity : AppCompatActivity(), CallSearchAdapter.ViewMarkHandler,
    ITextChangeListener, View.OnClickListener {
    private lateinit var binding: ActivityCallLogSearchBinding
    private lateinit var searchAdapter : CallSearchAdapter
    private lateinit var editTextListener: TextChangeListener
    private var queryText = ""
    private var foundResultFor = ""
    private lateinit var viewmodel: CallLogSearchViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallLogSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initListeners()
        initViewModel()
        observeCallLogsLivedata()


    }

    private fun observeCallLogsLivedata() {
        viewmodel.callLogs.observe(this, Observer {
            if(it.size> 0){
                foundResultFor = "\"$queryText\" in recent calls"
                binding.tvQueryItem.text = foundResultFor
            }
            searchAdapter.submitCallLogs(it)
        })
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

        binding.reclrVResultFull.layoutManager = CustomLinearLayoutManager(this)
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
//            val lst : List<CallLogTable> = mutableListOf()
            foundResultFor = ""
            binding.tvQueryItem.text = foundResultFor

//            this.searchAdapter!!.submitCallLogs(lst) //if search query is empty empty recyclerview

        }else{
            queryText = text
            viewmodel.search(text)
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