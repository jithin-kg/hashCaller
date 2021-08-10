package com.hashcaller.view.ui.auth.getinitialInfos


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.R
import com.hashcaller.databinding.ActivityGetInitialUserInfoBinding
import com.hashcaller.datastore.DataStoreInjectorUtil
import com.hashcaller.datastore.DataStoreViewmodel
import com.hashcaller.network.HttpStatusCodes
import com.hashcaller.repository.user.UserInfoDTO
import com.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_STORAGE
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.view.ui.MainActivity
import com.hashcaller.view.ui.auth.PermissionRequestActivity
import com.hashcaller.view.ui.contacts.hasMandatoryPermissions
import com.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.hashcaller.view.ui.sms.individual.util.beInvisible
import com.hashcaller.view.ui.sms.individual.util.beVisible
import com.hashcaller.view.ui.sms.individual.util.toast
import com.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.hashcaller.view.utils.validateInput
import com.vmadalin.easypermissions.EasyPermissions
import okhttp3.MultipartBody

class GetInitialUserInfoActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var dataStoreViewmodel: DataStoreViewmodel

    private lateinit var binding:ActivityGetInitialUserInfoBinding
//    private var imgFile: File? = null
//    private var picturePath: String = ""
    private lateinit var imagePickerHelper : ImagePickerHelper
    var imgeMultipartBody:MultipartBody.Part? = null
    private  var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetInitialUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rcfirebaseAuth = FirebaseAuth.getInstance()
        tokenHelper = TokenHelper(rcfirebaseAuth?.currentUser)
        clearErrorMessageOnFocus()
        binding.btnUserContinue.setOnClickListener(this)
        imagePickerHelper = ImagePickerHelper()
        initViewmodels()
        initListeners()

//        loadImage(this, binding.imgVAvatarInitial, "@drawable/contact_circular_background_grey")

    }

    private fun initViewmodels() {
        userInfoViewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                this,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
        dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(
            DataStoreViewmodel::class.java
        )
    }

    private fun initListeners() {
        binding.imgVAvatarInitial.setOnClickListener(this)
    }

    private fun clearErrorMessageOnFocus() {
        binding.editTextFName.doOnTextChanged { text, start, before, count ->
            if (text != null) {
                val message = "First name can have maximum 25 characters"
                toggleErrorMessage(text, binding.outlinedTextField, message)
            }
        }

        binding.editTextLName.doOnTextChanged { text, start, before, count ->
            if (text != null) {
                val message = "Last name can have maximum 25 characters"

                toggleErrorMessage(text, binding.outlinedTextField2, message)
            }
        }
    }

    private fun toggleErrorMessage(
        text: CharSequence,
        outlinedTextField: TextInputLayout,
        message: String
    ) {
            if(text.length > 25){
                outlinedTextField.error = message
            }else{
                outlinedTextField.error = null
            }


    }


    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.btnUserContinue -> {
                binding.btnUserContinue.isEnabled = false
                Log.d(TAG, "onClick: btn")
                sendUserInfo()
            }
            R.id.imgVAvatarInitial -> {
                if(hasStoragePermission()){
                    startImagePickActivity()
                }else{
                    EasyPermissions.requestPermissions(this, perms= arrayOf(READ_EXTERNAL_STORAGE),
                        rationale = "Hash caller need storage permission to configure profile picture",
                        requestCode=REQUEST_CODE_STORAGE)
                }

            }
        }

    }
    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)
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
                    val selectedImageUri: Uri? = data.data
                    binding.imgVAvatarInitial.setImageURI(selectedImageUri)
                    userInfoViewModel.processImage(this, selectedImageUri, imagePickerHelper)
                }
            }
        }

    }



    @SuppressLint("LongLogTag")
private fun sendUserInfo() {

    val firstName = binding.editTextFName.text.toString().trim()
    val lastName = binding.editTextLName.text.toString().trim()
//    val email = binding.editTextEmail.text.toString().trim()

        userInfoViewModel.compresSAndPrepareForUpload(imagePickerHelper.imgFile,
            this@GetInitialUserInfoActivity).observe(this@GetInitialUserInfoActivity,
            Observer {
                imgeMultipartBody = it

                binding.editTextFName.error = null
                binding.editTextLName.error = null
                val isValid = validateInput(firstName, lastName, binding.outlinedTextField, binding.outlinedTextField2);
                if(isValid){
                    Log.d(TAG, "isvalid: ")
                    var userInfo = UserInfoDTO()
                    userInfo.firstName = firstName;
                    userInfo.lastName =  lastName;
//        userInfo.email = "email";

//        val body: MultipartBody.Part = createFormData.createFormData("files[0]", file.getName(), requestFile)
//        userInfo.profilePic =imgPart



                    upload(userInfo, imgeMultipartBody)
                }
            })




}



    @SuppressLint("LongLogTag")
    private fun upload(userInfo: UserInfoDTO, body: MultipartBody.Part?) {
        binding.pgBarInfo.beVisible()
        userInfoViewModel.upload(userInfo, body, this).observe(this, Observer {response->
            when(response.code()){
                HttpStatusCodes.CREATED ->{
                    binding.pgBarInfo.beInvisible()
                    response.body()?.let { it1 ->
                        userInfoViewModel.saveUserInfoInLocalDb(it1, dataStoreViewmodel).observe(this, Observer {
                            when (it) {
                                OPERATION_COMPLETED -> {

                                    saveToSharedPref(true)
                                    if(hasMandatoryPermissions()){
                                        val i = Intent(this, MainActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }else {
                                        val i = Intent(this, PermissionRequestActivity::class.java)
                                        startActivity(i)
                                        overridePendingTransition(R.anim.in_anim,
                                            R.anim.out_anim
                                        )
                                        finish()
                                    }

                                }
                            }
                        })
                    }
                }
                else ->{
                binding.pgBarInfo.beInvisible()
                toast("Something went wrong")
                binding.btnUserContinue.isEnabled = true
            }
            }

        })

//        userInfoViewModel.upload(userInfo).observe(this, Observer {
//
//            it?.let { resource: Resource<Response<NetWorkResponse>?> ->
////                val resMessage = resource.data?.body()?.message
////                when (resource.status) {
////
////                    Status.SUCCESS -> {
////                        if (resMessage.equals(EUserResponse.NO_SUCH_USER)) { // there is no such user in server
////                            Log.d(TAG, "checkIfNewUser: no such user")
////                            //This is a new user
////                            val i = Intent(applicationContext, GetInitialUserInfoActivity::class.java)
////                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                            //set userLoggedIn = false in shared preference
////
////                            saveToSharedPref(false)
////
////                            applicationContext.startActivity(i)
////
////                        }else if(resMessage.equals(EUserResponse.EXISTING_USER)){
////                            Log.d(UserUploadHelper.TAG, "upload: user already exist")
//////                            val i  = Intent(applicationContext, MainActivity::class.java)
//////                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//////                            //set userLogedIn = true in shared preferecce
//////                            saveToSharedPref(true)
////
////                            userInfoViewModel.saveUserInfoInLocalDb(resMessage)
////                            val i  = Intent(this, MainActivity::class.java)
////                            startActivity(i)
////                            finish()
////
////                        }
////                        Log.d(TAG, "checkIfNewUser: success ${resource.data?.body()?.message}")
////                    }
////                    Status.LOADING -> {
////                        Log.d(TAG, "checkIfNewUser: Loading")
////                    }
////                    else -> {
////                        Log.d(TAG, "checkIfNewUser: else $resource")
////                        Log.d(TAG, "checkIfNewUser:error ")
////                    }
////
////
////                }
////
////            }
//        })
    }

    private fun saveToSharedPref(b: Boolean) {
        sharedPreferences = applicationContext.getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE
        )

        val editor = sharedPreferences.edit()
        editor.putBoolean("isUserInfoAvailable", b)
        editor.commit()
    }



    companion object{
        const val TAG = "__GetInitialUserInfoActivity"
    }
}