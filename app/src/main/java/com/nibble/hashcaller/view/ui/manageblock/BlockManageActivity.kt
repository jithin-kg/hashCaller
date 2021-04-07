package com.nibble.hashcaller.view.ui.manageblock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListActivity
import com.nibble.hashcaller.view.ui.contacts.isBlkForeignCallsEnabled
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isBlockTopSpammersAutomaticallyEnabled
import com.nibble.hashcaller.view.ui.contacts.writeBoolToSharedPref
import com.nibble.hashcaller.view.ui.extensions.isScreeningRoleHeld
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import kotlinx.android.synthetic.main.activity_block_manage.*


class BlockManageActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    private lateinit var sharedpreferences: SharedPreferences
    private  var isBlockTopSpammersEnabled = false
    private var isBlockForeignCallsEnabled = false
    private var isBlockNonContactCallsEnabled = false
    private lateinit var viewmodel : BlockSettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_manage)

        viewmodel = ViewModelProvider(this, BlockSettingsInjectorUtil.provideContactsViewModelFactory(this)).get(
            BlockSettingsViewModel::class.java)
        toggleRequestScreeningRoleBtn()
        initListeners()
        getSharedPrefValues()
        setToggleButtons()


    }

    override fun onPostResume() {
        super.onPostResume()
        toggleRequestScreeningRoleBtn()
    }

    private fun toggleRequestScreeningRoleBtn() {
        var isButtonHide = false // indicates whether the btnRqstScreeningPermission should be hidden or not
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
              if(isScreeningRoleHeld()){
                  isButtonHide = true
              }
            } else {
                isButtonHide = true
            }

        if(isButtonHide){
            layoutScreeningPermission.beGone()
            imgViewDivierBlockUnderBtn.beGone()
        }else{
            layoutScreeningPermission.beVisible()
            imgViewDivierBlockUnderBtn.beVisible()
        }
    }


    private fun setToggleButtons() {
        blockSpammersAuto.isChecked = isBlockTopSpammersEnabled
        blockForeignCoutries.isChecked = isBlockForeignCallsEnabled
        blockNotIncontacts.isChecked = isBlockNonContactCallsEnabled
    }
    private fun getSharedPrefValues() {
        sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
        isBlockTopSpammersEnabled = isBlockTopSpammersAutomaticallyEnabled()
        isBlockForeignCallsEnabled = isBlkForeignCallsEnabled()
        isBlockNonContactCallsEnabled  = isBlockNonContactsEnabled()
    }



    private fun toggleSwitch(switchId: Int) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.blockNotIncontacts -> {
               writeBoolToSharedPref("isBlockNonContactCallsEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)
            }
            R.id.blockSpammersAuto ->{
                writeBoolToSharedPref("isBlockTopSpamersAutomaticallyEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)
            }
            R.id.blockForeignCoutries ->{
                writeBoolToSharedPref("isBlockForeignCallsEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)
            }
        }
    }

//    private fun writeToSharedPref(key: String, checked: Boolean) {
//        val sharedPref = this?.getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
//        with (sharedPref.edit()) {
//            putBoolean(key, checked)
//            apply()
//        }
//    }

    companion object{
        const val TAG = "__BlockManageActivity"
    }

    private fun initListeners() {
        blockNotIncontacts.setOnCheckedChangeListener(this)
        blockForeignCoutries.setOnCheckedChangeListener(this)
        blockSpammersAuto.setOnCheckedChangeListener(this)
        layoutBlockContains.setOnClickListener(this)
        layoutBlockEndsWith.setOnClickListener(this)
        layoutBlockBeginsWith.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.layoutBlockContains ->{
                startBlockListActivity(NUMBER_CONTAINING)
            }
            R.id.layoutBlockEndsWith ->{
                startBlockListActivity(NUMBER_ENDS_WITH)
            }
            R.id.layoutBlockBeginsWith ->{
                startBlockListActivity(NUMBER_STARTS_WITH)
            }
        }
    }

    private fun startBlockListActivity(value: Int) {
        val intent = Intent(this, BlockListActivity::class.java)
        intent.putExtra(KEY_INTENT_BLOCK_LIST, value )
        startActivity(intent)
    }
}