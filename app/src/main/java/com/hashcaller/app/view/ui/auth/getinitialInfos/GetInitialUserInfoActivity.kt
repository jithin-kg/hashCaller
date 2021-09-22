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
    private lateinit var googleSignInCallBack: ActivityResultLauncher<Intent>

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
        registerGoogleActivityResult()


    }
    fun registerGoogleActivityResult() {
        googleSignInCallBack = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task);
        }
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
        when (v?.id) {
            R.id.btnUserContinue -> {
                sendUserInfo()
            }
            R.id.btnGoogle -> {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInCallBack.launch(signInIntent)
            }
            R.id.imgVAvatarInitial -> {
                startImagePickActivity()
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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
    }

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
                        signupUser(it)
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            toast("Unable to sign in using google")
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
    }

    private fun onUserInfoSavedInLocalDb() {

        if (hasMandatoryPermissions()) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
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