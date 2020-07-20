package com.nibble.hashcaller.view.ui.BlockConfig//import android.content.Context
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.nibble.hashcaller.R
//import com.nibble.hashcaller.view.adapter.BlockListAdapter
//import com.nibble.hashcaller.view.ui.BlockConfig.MyViewModel
//import kotlinx.android.synthetic.main.block_list_fragment.*
//import kotlinx.android.synthetic.main.block_list_fragment.switch1
//import kotlinx.android.synthetic.main.fragment_blk_list.*
//
///**
//// * Created by Jithin KG on 03,July,2020
//// */
//class BlockedListFragment : Fragment(),View.OnClickListener {
//    //    private SQLiteDatabaseHandler db;
//    private val listBlocked: List<String> = ArrayList()
//    private var adapter: BlockListAdapter? = null
//    private var blocklistView: View? = null
//    var sharedpreferences: SharedPreferences? = null
//    //Recyclerview
//    private lateinit var blockListViewModel: MyViewModel
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        blocklistView = inflater.inflate(R.layout.block_list_fragment, container, false)
//        initialize()
//        rcrViewPtrnList?.setHasFixedSize(true)
//        adapter = context?.let { BlockListAdapter(it) }
//        rcrViewPtrnList?.adapter = adapter
//        rcrViewPtrnList?.layoutManager = LinearLayoutManager(context)
//
//        //View Model
//        blockListViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
//
//
////        fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fabBtn);
//        /**
//         * Observing for data change and update the emptyList cache in adapter
//         */
//        blockListViewModel.allblockedList.observe(viewLifecycleOwner, Observer { blockLists ->
//            blockLists?.let { adapter?.setBlockedListPatterns(it) }
//            adapter?.setBlockedListPatterns(blockLists)
//        })
//
//
//
//
////        manageFabBtn(fab);
//        return blocklistView
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        switch1?.setOnCheckedChangeListener { buttonView, isChecked ->
//            val editor = sharedpreferences!!.edit()
//            if (isChecked) {
//
//                if (!sharedpreferences!!.contains("onlyRecvCallFromContacts")) {
//                    editor.putBoolean("onlyRecvCallFromContacts", true)
//                    editor.apply()
//                } else {
//                    editor.putBoolean("onlyRecvCallFromContacts", true)
//                    editor.commit()
//                }
//            } else {
//                editor.putBoolean("onlyRecvCallFromContacts", false)
//                editor.commit()
//
//            }
//           buttonShowList.setOnClickListener(this)
//        }
//
//        //TODO i should be giving this observable in appropriate fragments instead of the activity
////        blockListViewModel!!.allPatterns.observe(viewLifecycleOwner, { blockedListPatterns ->
////            adapter!!.setBlockedListPatterns(blockedListPatterns)
////            //Update Recycler view
//////                Toast.makeText(MainActivity.this, "onChanged", Toast.LENGTH_SHORT).show();
////        })
//
//    }
//
//    private fun initialize() {
//
//
//        sharedpreferences = activity?.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE)
//        val `val` = sharedpreferences?.getBoolean("onlyRecvCallFromContacts", false)
//        if (`val`!!) {
//            switch1?.setChecked(true)
//        }
//
//    }
//
//    private fun manageFabBtn(fab: FloatingActionButton) {
//        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
//    }
//
//    companion object {
//        private const val VERTICAL = 30
//        private const val MyPREFERENCES = "onlyIncCallFromContact"
//        private const val TAG = "BlockedListFragment"
//    }
//
//    override fun onClick(v: View?) {
//        Log.d(TAG, "onClick:show list ")
//
//        blockListViewModel.allblockedList.observe(viewLifecycleOwner, Observer { blockLists ->
//            blockLists?.let { adapter?.setBlockedListPatterns(it) }
//            adapter?.setBlockedListPatterns(blockLists)
//        })
//    }
//}