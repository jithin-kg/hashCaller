package com.nibble.hashcaller.view.ui.sms.individual

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.DeliverReceiver
import com.nibble.hashcaller.utils.SentReceiver
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES

import com.nibble.hashcaller.view.ui.sms.util.SMS
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
    private var sendBroadcastReceiver: BroadcastReceiver = SentReceiver()
    private var deliveryBroadcastReceiver: BroadcastReceiver = DeliverReceiver()

    //    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_s_m_s)

         contactAddress = intent.getStringExtra(CONTACT_ADDRES)
        contact = contactAddress


        initViewModel()
        initAdapter()
        initListners()
        setupClickListerner()
        registerAdapterListener()
        setupViewmodelObserver()


    }

    private fun initListners() {
        imgBtnSend.setOnClickListener(this)
    }


    private fun setupViewmodelObserver() {
        viewModel.SMS.observe(this, Observer { sms->
            sms.let {
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                adapter.submitList(it)
//                newSize = it.size
                Log.d(TAG, "setupViewmodelObserver: size : ${it.size}")
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

//                adapter.notifyItemRangeChanged(oldSize, it.size)
//                oldSize = it.size
//                recyclerView.scrollToPosition(recyclerView.adapter!!.itemCount -1 )
//                val recyclerViewState =
//                    recyclerView.layoutManager!!.onSaveInstanceState()
//// apply diff result here (dispatch updates to the adapter)
//// apply diff result here (dispatch updates to the adapter)
//                recyclerView.layoutManager!!.onRestoreInstanceState(recyclerViewState)
//                adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
//                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                        super.onItemRangeInserted(positionStart, itemCount)
//                        recyclerView.smoothScrollToPosition(0)
//                    }
//                })



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

        }
    }

    private fun sendSms() {
        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"

        var sendIntent:PendingIntent? = PendingIntent.getBroadcast(this, 0, Intent(SENT), 0)
        var deliveryIntent:PendingIntent? = PendingIntent.getBroadcast(this, 0, Intent(DELIVERED), 0)

        registerReceiver(sendBroadcastReceiver, IntentFilter(SENT))
        registerReceiver(deliveryBroadcastReceiver, IntentFilter(DELIVERED))

        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            contactAddress,null , edtTxtMSg.text.toString() ,
            sendIntent, deliveryIntent
        )

    }
}