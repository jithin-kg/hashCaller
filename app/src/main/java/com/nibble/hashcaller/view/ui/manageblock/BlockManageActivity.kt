package com.nibble.hashcaller.view.ui.manageblock

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.ContacInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_BLOCK_CONFIGURATIONS
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

        initListeners()
        getSharedPrefValues()
        setToggleButtons()


    }

    private fun initListeners() {
        blockNotIncontacts.setOnCheckedChangeListener(this)
        blockForeignCoutries.setOnCheckedChangeListener(this)
        blockSpammersAuto.setOnCheckedChangeListener(this)

    }

    private fun setToggleButtons() {
        blockSpammersAuto.isChecked = isBlockTopSpammersEnabled
        blockForeignCoutries.isChecked = isBlockForeignCallsEnabled
        blockNotIncontacts.isChecked = isBlockNonContactCallsEnabled
    }
    private fun getSharedPrefValues() {

        sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
        isBlockTopSpammersEnabled = sharedpreferences.getBoolean("isBlockTopSpamersAutomaticallyEnabled", false)
        isBlockForeignCallsEnabled = sharedpreferences.getBoolean("isBlockForeignCallsEnabled" ,false)
        isBlockNonContactCallsEnabled  = sharedpreferences.getBoolean("isBlockNonContactCallsEnabled", false)
    }



    private fun toggleSwitch(switchId: Int) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.blockNotIncontacts -> {
               writeToSharedPref("isBlockNonContactCallsEnabled", isChecked)
            }
            R.id.blockSpammersAuto ->{
                writeToSharedPref("isBlockTopSpamersAutomaticallyEnabled", isChecked)
            }
            R.id.blockForeignCoutries ->{
                writeToSharedPref("isBlockForeignCallsEnabled", isChecked)
            }
        }
    }

    private fun writeToSharedPref(key: String, checked: Boolean) {
        val sharedPref = this?.getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean(key, checked)
            apply()
        }
    }

    companion object{
        const val TAG = "__BlockManageActivity"
    }

    override fun onClick(v: View?) {

    }
}