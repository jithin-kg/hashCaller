package com.hashcaller.app.view.ui.blockConfig

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityCreteBlockListPatternBinding
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_CONTAINS
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_ENDS_WITH
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_STARTS_WITH
import com.hashcaller.app.stubs.TelephonyInfo
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListViewModel
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.sms.individual.util.KEY_INTENT_BLOCK_LIST
import com.hashcaller.app.work.formatPhoneNumber


class ActivityCreteBlockListPattern : AppCompatActivity(), View.OnClickListener,LifecycleObserver,
    AdapterView.OnItemClickListener, MyUndoListener.SnackBarListner {

    private lateinit var binding : ActivityCreteBlockListPatternBinding
    private lateinit var  blockListViewModel: BlockListViewModel
    private  var themeLiveData:MutableLiveData<Int>? = null
    private var prevtheme:Int? = null
    private var patterntype = BLOCK_TYPE_STARTS_WITH //by default the type is create pattern number starts with

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")

        super.onCreate(savedInstanceState)
        binding = ActivityCreteBlockListPatternBinding.inflate(layoutInflater)
        patterntype = intent.getIntExtra(KEY_INTENT_BLOCK_LIST, BLOCK_TYPE_STARTS_WITH)
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
       finishAfterTransition()
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
                BLOCK_TYPE_STARTS_WITH ->{
                    patternRegex = "$newPattern([0-9]*)"
                    message = "Calls from number starting with $newPattern will be blocked."
                }
                BLOCK_TYPE_ENDS_WITH ->{
                    patternRegex = "([0-9]*$newPattern)"
                    message = "Calls from number ending  with $newPattern will be blocked."
                }
                BLOCK_TYPE_CONTAINS ->{
                    patternRegex = "([0-9]*$newPattern[0-9]*)"
                    message = "Calls from number containing  $newPattern will be blocked."
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

    @SuppressLint("LogNotTimber", "NewApi")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnBackBlock->{
                finishAfterTransition()
            }
            R.id.btnSave -> {
                Log.d(TAG, "onClick: $patterntype")
                savePattern()
//                getSimIndexForSubscriptionId()
//                getAvailableSIMCardLabels()
//                getSimAndNumberPairList()
//                isDualSimOrNot()
//                requestHint()
            }
        }
    }





    private fun isDualSimOrNot() {
        val telephonyInfo = TelephonyInfo.getInstance(this)
        val imeiSIM1 = telephonyInfo.imeiSIM1
        val imeiSIM2 = telephonyInfo.imeiSIM2
        val isSIM1Ready = telephonyInfo.isSIM1Ready
        val isSIM2Ready = telephonyInfo.isSIM2Ready
        val isDualSIM = telephonyInfo.isDualSIM
        Log.i(
            "Dual = ", """ IME1 : $imeiSIM1
 IME2 : $imeiSIM2
 IS DUAL SIM : $isDualSIM
 IS SIM1 READY : $isSIM1Ready
 IS SIM2 READY : $isSIM2Ready
"""
        )
    }

    fun saveData(view: View) {
        Log.d(TAG, "onClick: ")

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "onItemClick: position $position")
        when(position){

            0 ->{
                patterntype = BLOCK_TYPE_STARTS_WITH
            }
            1 ->{
                patterntype = BLOCK_TYPE_ENDS_WITH
            }
            2 ->{
                patterntype = BLOCK_TYPE_CONTAINS
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