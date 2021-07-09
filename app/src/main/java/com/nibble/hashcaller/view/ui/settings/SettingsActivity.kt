package com.nibble.hashcaller.view.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySettingsBinding
import com.nibble.hashcaller.network.HttpStatusCodes
import com.nibble.hashcaller.network.user.GetUserDataResponse
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.manageblock.BlockManageActivity
import com.nibble.hashcaller.view.ui.notifications.ManageNotificationsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.view.ui.userdata.GetUserDataActiivty
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.Exception


class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivitySettingsBinding
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private  lateinit  var userInfoViewModel: UserInfoViewModel
    private var isInternetAvailable = false
    private var readPermissionGranted = false;
    private var writePermissionGranted = false;
    private val fileName = "myData.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper =  TokenHelper(user)
        initViewmodel()
        initListeners()
        observeInternetLivedata()

    }
    private  fun observeInternetLivedata() {
        val cl = ConnectionLiveData(this)
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
    }

    private fun checkPermission() {
       val hasReadPermission =  EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
       val hasWritePermission = EasyPermissions.hasPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //write permission is only needed on android api level <=29
       val minSDK29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSDK29

        val permissionList: MutableList<String> = mutableListOf()
        if(!readPermissionGranted){
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!writePermissionGranted){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionList.isNotEmpty()){
            val request = PermissionRequest.Builder(this)
                .code(PermisssionRequestCodes.REQUEST_CODE_CALL_LOG)
                .perms(permissionList.toTypedArray())
                .rationale("HashCaller needs access to storage to get user data")
                .positiveButtonText("Continue")
                .negativeButtonText("Cancel")
                .build()
            EasyPermissions.requestPermissions(this, request)


            EasyPermissions.requestPermissions(this, request )
        }

    }

    private fun saveFileToExternalStorage(response: Response<GetUserDataResponse>?){
        if(response!=null){
            when(response.code()){
                HttpStatusCodes.STATUS_OK -> {
                    if(response.body()!=null){
                        userInfoViewModel.saveFile(
                            response.body(),
                            openFileOutput(fileName, MODE_PRIVATE)
                            ).observe(this, Observer { operationStatus ->
                            when(operationStatus){
                                OPERATION_COMPLETED -> {
                                    val intent = Intent(this@SettingsActivity, GetUserDataActiivty::class.java)
                                    startActivity(intent)
                                }
                            }


                        })
                    }
                }
            }
        }
//lifecycleScope.launchWhenStarted {
//withContext(Dispatchers.IO){
//     var fos:FileOutputStream? = null
//     var fis:FileInputStream? = null
//    try {
//        fos =  openFileOutput(fileName, MODE_PRIVATE)
//        fos?.write(text.toByteArray())
////        toast("file saved to $filesDir / $fileName")
//        Log.d(TAG, "file saved to $filesDir / $fileName")
//    }catch (e:Exception){
//        Log.d(TAG, "saveFileToExternalStorage: $e")
//    }finally {
//        fos?.close()
//    }
//
//    try{
//        fis = openFileInput(fileName)
//        val isr = InputStreamReader(fis)
//        val br = BufferedReader(isr)
//        val sb = StringBuilder()
//        var text:String? = ""
//        var hasNextLine = true
//        while (hasNextLine){
//            text =  br.readLine()
//            if(text!=null){
//                sb.append(text).append("\n")
//            }else {
//                hasNextLine = false
//            }
//        }
//       withContext(Dispatchers.Main){
//           val intent = Intent(this@SettingsActivity, GetUserDataActiivty::class.java)
//           startActivity(intent)
//
//       }
////        Log.d(TAG, "saveFileToExternalStorage: $sb")
//    }catch (e:Exception){
//        Log.d(TAG, "readFile: $e")
//    }
//    finally {
//        fis?.close()
//    }
//}
//}
    }
    private fun initViewmodel() {
        tokenHelper = TokenHelper(FirebaseAuth.getInstance().currentUser)
        userInfoViewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
    }


    private fun initListeners() {
        binding.imgBtnBackMain.setOnClickListener(this)
        binding.layoutManageBlocking.setOnClickListener(this)
        binding.layoutNotifications.setOnClickListener(this)
        binding.layoutRequestUserInfo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutManageBlocking -> {
                startBlockManageActivity()
            }
            R.id.imgBtnBackMain -> {
                finishAfterTransition()
            }
            R.id.layoutNotifications -> {
                val intent = Intent(this, ManageNotificationsActivity::class.java)
                startActivity(intent)
            }
            R.id.layoutRequestUserInfo -> {
                onGetUserDataClicked()
//                val intent = Intent(this, GetUserDataActiivty::class.java)
//                startActivity(intent)

            }
        }
    }

    private fun onGetUserDataClicked() {
        if(isInternetAvailable){
            binding.pgBarGetUserInfo.beVisible()
            userInfoViewModel.getUserDataInHashcaller().observe(this, Observer {
                saveFileToExternalStorage(it)
            })
        }else {
            toast(getString(R.string.no_internet))
        }

    }

    private fun startBlockManageActivity() {
        val intent = Intent(this, BlockManageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
    companion object{
        const val TAG = "__SettingsActivity"
    }
}