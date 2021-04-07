package com.nibble.hashcaller.view.ui.blockConfig

import android.annotation.SuppressLint
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.*
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.KEY_INTENT_BLOCK_LIST
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_ENDS_WITH
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_add_new_pattern.*
import kotlinx.android.synthetic.main.activity_add_new_pattern.editTextNewPattern
import kotlinx.android.synthetic.main.activity_crete_block_list_pattern.*


class ActivityCreteBlockListPattern : AppCompatActivity(), View.OnClickListener,LifecycleObserver,
    AdapterView.OnItemClickListener {
    private lateinit var  blockListViewModel: BlockListViewModel
    private  var themeLiveData:MutableLiveData<Int>? = null
    private var prevtheme:Int? = null
    private var patterntype = NUMBER_STARTS_WITH //by default the type is create pattern number starts with

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")

        super.onCreate(savedInstanceState)
        patterntype = intent.getIntExtra(KEY_INTENT_BLOCK_LIST, NUMBER_STARTS_WITH)
        setContentView(R.layout.activity_crete_block_list_pattern)
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)
        Log.d(TAG, "onCreate: intent value is $patterntype")
        intiListeners()
        setDropDownList()


    }

    private fun setDropDownList() {
        var blocktypes = resources.getStringArray(R.array.custom_block_type)
        val arrayAdapter = ArrayAdapter(this, R.layout.blokc_type_drop_down, blocktypes)

        autoCompletTxtViewBlkType.setAdapter(arrayAdapter)
        autoCompletTxtViewBlkType.setText(blocktypes[patterntype],false)
        autoCompletTxtViewBlkType.onItemClickListener = this
    }


    override fun onBackPressed() {
        finish()
        super.onBackPressed()
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
        val newPattern = formatPhoneNumber(editTextNewPattern?.text?.toString()!!)
        if(newPattern.isNotEmpty()){
            var patternRegex = ""
            when(patterntype){
                NUMBER_STARTS_WITH ->{
                    patternRegex = "$newPattern([0-9]*)"
                }
                NUMBER_ENDS_WITH ->{
                    patternRegex = "([0-9]*$newPattern)"
                }
                NUMBER_CONTAINING ->{
                    patternRegex = "([0-9]*$newPattern[0-9]*)"
                }
            }
            val blockListPattern =
                BlockedListPattern(
                    null,
                    newPattern!!,
                    patternRegex,
                    patterntype
                )
            blockListViewModel.insert(blockListPattern).observe(this, Observer {
                if(it == OPERATION_COMPLETED){
                    finish()
                }
            })
        }


    }

    companion object {
        private const val TAG = "__ActivityAddNewPattern"
    }

    @SuppressLint("LogNotTimber")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnBackBlock->{
                finish()
            }
            R.id.btnSave -> {
                Log.d(TAG, "onClick: $patterntype")
                savePattern()

            }
        }
    }

    fun saveData(view: View) {
        Log.d(TAG, "onClick: ")

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "onItemClick: position $position")
        when(position){

            0 ->{
                patterntype = NUMBER_STARTS_WITH
            }
            1 ->{
                patterntype = NUMBER_ENDS_WITH
            }
            2 ->{
                patterntype = NUMBER_CONTAINING
            }
        }

    }


}