package com.nibble.hashcaller.view.ui.call

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_call_history.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallHistoryFragment : Fragment() {
    private lateinit var callHistoryFragment: View
    private lateinit var viewModel: CallHistoryViewmodel
    var callLogAdapter: DialerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        callHistoryFragment = inflater.inflate(R.layout.fragment_call_history, container, false)

        viewModel = ViewModelProvider(this, CallInjectorUtil.provideDialerViewModelFactory(context)).get(
            CallHistoryViewmodel::class.java)
        observeCallLog()
        return callHistoryFragment;
    }

    private fun observeCallLog() {
        viewModel.callLogs.observe(viewLifecycleOwner, Observer { logs->
            logs.let {
                callLogAdapter?.setCallLogs(it)
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        Log.d(TAG, "onViewCreated: ")


    }

    private fun initRecyclerView() {
        rcrViewCallHistoryLogs?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            callLogAdapter = DialerAdapter(context) { id:String, position:Int, view:View->onCallLogItemClicked(id, position, view)}
            adapter = callLogAdapter

        }
    }
    private fun onCallLogItemClicked(
        id: String,
        position: Int,
        view: View
    ) {
        Log.d(TAG, "onCallLog item clicked: $id")
        val id = callLogAdapter!!.getItemId(position)
        val v = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall)
        if(v.visibility == View.GONE){
            v.visibility = View.VISIBLE
        }else{
            v.visibility = View.GONE
        }

        Log.d(TAG, "onCallLogItemClicked: ")
//        val intent = Intent(context, IndividualCotactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, id)
//        startActivity(intent)
    }

    companion object {
        const val TAG = "__CallHistoryFragment"
    }
}