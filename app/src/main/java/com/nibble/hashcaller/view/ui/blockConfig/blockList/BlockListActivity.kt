package com.nibble.hashcaller.view.ui.blockConfig.blockList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityBlockListBinding
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.SwipeToDeleteCallback
import com.nibble.hashcaller.view.ui.blockConfig.ActivityCreteBlockListPattern
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.sms.individual.util.KEY_INTENT_BLOCK_LIST
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_block_list.*
import kotlinx.android.synthetic.main.fragment_blk_list.*

class BlockListActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityBlockListBinding
    private lateinit var blockListView: View
    private lateinit var adapter: BlockListAdapter
    private lateinit var swipeHandler: SwipeToDeleteCallback
    private lateinit var blockListViewModel: BlockListViewModel
    private var blockType  = NUMBER_STARTS_WITH
    private lateinit var blockListAdapter: BlockListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        blockType = intent.getIntExtra(KEY_INTENT_BLOCK_LIST, NUMBER_STARTS_WITH )
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        binding = ActivityBlockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeBlocklistLivedata()
        intiListeners()
        initSwipeHandler()
        initRecyclerView()
    }

    private fun intiListeners() {
        binding.fabBtnAddNewBlock.setOnClickListener(this)
    }

    private fun initSwipeHandler() {
        swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rcrViewPtrnList.adapter
                deletePattern(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rcrViewPtrnList)
    }

    private fun observeBlocklistLivedata() {
        blockListViewModel.allblockedList?.observe(this,
            Observer<List<BlockedListPattern>> { blockedListPatterns ->
                Log.d(TAG, "onViewCreated: " + blockedListPatterns?.size)

                blockedListPatterns?.let{blockListAdapter.submitPatternsList(it)}
            });
    }

    private fun deletePattern(pos: Int) {
        val item = blockListAdapter.getItemAtPosition(pos);
        blockListViewModel.delete(item.numberPattern, item.type).observe(this, Observer {
            blockListAdapter.notifyItemChanged(pos)
        })

        //TODO notify dataset changed in adapter and remove item from the list in adapter
    }

    override fun onDestroy() {
        super.onDestroy()
      binding.rcrViewPtrnList.adapter  = null
    }

    private fun initRecyclerView(){

       binding.rcrViewPtrnList?.apply {
            layoutManager = CustomLinearLayoutManager(this@BlockListActivity)
            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rcrViewPtrnList);
            val topSpacingDecorator =
                TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecorator)
            blockListAdapter =
                BlockListAdapter()
            adapter = blockListAdapter

        }
    }

    companion object{
        const val TAG = "__BlockListActivity"
    }

    override fun onClick(v: View?) {
        val i = Intent(this, ActivityCreteBlockListPattern::class.java)
//                i.putExtra("PersonID", personID);
        i.putExtra(KEY_INTENT_BLOCK_LIST, blockType)

        startActivity(i)
    }


}