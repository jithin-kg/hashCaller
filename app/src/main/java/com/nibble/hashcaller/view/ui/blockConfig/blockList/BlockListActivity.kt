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
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.SwipeToDeleteCallback
import com.nibble.hashcaller.view.ui.blockConfig.ActivityCreteBlockListPattern
import com.nibble.hashcaller.view.ui.sms.individual.util.KEY_INTENT_BLOCK_LIST
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_block_list.*
import kotlinx.android.synthetic.main.fragment_blk_list.*
import kotlinx.android.synthetic.main.fragment_blk_list.rcrViewPtrnList

class BlockListActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var blockListView: View
    private lateinit var adapter: BlockListAdapter
    private lateinit var swipeHandler: SwipeToDeleteCallback
    private lateinit var blockListViewModel: BlockListViewModel
    private var blockType  = NUMBER_CONTAINING
    private lateinit var blockListAdapter: BlockListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockType = intent.getIntExtra(KEY_INTENT_BLOCK_LIST, NUMBER_CONTAINING )
        setContentView(R.layout.activity_block_list)
        observeBlocklistLivedata()
        intiListeners()
        initSwipeHandler()
        initRecyclerView()
    }

    private fun intiListeners() {
        fabBtnAddNewBlock.setOnClickListener(this)
    }

    private fun initSwipeHandler() {
        swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rcrViewPtrnList.adapter
                deletePattern(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rcrViewPtrnList)
    }

    private fun observeBlocklistLivedata() {
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        blockListViewModel.allblockedList.observe(this,
            Observer<List<BlockedListPattern>> { blockedListPatterns ->
                Log.d(TAG, "onViewCreated: " + blockedListPatterns?.size)

                blockedListPatterns?.let{blockListAdapter.submitList(it)}
            });
    }

    private fun deletePattern(pos: Int) {
        val item = blockListAdapter.getItemAtPosition(pos);
        blockListViewModel.delete(item.numberPattern)
        //TODO notify dataset changed in adapter and remove item from the list in adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        rcrViewPtrnList.adapter  = null
    }

    private fun initRecyclerView(){

        rcrViewPtrnList?.apply {
            layoutManager = LinearLayoutManager(this@BlockListActivity)
            ItemTouchHelper(swipeHandler).attachToRecyclerView(rcrViewPtrnList);
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