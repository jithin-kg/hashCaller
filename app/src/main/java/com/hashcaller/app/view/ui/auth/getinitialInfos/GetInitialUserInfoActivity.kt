package com.hashcaller.app.view.ui.auth.getinitialInfos


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityGetInitialUserInfoBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.repository.user.UserInfoDTO
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_STORAGE
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.internet.CheckNetwork
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.auth.permissionrequest.PermissionRequestActivity
import com.hashcaller.app.view.ui.contacts.hasMandatoryPermissions
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.hashcaller.app.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.hashcaller.app.view.ui.sms.individual.util.beInvisible
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.ui.sms.individual.util.toast
import com.hashcaller.app.view.utils.imageProcess.ImagePickerHelper
import com.hashcaller.app.view.utils.validateInput
import com.vmadalin.easypermissions.EasyPermissions
import okhttp3.MultipartBody
import java.io.File

class GetInitialUserInfoActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var dataStoreViewmodel: DataStoreViewmodel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var networkChecker:CheckNetwork

    private lateinit var binding: ActivityGetInitialUserInfoBinding
    private var account: GoogleSignInAccount? = null
    private lateinit var imagePickerHelper: ImagePickerHelper
    var imgeMultipartBody: MultipartBody.Part? = null
    private var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null

    private lateinit var startForProfileImageResult: ActivityResultLauncher<Intent>
    private  var imgFile: File? = null

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
        initGoogleSigninClient()
        networkChecker = CheckNetwork(this)
        networkChecker.registerNetworkCallback()
        registerForImagePickerResult()

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
        dataStoreViewmodel = ViewModelProvider(
            this,
            DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)
        ).get(
            DataStoreViewmodel::class.java
        )
    }

    private fun initListeners() {
        binding.imgVAvatarInitial.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
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
        if (text.length > 25) {
            outlinedTextField.error = message
        } else {
            outlinedTextField.error = null
        }


    }


    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when (v?.id) {
            R.id.btnUserContinue -> {
                sendUserInfo()
            }
            R.id.btnGoogle -> {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, PermisssionRequestCodes.RC_SIGN_IN)
            }
            R.id.imgVAvatarInitial -> {
                startImagePickActivity()

//                if (hasStoragePermission()) {
//                    startImagePickActivity()
//                } else {
//                    EasyPermissions.requestPermissions(
//                        this, perms = arrayOf(READ_EXTERNAL_STORAGE),
//                        rationale = "Hash caller need storage permission to configure profile picture",
//                        requestCode = REQUEST_CODE_STORAGE
//                    )
//                }

            }
        }

    }
    private fun initGoogleSigninClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)
    }

    private fun startImagePickActivity() {
        ImagePicker.with(this)
            .cropSquare()	    			//Crop image
            .compress(30)			//Final image size will be less than 30 kb
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
        binding.pgBarImgPick.beVisible()
//        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(pickPhoto, REQUEST_CODE_IMG_PICK)
    }

//    @SuppressLint("LongLogTag")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode != RESULT_CANCELED) {
//            when (requestCode) {
//                REQUEST_CODE_IMG_PICK -> if (resultCode == RESULT_OK && data != null) {
//                    val selectedImageUri: Uri? = data.data
//                    binding.imgVAvatarInitial.setImageURI(selectedImageUri)
////                    userInfoViewModel.processImage(this, selectedImageUri, imagePickerHelper)
//                }
//                PermisssionRequestCodes.RC_SIGN_IN -> {
//                    binding.pgBarInfo.beVisible()
//                    binding.btnUserContinue.isEnabled = false
//                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//                    handleSignInResult(task);
//                }
//            }
//        }
//
//    }
    /**
     * https://developers.google.com/identity/sign-in/android/sign-in
     *
     * for configuring project in google cloud
     * https://developers.google.com/identity/sign-in/android/start-integrating
     */
    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            task?.let{
                account = task?.getResult(ApiException::class.java)
                if (task.isSuccessful) {
                    account?.let {
                        val email = it.email
                        val firstName:String = it.givenName?:""
                        val lastName = it.familyName?:""

                        signupUser(it)
//                        binding.editTextFName.setText(firstName)
//                        if(lastName.isNotEmpty()){
//                            binding.editTextLName.setText(lastName)
//                        }
//                        binding.editTextEmail.setText(email?:"")
//                        account.photoUrl?.let {
//                            googlePhotoUrl = account.photoUrl?.toString()?:""
//                            if(googlePhotoUrl.isNotEmpty()){
//                                Glide.with(this).load(googlePhotoUrl)
//                                    .into(binding.ivAvatar)
//                                binding.tvFirstLetterMain.beInvisible()
//                                isImageAvatarChosenFromGoogle = true
//                            }
//
//                        }
                    }

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            toast("Unable to sign in using google")
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun signupUser(account: GoogleSignInAccount) {
        if (CheckNetwork.isetworkConnected()) {

            userInfoViewModel.signupUserWithGoogle(account).observe(this, Observer {
               if(it!= null){
                   when(it.code()){
                       HttpStatusCodes.STATUS_OK,HttpStatusCodes.STATUS_CREATED -> {
                           userInfoViewModel.insertUserInfoGoogleInDb(
                               it.body()?.data,
                               dataStoreViewmodel
                           ).observe(this, Observer { updateOP->
                               when(updateOP){
                                   OPERATION_COMPLETED -> {
                                       onUserInfoSavedInLocalDb()
                                   }
                               }
                           })
                       }
                   }
               }else {
                   toast("Something went wrong")
               }
            })
        }else {
            toast("No internet")
        }
    }

    private fun registerForImagePickerResult() {

        startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                binding.pgBarImgPick.beInvisible()
                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    val mProfileUri = fileUri
                    imgFile = File(fileUri.path!!)
                    binding.imgVAvatarInitial.setImageURI(fileUri)

//                    showSaveUpdateBtn()
//                    isImageAvatarChosenFromGoogle = false
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    toast(ImagePicker.getError(data))
                } else {
                    toast("No image selected")
                }
            }
    }
    @SuppressLint("LongLogTag")
    private fun sendUserInfo() {

        val firstName = binding.editTextFName.text.toString().trim()
        val lastName = binding.editTextLName.text.toString().trim()
//    val email = binding.editTextEmail.text.toString().trim()

        userInfoViewModel.compresSAndPrepareForUpload(
            imgFile,
            this@GetInitialUserInfoActivity
        ).observe(this@GetInitialUserInfoActivity,
            Observer {
                imgeMultipartBody = it

                binding.editTextFName.error = null
                binding.editTextLName.error = null
                val isValid = validateInput(
                    firstName,
                    lastName,
                    binding.outlinedTextField,
                    binding.outlinedTextField2
                );
                if (isValid) {
                    binding.btnUserContinue.isEnabled = false

                    var userInfo = UserInfoDTO()
                    userInfo.firstName = firstName;
                    userInfo.lastName = lastName;
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
        userInfoViewModel.upload(userInfo, body, this).observe(this, Observer { response ->
            when (response.code()) {
                HttpStatusCodes.CREATED -> {
                    binding.pgBarInfo.beInvisible()
                    response.body()?.let { it1 ->
                        userInfoViewModel.saveUserInfoInLocalDb(it1, dataStoreViewmodel)
                            .observe(this, Observer {
                                when (it) {
                                    OPERATION_COMPLETED -> {
                                        onUserInfoSavedInLocalDb()

                                    }
                                }
                            })
                    }
                }
                else -> {
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

    private fun onUserInfoSavedInLocalDb() {

        if (hasMandatoryPermissions()) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
//                                        val i = Intent(this, PermissionRequestActivity::class.java)
            val i = Intent(
                this,
                PermissionRequestActivity::class.java
            )
            startActivity(i)
            overridePendingTransition(
                R.anim.in_anim,
                R.anim.out_anim
            )
            finish()
        }
    }

    private fun saveToSharedPref(b: Boolean) {
        sharedPreferences = applicationContext.getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE
        )

        val editor = sharedPreferences.edit()
        editor.putBoolean("isUserInfoAvailable", b)
        editor.commit()
    }


    companion object {
        const val TAG = "__GetInitialUserInfoActivity"
    }
}