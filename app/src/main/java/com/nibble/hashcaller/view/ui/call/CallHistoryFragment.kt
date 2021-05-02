package com.nibble.hashcaller.view.ui.call

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.CallLogAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import kotlinx.android.synthetic.main.fragment_call_history.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallHistoryFragment : Fragment(), View.OnClickListener {
    private lateinit var callHistoryFragment: View
    private lateinit var viewModel: CallHistoryViewmodel
    var callLogAdapter: CallLogAdapter? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callHistoryFragment = inflater.inflate(R.layout.fragment_call_history, container, false)

//        viewModel = ViewModelProvider(this, CallInjectorUtil.provideDialerViewModelFactory(context)).get(
//            CallHistoryViewmodel::class.java)
        if(checkContactPermission()){
            observeCallLog()

        }
        initListeners()
        observePermissionLiveData()
        observeLiveDataLoading()
        return callHistoryFragment;
    }

    private fun observeLiveDataLoading() {
        CallLogLiveData.isLoading.observe(viewLifecycleOwner, Observer {
            if (it){
                //show pg bar
                this.callHistoryFragment.pgbarCallHistory.visibility = View.VISIBLE
            }else{
                this.callHistoryFragment.pgbarCallHistory.visibility = View.GONE
            }
        })
    }

    private fun initListeners() {
        this.callHistoryFragment.btnCallhistoryPermission.setOnClickListener(this)
    }

    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { 
            if(it){
                Log.d(TAG, "observePermissionLiveData: permission given")
                observeCallLog()
                this.callHistoryFragment.btnCallhistoryPermission.visibility = View.GONE
            }else{
                this.callHistoryFragment.btnCallhistoryPermission.visibility = View.VISIBLE
                Log.d(TAG, "observePermissionLiveData: permission not given")
                if (this.viewModel!! != null  ) {
                    if(this.viewModel?.callLogs != null)
                        if(this.viewModel.callLogs!!.hasObservers())
                            this.viewModel?.callLogs?.removeObservers(this);
                }
            }
        })
    }

    private fun checkContactPermission(): Boolean {
        val permissionSms =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CALL_LOG)
        if(permissionSms!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkContactPermission: false")
            this.callHistoryFragment.pgbarCallHistory.visibility = View.GONE
            return false
        }
        Log.d(TAG, "checkContactPermission: true")

        return true
    }

    private fun observeCallLog() {
        viewModel.callLogs.observe(viewLifecycleOwner, Observer { logs->
            logs.let {
//                callLogAdapter?.submitCallLogs(it)
            }
        })
    }
    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkContactPermission()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initListeners()

        Log.d(TAG, "onViewCreated: ")


    }

    private fun initRecyclerView() {
//        rcrViewCallHistoryLogs?.apply {
//            layoutManager = LinearLayoutManager(activity)
//            val topSpacingDecorator =
//                TopSpacingItemDecoration(
//                    30
//                )
//            addItemDecoration(topSpacingDecorator)
////            callLogAdapter = DialerAdapter(context) { id:String, position:Int, view:View, btn:Int, callLog:CallLogData->onCallLogItemClicked(id, position, view, btn, callLog)}
//            adapter = callLogAdapter

//        }
    }
    private fun onCallLogItemClicked(
        id: String,
        position: Int,
        view: View,
        btn: Int,
        callLog: CallLogData
    ) {
        Log.d(TAG, "onCallLog item clicked: $id")
        val id = callLogAdapter!!.getItemId(position)
        val v = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall)

        when(btn){
            CallLogAdapter.BUTTON_SIM_1->{
                Log.d(TAG, "onCallLogItemClicked: buttonsim 1")
               makeCall(callLog)
            }
            CallLogAdapter.BUTTON_SIM_2->{

            }
            CallLogAdapter.BUTTON_SMS->{

            }
            CallLogAdapter.BUTTON_INFO->{

            }

        }
//        if(v.visibility == View.GONE){
//            v.visibility = View.VISIBLE
//        }else{
//            v.visibility = View.GONE
//        }

        Log.d(TAG, "onCallLogItemClicked: ")
//        val intent = Intent(context, IndividualCotactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, id)
//        startActivity(intent)
    }

    private fun makeCall(callLog: CallLogData) {
        val num = callLog.number
        if(num.isNotEmpty())  {
            Log.d(TAG, "onClick: make call ")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            callIntent.data = Uri.parse("tel:$num")
            requireActivity().startActivity(callIntent)
        }
    }

    companion object {
        const val TAG = "__CallHistoryFragment"
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.btnCallhistoryPermission->{
//               val res  = PermissionUtil.requestCallLogPermission(this.requireActivity())
//                Log.d(TAG, "onClick: res is $res")
//                this.permissionGivenLiveData.value = res
            }
        }
    }
}