package com.nibble.hashcaller.view.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityProfileBinding
import com.nibble.hashcaller.network.NetworkResponseBase.Companion.EVERYTHING_WENT_WELL
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.internet.CheckNetwork
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.view.utils.getDecodedBytes
import com.nibble.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.nibble.hashcaller.view.utils.validateInput
import com.nibble.hashcaller.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import okhttp3.MultipartBody

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: UserInfoViewModel
    private var firstName:String? = null
    private var lastName:String? = null
    private lateinit var imagePickerHelper : ImagePickerHelper
    var imageMultipartBody: MultipartBody.Part? = null
    private lateinit var internetChecker:InternetChecker
    private lateinit var networkChecker:CheckNetwork
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        networkChecker = CheckNetwork(this)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser
        tokenHelper =  TokenHelper(user)
        initViewmodel()
        observeUserInfo()
        observeFormfields()
        initListeners()
        networkChecker.registerNetworkCallback()

    }

    private fun initListeners() {
        binding.ivAvatar.setOnClickListener(this)
        binding.btnUpdate.setOnClickListener(this)
    }

    private fun observeFormfields() {
        binding.editTextFName.doOnTextChanged{text, start, before, count ->
            toggleSaveButton(text.toString(), firstName)

        }
        binding.editTextLName.doOnTextChanged { text, start, before, count ->
            toggleSaveButton(text.toString(), lastName)
        }
    }

    private fun toggleSaveButton(text: String, nameInDb: String?) {
        if(!text.isNullOrEmpty()){
            if(text!= nameInDb){
                binding.btnUpdate.beVisible()
            }
            if (binding.editTextFName.text.toString() == firstName && binding.editTextLName.text.toString() == lastName){
                binding.btnUpdate.beInvisible()
            }

        }
    }

    private fun observeUserInfo() {
        viewModel.userInfoLivedata.observe(this, Observer {
            if (it != null) {
                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
                binding.tvFirstLetterMain.text = fLetter
                firstName = "${it.firstname}"
                lastName = "${it.lastName}"

                binding.editTextFName.setText(firstName)
                binding.editTextLName.setText(lastName)

                if(!it.photoURI.isNullOrEmpty()){
                    binding.ivAvatar.setImageBitmap(getDecodedBytes(it.photoURI))
                    binding.tvFirstLetterMain.beInvisible()
                }else{
                    binding.tvFirstLetterMain.beVisible()
                }
            }
        })
    }

    private fun initViewmodel() {
        viewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
        imagePickerHelper = ImagePickerHelper()
        internetChecker = InternetChecker(this)
    }
    companion object{
        const val TAG = "__ProfileActivity"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivAvatar ->{
                if(hasStoragePermission()) {
                    startImagePickActivity()
                }else{
                    EasyPermissions.requestPermissions(this, perms= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        rationale = "Hash caller need storage permission to configure profile picture",
                        requestCode= PermisssionRequestCodes.REQUEST_CODE_STORAGE
                    )
                }
            }
            R.id.btnUpdate ->{
                updateUserInfo()
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    private fun updateUserInfo() {
            if(CheckNetwork.isetworkConnected()){
                binding.btnUpdate.isEnabled = false
                val firstName = binding.editTextFName.text.toString().trim()
                val lastName = binding.editTextLName.text.toString().trim()

                viewModel.compresSAndPrepareForUpload(imagePickerHelper.imgFile, this).observe(this,
                    Observer {
                        imageMultipartBody = it

                        binding.editTextFName.error = null
//    binding.editTextEmail.error = null
                        binding.editTextLName.error = null
                        val isValid = validateInput(firstName, lastName, binding.outlinedTextField, binding.outlinedTextField2);
                        if(isValid){

                            var userInfo = UserInfoDTO()
                            userInfo.firstName = firstName;
                            userInfo.lastName =  lastName;

                            update(userInfo, imageMultipartBody)
                        }
                    })
            }else{
                toast("No internet")
            }
//        }




    }

    private fun update(userInfo: UserInfoDTO, imgMultiPart: MultipartBody.Part?){
        binding.pgBar.beVisible()
        viewModel.updateUserInfoInServer(userInfo, imgMultiPart).observe(this, Observer {
            when (it.isEverytingWentWell) {
                EVERYTHING_WENT_WELL -> {
                   viewModel.updateUserInfoInDb(it.result?.result?.firstName,
                       it.result?.result?.lastName, it.result?.result?.image).observe(this, Observer { status ->
                           when(status){
                               OPERATION_COMPLETED ->{
                                   binding.pgBar.beGone()
                                   binding.btnUpdate.isEnabled = true
                               }
                           }
                   })
                }
                else ->{
                    toast("Something went wrong")
                }
            }
        })
    }
    private fun startImagePickActivity() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_CODE_IMG_PICK)
    }
    @SuppressLint("LongLogTag")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                REQUEST_CODE_IMG_PICK -> if (resultCode == RESULT_OK && data != null) {
                    binding.btnUpdate.beVisible()
                    val selectedImageUri: Uri? = data.data
                    binding.ivAvatar.setImageURI(selectedImageUri)
                    viewModel.processImage(this, selectedImageUri, imagePickerHelper)
                }
            }
        }

    }

}