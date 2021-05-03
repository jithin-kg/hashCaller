package com.nibble.hashcaller.view.ui.auth.getinitialInfos


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
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityGetInitialUserInfoBinding
import com.nibble.hashcaller.network.NetworkResponseBase.Companion.EVERYTHING_WENT_WELL
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.view.utils.imageProcess.ImagePickerHelper
import com.nibble.hashcaller.view.utils.validateInput
import okhttp3.MultipartBody

class GetInitialUserInfoActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var binding:ActivityGetInitialUserInfoBinding
//    private var imgFile: File? = null
//    private var picturePath: String = ""
    private lateinit var imagePickerHelper : ImagePickerHelper
    var imgeMultipartBody:MultipartBody.Part? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetInitialUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clearErrorMessageOnFocus()
        binding.btnUserContinue.setOnClickListener(this)
        imagePickerHelper = ImagePickerHelper()
        userInfoViewModel = ViewModelProvider(
            this, UserInfoInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(
            UserInfoViewModel::class.java
        )
        initListeners()

//        loadImage(this, binding.imgVAvatarInitial, "@drawable/contact_circular_background_grey")

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
                Log.d(TAG, "onClick: btn")
                sendUserInfo()
            }
            R.id.imgVAvatarInitial -> {
                startImagePickActivity()
            }
        }
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
        userInfoViewModel.upload(userInfo, body, this).observe(this, Observer {
            when(it.isEverytingWentWell){
                EVERYTHING_WENT_WELL ->{

                    binding.pgBarInfo.beInvisible()
                    it.result?.let { it1 ->
                        userInfoViewModel.saveUserInfoInLocalDb(it1).observe(this, Observer {
                            when (it) {
                                OPERATION_COMPLETED -> {
                                    saveToSharedPref(true)
                                    val i = Intent(this, MainActivity::class.java)
                                    startActivity(i)
                                    finish()
                                }
                            }
                        })
                    }
                } else ->{
                binding.pgBarInfo.beInvisible()
                toast("Something went wrong")
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