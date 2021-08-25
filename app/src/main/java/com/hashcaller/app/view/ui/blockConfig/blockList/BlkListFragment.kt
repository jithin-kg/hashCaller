package com.hashcaller.app.view.ui.blockConfig.blockList


import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.view.ui.SwipeToDeleteCallback


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
    private lateinit var swipeHandler:SwipeToDeleteCallback
    private lateinit var blockListViewModel: BlockListViewModel


    private lateinit var blockListAdapter: BlockListAdapter


    private fun initRecyclerView(){

//        rcrViewPtrnList?.apply {
//            layoutManager = LinearLayoutManager(activity)
//            ItemTouchHelper(swipeHandler).attachToRecyclerView(rcrViewPtrnList);
//            val topSpacingDecorator =
//                TopSpacingItemDecoration(30)
//            addItemDecoration(topSpacingDecorator)
//            blockListAdapter =
//                BlockListAdapter()
//            adapter = blockListAdapter
//
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        adapter = BlockListAdapter()

        swipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val adapter = rcrViewPtrnList.adapter
                deletePattern(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
//        itemTouchHelper.attachToRecyclerView(rcrViewPtrnList)

    }
    private fun addDataSet(){
//        val data = DataSource.createDataSet()

//


    }

    override fun onCreateView(  inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?) :View{
        super.onCreate(savedInstanceState)
// create ContextThemeWrapper from the original Activity Context with the custom theme
        //darktheme
        val contextThemeWrapper =  ContextThemeWrapper(activity, R.style.Theme_MyDarkTheme);
        setBackgroundTheme()

        // clone the inflater using the ContextThemeWrapper
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

         blockListView = localInflater.inflate(R.layout.fragment_blk_list, container, false)

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
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        blockListViewModel.allblockedList?.observe(viewLifecycleOwner,
            Observer<List<BlockedListPattern>> { blockedListPatterns ->
                Log.d(TAG, "onViewCreated: " + blockedListPatterns?.size)
//                adapter = BlockListAdapter(blockedListPatterns)
//                adapter.setBlockedListPatterns(blockedListPatterns)
                blockedListPatterns?.let{blockListAdapter.submitList(it)}
                //Update Recycler view
//                blogAdapter?.submitList(blockedListPatterns)
//                adapter.notifyDataSetChanged()
                //                Toast.makeText(MainActivity.this, "onChanged", Toast.LENGTH_SHORT).show();
            });


        return blockListView

    }

    @SuppressLint("SwitchIntDef")
    private fun setBackgroundTheme() {
        val nightModeFlags = requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val type = AppCompatDelegate.getDefaultNightMode()
        when (type) {
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d(TAG, "setBackgroundTheme: light")
            }
            UI_MODE_NIGHT_YES -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Log.d(TAG, "setBackgroundTheme: night mode yes")
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                Log.d(TAG, "setBackgroundTheme: undefined")

            }else ->{
            Log.d(TAG, "setBackgroundTheme: else")
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            activity?.setTheme(R.style.MyLightTheme)
        }
        }
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
override fun onDestroyView() {
    super.onDestroyView()
//    rcrViewPtrnList.adapter  = null
}

    override fun onInflate(activity: Activity, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(activity, attrs, savedInstanceState)
        Log.v(TAG, "onInflate called")

        val a = activity.obtainStyledAttributes(attrs, R.styleable.ds)

//        val myString = a.getText(R.styleable.ds)
//        if (myString != null) {
//            Log.v(TAG, "My String Received : $myString")
//        }

//        val myInteger = a.getInt(R.styleable.AdFragment_my_integer, -1)
//        if (myInteger != -1) {
//            Log.v(TAG, "My Integer Received :$myInteger")
//        }

        a.recycle()
    }

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

    /**
     * ItemTouchHelper.Simple call back is used to get swipe in reacycler view
     */



    private fun deletePattern(pos: Int) {
        val item = blockListAdapter.getItemAtPosition(pos);
        blockListViewModel.delete(item.numberPattern, item.type)
        //TODO notify dataset changed in adapter and remove item from the list in adapter
    }

    companion object{
        private val TAG = "__BlckListFragment"
    }
}