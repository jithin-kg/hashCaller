package com.nibble.hashcaller.view.ui.call.search

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityCallLogSearchBinding
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.makeCall
import com.nibble.hashcaller.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.nibble.hashcaller.view.ui.sms.individual.util.UNMARK_ITEM
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.ITextChangeListener
import com.nibble.hashcaller.view.ui.sms.util.TextChangeListener

class CallLogSearchActivity : AppCompatActivity(), CallSearchAdapter.ViewMarkHandler,
    ITextChangeListener, View.OnClickListener, DialerAdapter.ViewMarkHandler {
    private lateinit var binding: ActivityCallLogSearchBinding
    private lateinit var searchAdapter : DialerAdapter
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
                binding.tvQueryItem.beVisible()
            }else{
                binding.tvQueryItem.beGone()
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
        binding.imgBtnBackCallhistory.setOnClickListener(this)

    }
    private fun initRecyclerView() {


        searchAdapter = DialerAdapter(this,this) {

                id:Long, position:Int, view:View, btn:Int, callLog: CallLogTable, clickType:Int ->onCallItemClicked(id, position, view, btn, callLog,clickType)};

            binding.reclrVResultFull.layoutManager = LinearLayoutManager(this)

        binding.reclrVResultFull.layoutManager = CustomLinearLayoutManager(this)
        binding.reclrVResultFull.adapter = this.searchAdapter
    }

    private fun onCallItemClicked(id: Long, position: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int): Int {
        when(clickType) {
            TYPE_MAKE_CALL ->{
                makeCall(callLog.number)
            } else ->{
            startIndividualContactActivity(callLog, view)
        }
        }


        return UNMARK_ITEM
    }

    private fun startIndividualContactActivity(log: CallLogTable, view: View) {
        var name = log.name
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.number
        }
        val intent = Intent(this, IndividualCotactViewActivity::class.java )
        intent.putExtra(com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name )
        intent.putExtra("photo", "")
        intent.putExtra("color", log.color)

        val pairList = ArrayList<android.util.Pair<View, String>>()
        val imgViewUserPhoto = view.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.imgViewUserPhoto)
        val textViewCrclr = view.findViewById<TextView>(R.id.textViewCrclr)

//                val p1 = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
        val p2 = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
//                pairList.add(p1)
        pairList.add(p2)
        val options = ActivityOptions.makeSceneTransitionAnimation(this,pairList[0] )
        options.toBundle()
        startActivity(intent, options.toBundle())
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
            foundResultFor = text
            queryText = text
            viewmodel.search(text)
        }
        binding.tvQueryItem.text = foundResultFor

    }

    override fun afterTextChanged(s: Editable) {

    }

    override fun onClick(v: View?) {
        when(v?.id){

            R.id.imgBtnBackCallhistory ->{
                finish()
            }

        }
    }
companion object{
    const val TAG ="__CallLogSearchActivity"
}

}