package com.nibble.hashcaller.view.ui.blockConfig

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityCreteBlockListPatternBinding
import com.nibble.hashcaller.utils.constants.IntentKeys
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.MyUndoListener
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.work.formatPhoneNumber


class ActivityCreteBlockListPattern : AppCompatActivity(), View.OnClickListener,LifecycleObserver,
    AdapterView.OnItemClickListener, MyUndoListener.SnackBarListner {

    private lateinit var binding : ActivityCreteBlockListPatternBinding
    private lateinit var  blockListViewModel: BlockListViewModel
    private  var themeLiveData:MutableLiveData<Int>? = null
    private var prevtheme:Int? = null
    private var patterntype = NUMBER_STARTS_WITH //by default the type is create pattern number starts with

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")

        super.onCreate(savedInstanceState)
        binding = ActivityCreteBlockListPatternBinding.inflate(layoutInflater)
        patterntype = intent.getIntExtra(KEY_INTENT_BLOCK_LIST, NUMBER_STARTS_WITH)
        setContentView(binding.root)
        initViewmodel()
        Log.d(TAG, "onCreate: intent value is $patterntype")
        intiListeners()
        setDropDownList()


    }

    private fun initViewmodel() {
        //todo create viewmodelinjector util
        blockListViewModel = ViewModelProvider(this).get(BlockListViewModel::class.java)

    }

    private fun setDropDownList() {
        var blocktypes = resources.getStringArray(R.array.custom_block_type)
        val arrayAdapter = ArrayAdapter(this, R.layout.blokc_type_drop_down, blocktypes)

        binding.autoCompletTxtViewBlkType.setAdapter(arrayAdapter)
        binding.autoCompletTxtViewBlkType.setText(blocktypes[patterntype],false)
        binding.autoCompletTxtViewBlkType.onItemClickListener = this
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
        binding.imgBtnBackBlock.setOnClickListener(this)
       binding.btnSave.setOnClickListener(this)
    }

    private fun savePattern() {
        Log.d(TAG, "save button clicked")
        var message = ""
        val newPattern = formatPhoneNumber(binding.editTextNewPattern?.text?.toString()!!)
        if(newPattern.isNotEmpty()){
            var patternRegex = ""
            when(patterntype){
                NUMBER_STARTS_WITH ->{
                    patternRegex = "$newPattern([0-9]*)"
                    message = "Calls and SMS number starting with $newPattern will be blocked."
                }
                NUMBER_ENDS_WITH ->{
                    patternRegex = "([0-9]*$newPattern)"
                    message = "Calls and SMS number ending  with $newPattern will be blocked."
                }
                NUMBER_CONTAINING ->{
                    patternRegex = "([0-9]*$newPattern[0-9]*)"
                    message = "Calls and SMS number containing  $newPattern will be blocked."
                }
            }
            blockListViewModel.insert(newPattern, patterntype).observe(this, Observer {
                if(it == OPERATION_COMPLETED){
//                    finish()
                    showSnackBar(message)
                }
            })
        }


    }

    private fun showSnackBar(message: String) {

        val sbar = Snackbar.make(binding.layoutCreatePattern,
            message,
            Snackbar.LENGTH_SHORT)
//        lastOperationPerformed = OPERTION_MUTE
        sbar.setAction("View", MyUndoListener(this))
//        sbar.anchorView = bottomNavigationView

        sbar.show()
//        Handler().postDelayed(
//            {
//            val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }, 1500)
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

    override fun onUndoClicked() {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra(IntentKeys.SHOW_BLOCK_LIST,IntentKeys.SHOW_BLOCK_LIST_VALUE )
//        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
////        startActivity(intent)
//        startActivityIfNeeded(intent, 0)
        finishAfterTransition()
    }


}