package com.hashcaller.view.ui.blockConfig

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.hashcaller.R
import com.hashcaller.databinding.BlockConfigFragmentBinding
import com.hashcaller.local.db.blocklist.BlockedListPattern
import com.hashcaller.view.adapter.ViewPagerAdapter
import com.hashcaller.view.ui.MainActivity
import com.hashcaller.view.ui.SwipeToDeleteCallback
import com.hashcaller.view.ui.blockConfig.blockList.BlkListFragment
import com.hashcaller.view.ui.blockConfig.blockList.BlockListAdapter
import com.hashcaller.view.ui.blockConfig.blockList.BlockListViewModel
import com.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.view.ui.extensions.requestAlertWindowPermission
import com.hashcaller.view.ui.sms.individual.util.TYPE_CLICK_ALLOW_OVERLAY
import com.hashcaller.view.ui.sms.individual.util.TYPE_CLICK_DISMISS_OVERLAY
import com.hashcaller.view.ui.sms.individual.util.beGone
import com.hashcaller.view.ui.sms.individual.util.beVisible
import com.hashcaller.view.utils.IDefaultFragmentSelection
import com.hashcaller.view.utils.TopSpacingItemDecoration


/**
 * Created by Jithin KG on 03,July,2020
 */
//import database.sql.SQLiteDatabaseHandler;
class BlockConfigFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection {
    //    private SQLiteDatabaseHandler db;
    private var isDflt = false
    private var blkListFragment : BlkListFragment? = null
    private lateinit var binding: BlockConfigFragmentBinding
    private lateinit var blockListAdapter: BlockListAdapter
    private lateinit var blockListViewModel: BlockListViewModel
    private lateinit var swipeHandler: SwipeToDeleteCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BlockConfigFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(savedInstanceState!= null){
//            if(childFragmentManager.getFragment(savedInstanceState, "blkListFragment") !=null)
//            this.blkListFragment = childFragmentManager.getFragment(savedInstanceState, "blkListFragment") as BlkListFragment

        }
        initSwipeHandler()
        initRecyclerView()


    }
    private fun initSwipeHandler() {
        context?.let {
            swipeHandler = object : SwipeToDeleteCallback(it) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding.rcrViewPtrnList.adapter
                    deletePattern(viewHolder.adapterPosition)
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(binding.rcrViewPtrnList)
        }

    }
    private fun deletePattern(pos: Int) {
        val item = blockListAdapter.getItemAtPosition(pos);
        blockListViewModel.delete(item.numberPattern, item.type).observe(this, Observer {
            blockListAdapter.notifyItemChanged(pos)
        })

        //TODO notify dataset changed in adapter and remove item from the list in adapter
    }

    private fun initViewModel() {
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
    }

    private fun initRecyclerView(){

        binding.rcrViewPtrnList?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rcrViewPtrnList);
            val topSpacingDecorator =
                TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecorator)
            blockListAdapter =
                BlockListAdapter(){clickType:Int -> onListItemClicked(clickType)}
            adapter = blockListAdapter

        }
    }
    fun onListItemClicked(clickType:Int){
        when(clickType){
            TYPE_CLICK_ALLOW_OVERLAY ->{
                (activity as AppCompatActivity).requestAlertWindowPermission()
            }
            TYPE_CLICK_DISMISS_OVERLAY -> {
                blockListViewModel.setDismissedState(true)
                removePermissionItemFromList()
            }
        }
    }

    private fun removePermissionItemFromList() {
        blockListAdapter?.removePermissionItemFromView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && !this::blockListViewModel.isInitialized){
//            if (context?.hasReadContactsPermission() == true) {
                initRecyclerView()
                initViewModel()
                 observeBlocklistLivedata()


//            } else {
//                hideRecyevlerView()
//            }
        }
    }
    private fun observeBlocklistLivedata() {
        blockListViewModel.allblockedList?.observe(viewLifecycleOwner,
            Observer<MutableList<BlockedListPattern>> { blockedListPatterns ->
                binding.pgBarBlockList.beGone()
               lifecycleScope.launchWhenStarted {
                   blockedListPatterns?.let{ list->
                       //important to set animation controller for recyclerview to show recyclerview animation
                       val animationController: LayoutAnimationController =
                           AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim_recycler_view)
                       binding.rcrViewPtrnList.layoutAnimation = animationController
                       if(!Settings.canDrawOverlays(context) && !blockListViewModel.getDismissedState()){
                           list.add(0, BlockedListPattern(id=LIST_DUMMY_ID,"", "", 0))
                       }else {
                           if(list.isNotEmpty()){
                               if(list[0].id == LIST_DUMMY_ID){
                                   list.removeAt(0)
                               }
                           }
                       }
                       blockListAdapter.submitPatternsList(list)

                   }
                   if(blockedListPatterns.isNullOrEmpty()){
                       binding.tvInfo.beVisible()
                   }else {
                       binding.tvInfo.beGone()
                   }
               }
            })
        ;
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        if(this.blkListFragment != null){
//            if(this.blkListFragment!!.isAdded){
//                childFragmentManager.putFragment(outState,"blkListFragment", this.blkListFragment!!)
//            }
//        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intListeners()
//        setupViewPager(viewPagerBlockConfig)
//        tabLayoutBlockConfig?.setupWithViewPager(viewPagerBlockConfig)
    }

    private fun intListeners() {

//       binding.fabBtnShowDialpad?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_add_24))
        binding.fabBtnShowAdd.setOnClickListener(this as View.OnClickListener)
        binding.imgBtnHamBrgerBlk.setOnClickListener(this)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        if(this.blkListFragment == null){
            this.blkListFragment = BlkListFragment()
        }
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
//        viewPagerAdapter.addFragment(BlkListFragment(), "BlockList")
        viewPagerAdapter.addFragment(this.blkListFragment!!, "Blk")
//        viewPagerAdapter.addFragment(BlockScheduleFragment(), "Schedule")
        viewPager?.adapter = viewPagerAdapter
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnHamBrgerBlk ->{
                (activity as MainActivity).showDrawer()
//                requireContext().startSettingsActivity(activity)
            }
            R.id.fabBtnShowAdd -> {
                val i = Intent(context, ActivityCreteBlockListPattern::class.java)
//                i.putExtra("PersonID", personID);
                startActivity(i)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if(Settings.canDrawOverlays(context)){
            removePermissionItemFromList()
        }else {
            addItemPermissionItemToList()
        }

    }

    private fun addItemPermissionItemToList() {
        blockListAdapter.addPermissionItemToList()
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}

    companion object {
        const val TAG = "__BlockConfigFragment"
        const val LIST_DUMMY_ID = -1

    }
}
