package com.nibble.hashcaller.view.ui.blockConfig

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.*
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListViewModel
import kotlinx.android.synthetic.main.activity_add_new_pattern.*
import kotlinx.android.synthetic.main.activity_add_new_pattern.editTextNewPattern
import kotlinx.android.synthetic.main.activity_crete_block_list_pattern.*


class ActivityCreteBlockListPattern : AppCompatActivity(), View.OnClickListener,LifecycleObserver  {
    private lateinit var  blockListViewModel: BlockListViewModel
    private  var themeLiveData:MutableLiveData<Int>? = null
    private var prevtheme:Int? = null


    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_crete_block_list_pattern)
    intiListeners()
    }





    override fun onPostResume() {

        Log.d(TAG, "onPostResume: ")
        super.onPostResume()
    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun resume(){
//        Log.d(TAG, "resume: ")
//    }
    private fun intiListeners() {
        imgBtnBackBlock.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    private fun savePattern() {
        Log.d(TAG, "save button clicked")
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        val newPattern = editTextNewPattern?.text?.toString()?.trim()
        val patternRegex = "$newPattern([0-9]*)"

        val blockListPattern =
            BlockedListPattern(
                null,
                newPattern!!,
                patternRegex
            )
        blockListViewModel.insert(blockListPattern)

    }

    companion object {
        private const val TAG = "__ActivityAddNewPattern"
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnBackBlock->{
                finish()
            }
            R.id.btnSave->{
                savePattern()
                true
                finish()
            }
        }
    }

    fun saveData(view: View) {
        Log.d(TAG, "onClick: ")

    }


}