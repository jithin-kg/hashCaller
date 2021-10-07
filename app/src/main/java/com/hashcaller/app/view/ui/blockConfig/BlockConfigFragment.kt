package com.hashcaller.app.view.ui.blockConfig

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
import androidx.viewpager.widget.ViewPager
import com.hashcaller.app.R
import com.hashcaller.app.databinding.BlockConfigFragmentBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.blocklist.BlockTypes
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_CONTAINS
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_ENDS_WITH
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CALL_LOG
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CONTACTS
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_WITH
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListAdapter
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListViewModel
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.extensions.requestAlertWindowPermission
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.utils.SwipeHelper
import com.hashcaller.app.view.utils.ConfirmDialogFragment2
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.TopSpacingItemDecoration


/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockConfigFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection {
    //    private SQLiteDatabaseHandler db;
    private var isDflt = false
    private lateinit var binding: BlockConfigFragmentBinding
    private lateinit var blockListAdapter: BlockListAdapter
    private lateinit var blockListViewModel: BlockListViewModel
//    private lateinit var swipeHandler: SwipeToDeleteCallback
    private lateinit var  itemTouchHelper: ItemTouchHelper
    private lateinit var dataStoreRepository: DataStoreRepository
    private var patternToDelete:BlockedListPattern? = null
    private lateinit var generalBlockViewmodel: GeneralblockViewmodel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BlockConfigFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    private fun archiveButton(position: Int) : SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(
            requireContext(),
            "Archive",
            14.0f,
            android.R.color.holo_blue_light,
            object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {
                    requireContext().toast("Archived item $position")
                }
            })
    }
    private fun markAsUnreadButton(position: Int) : SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(
            requireContext(),
            "Mark as unread",
            14.0f,
            android.R.color.holo_green_light,
            object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {
                   requireContext(). toast("Marked as unread item $position")
                }
            })
    }





    private fun deletePattern(pos: Int) {
        val item = blockListAdapter.getItemAtPosition(pos);
        blockListViewModel.delete(item.numberPattern, item.type).observe(this, Observer {
            blockListAdapter.notifyItemChanged(pos)
        })

    }

    private fun initViewModel() {
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        generalBlockViewmodel = ViewModelProvider(this, GeneralBlockInjectorUtil.provideViewModel(
            requireContext()
        )).get(GeneralblockViewmodel::class.java)
    }

    private fun initRecyclerView(){

        binding.rcrViewPtrnList?.apply {
            layoutManager = CustomLinearLayoutManager(context)
//            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rcrViewPtrnList);
            val topSpacingDecorator =
                TopSpacingItemDecoration(30)
            addItemDecoration(topSpacingDecorator)
//            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            blockListAdapter =
                BlockListAdapter(){clickType:Int,pattern:BlockedListPattern?, position:Int? -> onListItemClicked(clickType, pattern, position)}
            adapter = blockListAdapter


        }
    }
    fun onListItemClicked(clickType: Int, pattern: BlockedListPattern?, position: Int?){
        when(clickType){
            TYPE_CLICK_ALLOW_OVERLAY ->{
                (activity as AppCompatActivity).requestAlertWindowPermission()
            }
            TYPE_CLICK_DISMISS_OVERLAY -> {
                blockListViewModel.setDismissedState(true)
                removePermissionItemFromList()
            }
            TYPE_CLICK_DELETE_PATTERN -> {
                var message = ""
                pattern?.let {p->
                    when(p.type){
                        BLOCK_TYPE_STARTS_WITH -> {
                            message = "Unblock numbers that starts with '${p.numberPattern}'?"
                        }
                        BLOCK_TYPE_CONTAINS -> {
                            message = "Unblock numbers that contains '${p.numberPattern}'?"
                        }
                        BLOCK_TYPE_ENDS_WITH -> {
                            message = "Unblock numbers that ends with '${p.numberPattern}'?"
                        }
                        BLOCK_TYPE_EXACT_NUMBER -> {
                            message = "Unblock number'${p.numberPattern}' ? "
                        }
                        BLOCK_TYPE_FROM_CALL_LOG, BLOCK_TYPE_FROM_CONTACTS -> {
                            var name: String = p.name
                            if(name.isEmpty())
                                name = p.numberPattern
                            message = "Unblock '$name' ?"
                        }
                    }
                    patternToDelete = p
                    showAlert(message)
                }

            }
        }
    }

    private fun showAlert(message: String) {
        val dialog = ConfirmDialogFragment2(
            getSpannableString("Calls from this number won't be blocked"),
            getSpannableString(message)
            )
        {action:Int -> onDialogFragmentAction(action)}
        dialog.show(requireActivity().supportFragmentManager, "unblock")
    }

    private fun onDialogFragmentAction(actionType:Int){
        when(actionType){
            ConfirmDialogFragment2.ON_POSITIVE_ACTION -> {
                patternToDelete?.let {
//                    if(it.type == BlockTypes.BLOCK_TYPE_EXACT_NUMBER || it.type == BlockTypes.BLOCK_TYPE_FROM_CALL_LOG || it.type == BlockTypes.BLOCK_TYPE_FROM_CONTACTS ){
                        generalBlockViewmodel.removeFromBlockList(
                            it.numberPattern,
                            it.type,
                            getRandomColor(),
                            requireActivity()
                        )
                }

            }

        }
    }

    private fun removePermissionItemFromList() {
        if(this::blockListAdapter.isInitialized)
            blockListAdapter.removePermissionItemFromView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden && !this::blockListViewModel.isInitialized){
            initRecyclerView()
            initViewModel()
            observeBlocklistLivedata()

        }
    }
    private fun observeBlocklistLivedata() {
        blockListViewModel.allblockedList?.observe(viewLifecycleOwner,
            Observer<MutableList<BlockedListPattern>> { blockedListPatterns ->
                binding.pgBarBlockList.beGone()
               lifecycleScope.launchWhenStarted {
                   blockedListPatterns?.let{ list->
                       //important to set animation controller for recyclerview to show recyclerview animation
//                       val animationController: LayoutAnimationController =
//                           AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim_recycler_view)

//                       binding.rcrViewPtrnList.layoutAnimation = animationController
//                       if(!Settings.canDrawOverlays(context) && !blockListViewModel.getDismissedState()){
//                           list.add(0, BlockedListPattern(id=LIST_DUMMY_ID,"", "", 0))
//                       }else {
//                           if(list.isNotEmpty()){
//                               if(list[0].id == LIST_DUMMY_ID){
//                                   list.removeAt(0)
//                               }
//                           }
//                       }
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
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        if(Settings.canDrawOverlays(context)){
            binding.layoutOverlayPermission.beGone()
        }else {
            binding.layoutOverlayPermission.beVisible()
        }
        dataStoreRepository = DataStoreRepository(requireContext().tokeDataStore)
        lifecycleScope.launchWhenStarted {
            binding.radioCntct.isChecked = dataStoreRepository.getBoolean(PreferencesKeys.KEY_BLOCK_NON_CONTACT)
        }
    }

    private fun initListeners() {

        with(binding){
            layoutRadioContactsOnly.setOnClickListener(this@BlockConfigFragment)
            radioCntct.setOnClickListener(this@BlockConfigFragment)
            fabBtnShowAdd.setOnClickListener(this@BlockConfigFragment as View.OnClickListener)
            imgBtnHamBrgerBlk.setOnClickListener(this@BlockConfigFragment)
            btnDismiss.setOnClickListener {
                layoutOverlayPermission.beGone()
            }
            btnSetup.setOnClickListener {
                (activity as AppCompatActivity).requestAlertWindowPermission()
            }
        }

    }

    private fun setupViewPager(viewPager: ViewPager?) {
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnHamBrgerBlk ->{
                (activity as MainActivity).showDrawer()
            }
            R.id.fabBtnShowAdd -> {
                val i = Intent(context, ActivityCreteBlockListPattern::class.java)
//                val i = Intent(context, SampleActivity::class.java)
                startActivity(i)
            }
            R.id.layoutRadioContactsOnly -> {
                binding.radioCntct.isChecked = !binding.radioCntct.isChecked
                onRadioContactChange()
            }
            R.id.radioCntct -> {
                onRadioContactChange()
            }
        }

    }

    private fun onRadioContactChange() {
        lifecycleScope.launchWhenStarted {
            dataStoreRepository.setBoolean(binding.radioCntct.isChecked, PreferencesKeys.KEY_BLOCK_NON_CONTACT )
           if(binding.radioCntct.isChecked){
               requireContext().toast("Only calls from your contacts will be able to reach you")
           }
        }
    }

    override fun onResume() {
        super.onResume()
        if(Settings.canDrawOverlays(context)){
            binding.layoutOverlayPermission.beGone()
        }
//        if(Settings.canDrawOverlays(context)){
//            removePermissionItemFromList()
//        }else {
//            addItemPermissionItemToList()
//        }

    }

    private fun addItemPermissionItemToList() {
        if(this::blockListAdapter.isInitialized)
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
