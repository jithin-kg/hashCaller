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
import com.nibble.hashcaller.databinding.ActivityBlockManageBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListActivity
import com.nibble.hashcaller.view.ui.contacts.isBlkForeignCallsEnabled
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.isBlockTopSpammersAutomaticallyEnabled
import com.nibble.hashcaller.view.ui.contacts.writeBoolToSharedPref
import com.nibble.hashcaller.view.ui.extensions.isScreeningRoleHeld
import com.nibble.hashcaller.view.ui.sms.individual.util.*


class BlockManageActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    private lateinit var binding:ActivityBlockManageBinding

    private lateinit var sharedpreferences: SharedPreferences
    private  var isBlockTopSpammersEnabled = false
    private var isBlockForeignCallsEnabled = false
    private var isBlockNonContactCallsEnabled = false
    private lateinit var viewmodel : BlockSettingsViewModel
    private lateinit var dataStoreViewmodel: DataStoreViewmodel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewmodel()
        toggleRequestScreeningRoleBtn()
        initListeners()
        getSharedPrefValues()
        setToggleButtons()


    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, BlockSettingsInjectorUtil.provideContactsViewModelFactory(applicationContext)).get(
            BlockSettingsViewModel::class.java)
        dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(DataStoreViewmodel::class.java)
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
            binding.layoutScreeningPermission.beGone()
            binding.imgViewDivierBlockUnderBtn.beGone()
        }else{
            binding.layoutScreeningPermission.beVisible()
            binding.imgViewDivierBlockUnderBtn.beVisible()
        }
    }


    private fun setToggleButtons() {
        binding.blockSpammersAuto.isChecked = isBlockTopSpammersEnabled
        binding.blockForeignCoutries.isChecked = isBlockForeignCallsEnabled
        binding.blockNotIncontacts.isChecked = isBlockNonContactCallsEnabled
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
//               writeBoolToSharedPref("isBlockNonContactCallsEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)


            }
            R.id.blockSpammersAuto ->{
//                writeBoolToSharedPref("isBlockTopSpamersAutomaticallyEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)
                dataStoreViewmodel.enableBlockCommonSpammers(isChecked)

            }
            R.id.blockForeignCoutries ->{
//                writeBoolToSharedPref("isBlockForeignCallsEnabled", isChecked, SHARED_PREF_BLOCK_CONFIGURATIONS)
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
        binding.blockNotIncontacts.setOnCheckedChangeListener(this)
        binding.blockForeignCoutries.setOnCheckedChangeListener(this)
        binding.blockSpammersAuto.setOnCheckedChangeListener(this)
        binding.layoutBlockContains.setOnClickListener(this)
        binding.layoutBlockEndsWith.setOnClickListener(this)
        binding.layoutBlockBeginsWith.setOnClickListener(this)

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