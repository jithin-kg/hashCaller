package com.hashcaller.app.view.ui.call.search

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityCallLogSearchBinding
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.dialer.CallLogAdapter
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.hashcaller.app.view.ui.sms.individual.util.UNMARK_ITEM
import com.hashcaller.app.view.ui.sms.individual.util.beGone
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.ui.sms.util.ITextChangeListener
import com.hashcaller.app.view.ui.sms.util.TextChangeListener
import com.vmadalin.easypermissions.EasyPermissions

class CallLogSearchActivity : AppCompatActivity(), CallSearchAdapter.ViewMarkHandler,
    ITextChangeListener, View.OnClickListener, CallLogAdapter.ViewHandlerHelper,
    SMSListAdapter.NetworkHandler {
    private lateinit var binding: ActivityCallLogSearchBinding
    private lateinit var searchAdapter : CallLogAdapter
    private lateinit var editTextListener: TextChangeListener
    private var queryText = ""
    private var foundResultFor = ""
    private lateinit var viewmodel: CallLogSearchViewModel
    private var isInternetAvailable = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallLogSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initListeners()
        initViewModel()
        observeCallLogsLivedata()
        observeInternetLivedata()



    }
    private fun observeInternetLivedata() {
        val cl = this?.let { ConnectionLiveData(it) }
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
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
        viewmodel = ViewModelProvider(this, CalllogSearchInjectorUtil.provideDialerViewModelFactory(applicationContext)).get(
            CallLogSearchViewModel::class.java)
    }

    private fun initListeners() {
        editTextListener = TextChangeListener(this)
        editTextListener.addListener(binding.searchVCallSearch)
        binding.imgBtnBackCallhistory.setOnClickListener(this)

    }
    private fun initRecyclerView() {


        searchAdapter = CallLogAdapter(this,this,this, isDarkThemeOn() ) {

                id:Long, position:Int, view:View, btn:Int, callLog: CallLogTable, clickType:Int, visibility:Int ->onCallItemClicked(id, position, view, btn, callLog,clickType,visibility)};

            binding.reclrVResultFull.layoutManager = LinearLayoutManager(this)

        binding.reclrVResultFull.layoutManager = CustomLinearLayoutManager(this)
        binding.reclrVResultFull.adapter = this.searchAdapter
    }

    private fun onCallItemClicked(
        id: Long,
        position: Int,
        view: View,
        btn: Int,
        callLog: CallLogTable,
        clickType: Int,
        visibility: Int
    ): Int {
        when(clickType) {
            TYPE_MAKE_CALL ->{
                if(EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)){
                    makeCall(callLog.number)
                }else {
                    requestCallPhonePermission()
                }
            } else ->{
            startIndividualContactActivity(callLog, view)
        }
        }


        return UNMARK_ITEM
    }

    private fun startIndividualContactActivity(log: CallLogTable, view: View) {
        var name = log.nameInPhoneBook
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.number
        }

        val intent = Intent(this, IndividualContactViewActivity::class.java )
        intent.putExtra(com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name )
        intent.putExtra("photo", log.thumbnailFromCp)
        intent.putExtra("color", log.color)

        val pairList = ArrayList<android.util.Pair<View, String>>()
        val imgViewUserPhoto = view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgVThumbnail)
        val textViewCrclr = view.findViewById<TextView>(R.id.textViewCrclr)

//                val p1 = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
//                pairList.add(p1)
        var pair:android.util.Pair<View, String>? = null
        if(log.thumbnailFromCp.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
        }else if(log.imageFromDb.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
        }else{
            pair = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(this,pairList[0] )
        options.toBundle()
        startActivity(intent, options.toBundle())
    }

    override fun isMarked(id: Long?): Boolean {
        return false
    }

    override fun isViewExpanded(id: Long): Boolean {
        return true
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

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable

    }

}