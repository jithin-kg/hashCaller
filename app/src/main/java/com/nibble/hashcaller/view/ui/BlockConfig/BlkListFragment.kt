package com.nibble.hashcaller.view.ui.BlockConfig

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.data.local.db.BlockedListPattern
import com.nibble.hashcaller.view.adapter.BlockListAdapter
import com.nibble.hashcaller.view.ui.tabian.BlogRecyclerAdapter
import com.nibble.hashcaller.view.ui.tabian.DataSource
import com.nibble.hashcaller.view.ui.tabian.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_tabian.*

import kotlinx.android.synthetic.main.fragment_blk_list.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BlkListFragment : Fragment(),View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var blockListView:View
    private lateinit var adapter: BlockListAdapter

    private lateinit var blockListViewModel: MyViewModel


    private lateinit var blogAdapter: BlogRecyclerAdapter

    private fun initRecyclerView(){

        rcrViewPtrnList?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecorator)
            blogAdapter = BlogRecyclerAdapter()
            adapter = blogAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        adapter = BlockListAdapter()






    }
    private fun addDataSet(){
//        val data = DataSource.createDataSet()

//


    }

    override fun onCreateView(  inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?) :View{
        super.onCreate(savedInstanceState)


         blockListView = inflater.inflate(R.layout.fragment_blk_list, container, false)

//        intiRecyclerView()
//
//        blockListViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
//
//        blockListViewModel.allblockedList.observe(viewLifecycleOwner,
//            Observer<List<BlockedListPattern>> { blockedListPatterns ->
//                Log.d(TAG, "onViewCreated: " + blockedListPatterns?.size)
//                adapter = BlockListAdapter(blockedListPatterns)
////                adapter.setBlockedListPatterns(blockedListPatterns)
//                //Update Recycler view
//                adapter.notifyDataSetChanged()
//                //                Toast.makeText(MainActivity.this, "onChanged", Toast.LENGTH_SHORT).show();
//            });
        blockListViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        blockListViewModel.allblockedList.observe(viewLifecycleOwner,
            Observer<List<BlockedListPattern>> { blockedListPatterns ->
                Log.d(TAG, "onViewCreated: " + blockedListPatterns?.size)
//                adapter = BlockListAdapter(blockedListPatterns)
//                adapter.setBlockedListPatterns(blockedListPatterns)
                blockedListPatterns?.let{blogAdapter.submitList(it)}
                //Update Recycler view
//                blogAdapter?.submitList(blockedListPatterns)
//                adapter.notifyDataSetChanged()
                //                Toast.makeText(MainActivity.this, "onChanged", Toast.LENGTH_SHORT).show();
            });


        return blockListView

    }

//    private fun intiRecyclerView() {
//        rcrViewPtrnList?.apply {
//            // set a LinearLayoutManager to handle Android
//            // RecyclerView behavior
//            layoutManager = LinearLayoutManager(activity)
//            // set the custom adapter to the RecyclerView
//            adapter = adapter
//
//        }
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        buttonShow.setOnClickListener(this)

        initRecyclerView()
        addDataSet()
        Log.d(TAG, "onViewCreated: ")
//        blockListView.rcrViewPtrnList.setAdapter(adapter)


    }

    override fun onClick(v: View?) {
        Log.d("Blk", "onClick: ")
        when(v?.id){
            R.id.buttonShow->{
//               blockListViewModel.allblockedList.observe(viewLifecycleOwner, Observer { blockLists ->
//                   Log.d("blk", "onClick: $blockLists " + blockLists.size)
//                   for (patter in  blockLists){
//                       Log.d("blk", " $patter")
//                   }
//                   blockLists?.let { adapter?.setBlockedListPatterns(it) }
//                   adapter?.setBlockedListPatterns(blockLists)
//                blockListViewModel.getVal()

//               })
                val allblockedList = blockListViewModel.allblockedList
                Log.d("blk", "onClick: $allblockedList" )
            }
        }
    }

    companion object{
        private val TAG = "__BlckListFragment"
    }
}