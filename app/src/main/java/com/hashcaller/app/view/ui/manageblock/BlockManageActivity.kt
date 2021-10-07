package com.hashcaller.app.view.ui.manageblock

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityBlockManageBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.extensions.requestDefaultSMSrole
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.extensions.isScreeningRoleHeld
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.util.SetAsDefaultSMSSnackbarListener
import kotlinx.android.synthetic.main.block_config_fragment.*


class BlockManageActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SetAsDefaultSMSSnackbarListener.SnackBarListner {
    private lateinit var binding:ActivityBlockManageBinding

//    private lateinit var sharedpreferences: SharedPreferences
    private  var isBlockTopSpammersEnabled = false
    private var isBlockForeignCallsEnabled = false
    private var isBlockNonContactCallsEnabled = false
    private lateinit var viewmodel : BlockSettingsViewModel
    private lateinit var dataStoreRepository: DataStoreRepository
    private lateinit var scrnRoleCallback: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreRepository = DataStoreRepository(this.tokeDataStore)
        initViewmodel()
        observeSharedPrefValues()
        toggleRequestScreeningRoleBtn()
        initListeners()
        setToggleButtons()
        regstrScreeningRoleResultCb()
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, BlockSettingsInjectorUtil.provideContactsViewModelFactory(applicationContext)).get(
            BlockSettingsViewModel::class.java)
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
    private fun observeSharedPrefValues() {
//        sharedpreferences = getSharedPreferences(SHARED_PREF_BLOCK_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
            lifecycleScope.launchWhenCreated {
                binding.blockSpammersAuto.isChecked  =  dataStoreRepository.getBoolean(PreferencesKeys.KEY_BLOCK_COMMONG_SPAMMERS)
                binding.blockForeignCoutries.isChecked = dataStoreRepository.getBoolean(PreferencesKeys.KEY_BLOCK_FOREIGN_NUMBER)
                binding.blockNotIncontacts.isChecked = dataStoreRepository.getBoolean(PreferencesKeys.KEY_BLOCK_NON_CONTACT)
            }



//        dataStoreViewmodel.getBoolean(PreferencesKeys.DO_NOT_RECIEVE_SPAM_SMS).observe(this, Observer {
////            binding.switchDoNotReceiveSpamSMS.isChecked = it
//
//        })

    }



    private fun toggleSwitch(switchId: Int) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.blockNotIncontacts -> {
                setBoolean(PreferencesKeys.KEY_BLOCK_NON_CONTACT, binding.blockNotIncontacts.isChecked)
            }
            R.id.blockSpammersAuto ->{
                  setBoolean(PreferencesKeys.KEY_BLOCK_COMMONG_SPAMMERS, binding.blockSpammersAuto.isChecked)
            }
            R.id.blockForeignCoutries ->{
                setBoolean(PreferencesKeys.KEY_BLOCK_FOREIGN_NUMBER, binding.blockForeignCoutries.isChecked)
            }
        }
    }

    private fun setBoolean(key: String, value:Boolean){
        lifecycleScope.launchWhenCreated {
            dataStoreRepository.setBoolean( value, key)
        }
    }



    companion object{
        const val TAG = "__BlockManageActivity"
    }

    private fun initListeners() {
        binding.blockNotIncontacts.setOnCheckedChangeListener(this)
        binding.blockForeignCoutries.setOnCheckedChangeListener(this)
        binding.blockSpammersAuto.setOnCheckedChangeListener(this)
        binding.imgBtnBackBlk.setOnClickListener(this)
        binding.btnRqstScreeningPermission.setOnClickListener(this)

//        binding.switchDoNotReceiveSpamSMS.setOnClickListener(this)
//        binding.layoutBlockContains.setOnClickListener(this)
//        binding.layoutBlockEndsWith.setOnClickListener(this)
//        binding.layoutBlockBeginsWith.setOnClickListener(this)

    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.imgBtnBackBlk -> {
                finishAfterTransition()
            }
            R.id.btnRqstScreeningPermission -> {
//                    requestScreeningRole()
                    val res = shouldReqstScreeningRole()
                    if(res.first){
                        //we should request screening role
                        val intent = res.second?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        scrnRoleCallback.launch(intent)
                    }
            }
        }
    }
    fun regstrScreeningRoleResultCb() {
        scrnRoleCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
//                callFragment.activtyResultisDefaultScreening()
            }
        }
    }


    private fun onDoNotRecieveSpamSmsClicked() {
//        if(binding.switchDoNotReceiveSpamSMS.isChecked){
//            if(isDefaultSMSHandler()){
//                dataStoreViewmodel.setBoolean(PreferencesKeys.DO_NOT_RECIEVE_SPAM_SMS, binding.switchDoNotReceiveSpamSMS.isChecked)
//            }else {
////                    binding.switchDoNotReceiveSpamSMS.isChecked = false
////                    showSnackB
//                showSnackBar(
//                    binding.layoutBlockManage,
//                    getString(R.string.ennable_for_blocking_sms),
//                    getString(R.string.enable_hash_caller_sms_action),
//                    SetAsDefaultSMSSnackbarListener(this)
//                )
//                binding.switchDoNotReceiveSpamSMS.isChecked = false
//            }
//        }else {
//            dataStoreViewmodel.setBoolean(PreferencesKeys.DO_NOT_RECIEVE_SPAM_SMS, binding.switchDoNotReceiveSpamSMS.isChecked)
//        }


    }



    override fun onSetAsDefaultSMSHandlerClicked() {
        requestDefaultSMSrole()
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }


}