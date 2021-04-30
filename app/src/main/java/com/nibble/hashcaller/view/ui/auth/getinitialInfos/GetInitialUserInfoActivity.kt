package com.nibble.hashcaller.view.ui.auth.getinitialInfos


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityGetInitialUserInfoBinding
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.contacts.utils.loadImage
import com.squareup.okhttp.RequestBody
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class GetInitialUserInfoActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var binding:ActivityGetInitialUserInfoBinding
    private var imgFile: File? = null
    private var picturePath: String = ""
    var body:MultipartBody.Part? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetInitialUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clearErrorMessageOnFocus()
        binding.btnUserContinue.setOnClickListener(this)
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
                    val bytes =
                        selectedImageUri?.let { contentResolver.openInputStream(it)?.readBytes() }
                            ?: return
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

//                    binding.imgVAvatarInitial.setImageURI(selectedImage)
                    loadImage(this, binding.imgVAvatarInitial, null, selectedImageUri)

                    val inputStrm: InputStream? = contentResolver.openInputStream(data.data!!)

                    prepareImageForUpload(getBytes(inputStrm!!))

                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImageUri != null) {
                        val cursor: Cursor? = contentResolver.query(
                            selectedImageUri,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            picturePath = cursor.getString(columnIndex)
                            imgFile = File(picturePath)
//                            lifecycleScope.launchWhenStarted {
//                                val compressedImageFile: File = Compressor.compress(
//                                    this@GetInitialUserInfoActivity,
//                                    imgFile!!
//                                ) {
////                                    resolution(1280, 720)
////                                    quality(80)
////                                    format(Bitmap.CompressFormat.WEBP)
//                                    size(4000) // 700 kb
//                                }
//                                prepareImageForUpload(getBytes(inputStrm!!))

//                                val requestFile: RequestBody? =
//                                    imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//                                body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }
//                                body = compressedImageFile.
//                               userInfoViewModel.compresSAndPrepareForUpload(imgFile,
//                                   this@GetInitialUserInfoActivity).observe(this@GetInitialUserInfoActivity,
//                                   Observer {
//                                       body = it
//                               })



//                                Log.d(TAG, "onActivityResult:$compressedImageFile ")
//                            }

                            cursor.close()
                        }
                    }
                }
            }
        }

    }



    private fun prepareImageForUpload(imageBytes: ByteArray?) {
        val requestFile: okhttp3.RequestBody? =
            imageBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
//        body = requestFile?.let { MultipartBody.Part.createFormData("image", "image.jpg", it) }


    }

    @Throws(IOException::class)
    fun getBytes(`is`: InputStream): ByteArray? {
        val byteBuff = ByteArrayOutputStream()
        val buffSize = 1024
        val buff = ByteArray(buffSize)
        var len = 0
        while (`is`.read(buff).also { len = it } != -1) {
            byteBuff.write(buff, 0, len)
        }
        return byteBuff.toByteArray()
    }
    @SuppressLint("LongLogTag")
private fun sendUserInfo() {

    val firstName = binding.editTextFName.text.toString().trim()
    val lastName = binding.editTextLName.text.toString().trim()
//    val email = binding.editTextEmail.text.toString().trim()

        userInfoViewModel.compresSAndPrepareForUpload(imgFile,
            this@GetInitialUserInfoActivity).observe(this@GetInitialUserInfoActivity,
            Observer {
                body = it

                binding.editTextFName.error = null
//    binding.editTextEmail.error = null
                binding.editTextLName.error = null
                val isValid = validateInput(firstName, lastName);
                if(isValid){
                    Log.d(TAG, "isvalid: ")
                    var userInfo = UserInfoDTO()
                    userInfo.firstName = firstName;
                    userInfo.lastName =  lastName;
//        userInfo.email = "email";

//        val body: MultipartBody.Part = createFormData.createFormData("files[0]", file.getName(), requestFile)
//        userInfo.profilePic =imgPart



                    upload(userInfo, body)
                }
            })




}



    @SuppressLint("LongLogTag")
    private fun upload(userInfo: UserInfoDTO, body: MultipartBody.Part?) {
        userInfoViewModel.upload(userInfo, body).observe(this, Observer {
            userInfoViewModel.saveUserInfoInLocalDb(it).observe(this, Observer {
                when (it) {
                    OPERATION_COMPLETED -> {
                        saveToSharedPref(true)
                        val i = Intent(this, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }
            })

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

    @SuppressLint("LongLogTag")
    private fun validateInput(firstName: String, lastName: String): Boolean {
        var isValid = true;
        if( firstName.isEmpty() || firstName.length < 3 || firstName.length > 25){
            binding.outlinedTextField.error = "First name should contain at least 3 characters"
            isValid = false;
        }
        if(firstName.length> 25){
            binding.outlinedTextField.error = "First name can have maximum 25 characters"
            isValid = false
        }
        if( lastName.length > 25){
            binding.outlinedTextField2.error = "Last name can have maximum 25  characters"
            isValid = false;

        }
        if( lastName.isEmpty() || lastName.length < 2 ){
            binding.outlinedTextField2.error = "Last name should contain at least 3 characters"
            isValid = false;
        }

        return isValid
}

    companion object{
        const val TAG = "__GetInitialUserInfoActivity"
    }
}