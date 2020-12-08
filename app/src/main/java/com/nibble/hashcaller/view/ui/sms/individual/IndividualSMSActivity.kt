package com.nibble.hashcaller.view.ui.sms.individual

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.HorizontalDottedProgress
import kotlinx.android.synthetic.main.activity_individual_s_m_s.*


class IndividualSMSActivity : AppCompatActivity(), SMSIndividualAdapter.ItemPositionTracker, View.OnClickListener {
    private lateinit var viewModel:SMSIndividualViewModel
    private lateinit var  recyclerView:RecyclerView
    private var oldList = mutableListOf<SMS>()
    private var contactAddress = ""
    private var oldLIstSize = 0
    private var countNewItem = 0
    private var recyclerViewAtEnd = true
    private var firstime = true
    private lateinit var adapter:SMSIndividualAdapter
    private lateinit var layoutMngr:LinearLayoutManager
//    private var messageSent: MutableLiveData<Boolean> = MutableLiveData()
//    private var time:String? = null
    private var address = ""
//    private var sendBroadcastReceiver: BroadcastReceiver = SentReceiver()
//    private var deliveryBroadcastReceiver: BroadcastReceiver = DeliverReceiver()

    //    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_s_m_s)
//        messageSent.value = false
        setSupportActionBar(toolbarSMSIndividual)

         contactAddress = intent.getStringExtra(CONTACT_ADDRES)
        contact = contactAddress

        observerSmsSent()
        configureToolbar()
        initViewModel()
        initAdapter()
        initListners()
        setupClickListerner()
        registerAdapterListener()
        setupViewmodelObserver()



    }

    private fun configureToolbar() {
        toolbarSMSIndividual.inflateMenu(R.menu.individual_sms_menu)
    }

    private fun observerSmsSent() {
//        messageSent.observe(this, Observer {
//                it->
//            run {
//            if (it == true) {
//                viewModel.moveToSent(time, address)

//                GlobalScope.launch {
//
//                    val values = ContentValues()
//                    values.put("type", "2")
//                    Log.d(TAG, "observerSmsSent: date $time")
//                    Log.d(TAG, "observerSmsSent: address $address")
////                    viewModel.moveToSent(time, address)
////                    contentResolver.update(SMSContract.ALL_SMS_URI, values, "_id='$id'  AND address='$address'",null)
////                   val selctionClause =  Telephony.TextBasedSmsColumns.DATE + " LIKE '$time' AND  "
//                    contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time' and address='$address'",null)
//
//                }

//            }
//            }
//        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.individual_sms_menu, menu)
//        super.onCreateOptionsMenu(menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ")
        return when(item.itemId){

            R.id.itemBlock->{
                Log.d(TAG, "onOptionsItemSelected: block")
                true

            }else->{
                super.onOptionsItemSelected(item)
            }
        }

    }


    private fun initListners() {
        imgBtnSend.setOnClickListener(this)
        imgBtnBackSmsIndividual.setOnClickListener(this)
//        btnUpdate.setOnClickListener(this)
    }


    private fun setupViewmodelObserver() {
        viewModel.SMS.observe(this, Observer { sms->
            sms.let {
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                adapter.submitList(it)
//                newSize = it.size
                Log.d(TAG, "setupViewmodelObserver: size : ${it.size}")
                Log.d(TAG, "setupViewmodelObserver:  type of last item ${it[it.size-1].type}")
                Log.d(TAG, "setupViewmodelObserver: msg of last item ${it[it.size-1].msgString}")
                Log.d(TAG, "setupViewmodelObserver: msg address ${it[it.size-1].addressString}")
                Log.d(TAG, "setupViewmodelObserver: msg time ${it[it.size-1].time}")

                adapter.setList(it)
                if(firstime){
                    recyclerView.scrollToPosition(it.size - 1);
                    firstime = false
                }
                if(!recyclerViewAtEnd){
                         countNewItem = it.size - oldLIstSize

                    tvcountShow.text = countNewItem.toString()
                    tvcountShow.visibility = View.VISIBLE
                }else{
                    clearNewMessageIndication()
                    oldLIstSize = it.size
                }

//                recyclerView.scrollToPosition(adapter.itemCount -1)
                //  adapter.notifyItemRangeInserted(adapter.itemCount, it!!.size -1 )
                if(recyclerViewAtEnd){
//                    recyclerView.scrollToPosition(it.size-1)

                }




            }
        })
    }

    private fun clearNewMessageIndication() {
        tvcountShow.visibility = View.GONE
        tvcountShow.text = ""
        tvNewMsgIndication.visibility = View.GONE

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, SMSIndividualInjectorUtil.provideViewModelFactory(this)).get(
            SMSIndividualViewModel::class.java)
    }

    private fun registerAdapterListener() {
        //for auto scrolling
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = adapter.getItemCount()
                val lastVisiblePosition =
                    layoutMngr.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 &&
                    lastVisiblePosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                } else {
                    if(recyclerViewAtEnd)
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        })
    }

    private fun initAdapter() {
        recyclerView =
            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView


        adapter = SMSIndividualAdapter(this, applicationContext ){ id:String -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.setHasFixedSize(true)
        layoutMngr = LinearLayoutManager(this)
        layoutMngr.stackFromEnd = true
        recyclerView.layoutManager = layoutMngr

        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
    }

    private fun setupClickListerner() {
        tvNewMsgIndication.setOnClickListener(this)
    }

    private fun onContactitemClicked(id: String) {

    }

    companion object{
        var contact:String? = null
        const val TAG = "__IndividualSMSActivity"
        lateinit var dotedPg:HorizontalDottedProgress

        private fun stopDotedAnimation() {
//            dotedPgSMSSenging.clearAnimation()
//            dotedPgSMSSenging.visibility = View.GONE
        }
    }

    override fun lastItemReached() {

        this.recyclerViewAtEnd = true
        tvNewMsgIndication.visibility = View.GONE
        clearNewMessageIndication()
    }

    override fun otherPosition() {
        this.recyclerViewAtEnd = false
        tvNewMsgIndication.visibility = View.VISIBLE
    }

    override fun shouldWeScroll() {
        if(this.recyclerViewAtEnd){
//            recyclerView.scrollToPosition(newSize-1)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.tvNewMsgIndication->{
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                clearNewMessageIndication()
            }R.id.imgBtnSend->{
                    sendSms()
            }
            R.id.imgBtnBackSmsIndividual->{
                finish()
            }
//            R.id.btnUpdate->{
//            viewModel.update()
//        }

        }
    }

    private fun sendSms() {
//        messageSent.value = false
        /**
         * When there is no network the messages is added to the queue
         * and at the moment user clicks send the message is added to outbox
         * and later updated to sended in table ,meanwhile when user starts typing the messsage is added to draft
         */
        Log.d(TAG, "sendSms: clicked contact address $contactAddress")
        val msg = edtTxtMSg.text.toString()
        viewModel.sendSms(msg, applicationContext, contactAddress)





    }
    override fun onResume() {
        super.onResume()
//            LocalBroadcastManager.getInstance(this).registerReceiver(
//                messagesReceiver,
//                IntentFilter("myhashcallersms")
//            )
        registerReceiver(messagesReceiver, IntentFilter("myhashcallersms"))

    }

    override fun onDestroy() {
        unregisterReceiver(messagesReceiver)
        super.onDestroy()
    }

    //receiver as a global variable in your Fragment class
    private val messagesReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.extras != null) {
//                val smsMessageStr = intent.extras!!.getString("idvalue")
                Log.d("messagesReceiver", "onReceive: intent ")
                val extras= intent.extras
                 var time = extras?.getString("date")!!
                 address = extras?.getString("address")!!
//                Log.d(TAG, "onReceive:id $id")
                Log.d(TAG, "onReceive: address $address")
                Log.d(TAG, "onReceive: time $time")
                val values = ContentValues().apply {
                    put("type", "2")
                }
//                viewModel.unregister()
                viewModel.moveToSent(time, address)

//                    Log.d(TAG, "observerSmsSent: date $time")
//                    Log.d(TAG, "observerSmsSent: address $address")
//                contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time'",null)
//                contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time' and address='$address'",null)
//                messageSent.value = true
            }
        }
    }

}

