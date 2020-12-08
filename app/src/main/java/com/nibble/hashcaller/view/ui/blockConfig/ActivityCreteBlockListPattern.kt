package com.nibble.hashcaller.view.ui.blockConfig

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListViewModel
import kotlinx.android.synthetic.main.activity_add_new_pattern.*
import kotlinx.android.synthetic.main.activity_add_new_pattern.editTextNewPattern
import kotlinx.android.synthetic.main.activity_crete_block_list_pattern.*


class ActivityCreteBlockListPattern : AppCompatActivity(), View.OnClickListener {
    private lateinit var  blockListViewModel: BlockListViewModel

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crete_block_list_pattern)

    intiListeners()

//        ActionBar actionBar;
//        actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar();
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
//            setTitle("Add Pattern");

//        //Define ColorDrawable object and parse color
//        // using parseColor method
//        // with color hash code as its parameter
        val colorDrawable = ColorDrawable(Color.parseColor("#3399FF"))
        //
//        // Set BackgroundDrawable
//        actionBar.setBackgroundDrawable(colorDrawable);
//        setTokenInEditText()
    }

    private fun intiListeners() {
        imgBtnBackBlock.setOnClickListener(this)
    }

//    private fun setTokenInEditText() {
//        sharedPreferences = applicationContext.getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
//        Log.d(TAG, "setTokenInEditText: " + sharedPreferences.getString("token", ""))
//        editTextTokenDisplay!!.setText(sharedPreferences.getString("token", ""))
//    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val menuInflater = menuInflater
//        menuInflater.inflate(R.menu.add_new_pattern_acitvity_bar, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.actionSave -> {
//                savePattern()
//                true
//            }
//
//            else -> {
//                finish()
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }

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

//        blockListViewModel =
//            ViewModelProvider(this).get<BlockListViewModel>(BlockListViewModel::class.java)
//        val newPattern = numberPattern!!.text.toString()
//        if (newPattern.trim { it <= ' ' }.isEmpty()) {
//            Toast.makeText(this, "Please Enter pattern to block", Toast.LENGTH_SHORT).show()
//            return
//        }
//        //TODO change country code ....
//        val countryCode: String = ccp.getSelectedCountryCode().replace("+", "")
//        val pattern = "($countryCode$newPattern)([0-9]*)"
//        Log.i("ActivityAddNewPattern", pattern)
//        val blockedListPattern = BlockedListPattern(newPattern, pattern)
//        blockListViewModel.insert(blockedListPattern)
//        finish()
    }

    companion object {
        private const val TAG = "ActivityAddNewPattern"
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnBackBlock->{
                finish()
            }
            R.id.buttonSave->{
                savePattern()
//                true
//                finish()
            }
        }
    }

    fun saveData(view: View) {
        Log.d(TAG, "onClick: ")
        savePattern()
    }
}