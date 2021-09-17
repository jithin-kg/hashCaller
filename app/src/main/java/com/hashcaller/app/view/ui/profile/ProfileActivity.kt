package com.hashcaller.app.view.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityProfileBinding
import com.hashcaller.app.databinding.BottomSheetProfileEditBinding
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.network.HttpStatusCodes.Companion.STATUS_CREATED
import com.hashcaller.app.repository.user.UserInfoDTO
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.RC_SIGN_IN
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.internet.CheckNetwork
import com.hashcaller.app.utils.internet.InternetChecker
import com.hashcaller.app.view.ui.auth.getinitialInfos.BasicBottomSheetfragment
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.REQUEST_CODE_IMG_PICK
import com.hashcaller.app.view.ui.sms.individual.util.beGone
import com.hashcaller.app.view.ui.sms.individual.util.beInvisible
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.ui.sms.individual.util.toast
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.view.utils.imageProcess.ImagePickerHelper
import com.hashcaller.app.view.utils.validateInput
import com.hashcaller.app.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import okhttp3.MultipartBody

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var bottomSheetBinding:BottomSheetProfileEditBinding
    private lateinit var viewModel: UserInfoViewModel
    private var firstName:String? = null
    private var email:String = ""
    private var bioStr:String = ""
    private var lastName:String? = null
    private lateinit var imagePickerHelper : ImagePickerHelper
    var imageMultipartBody: MultipartBody.Part? = null
    private lateinit var internetChecker:InternetChecker
    private lateinit var networkChecker:CheckNetwork
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private  var rcfirebaseAuth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isImageAvatarChosenFromGoogle = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private  var editTextEmail:EditText? = null


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
        setUpbottomSheet()
        networkChecker.registerNetworkCallback()
        rcfirebaseAuth = FirebaseAuth.getInstance()
//        Firebase.auth.signOut()
        initGoogleSigninClient()
    }

    private fun setUpbottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
//        val s =  BottomSheetProfileEditBinding.inflate(layoutInflater)
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_profile_edit, null)
//        bottomSheetBinding = BottomSheetProfileEditBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(viewSheet)
        editTextEmail = bottomSheetDialog.findViewById<EditText>(R.id.edtTextBSheet)
//        val inflater = LayoutInflater.from(this)
//        bottomSheetBinding = BottomSheetProfileEditBinding.in
//        bottomSheetBinding.

    }

    private fun initGoogleSigninClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initListeners() {
        binding.ivAvatar.setOnClickListener(this)
        binding.btnUpdate.setOnClickListener(this)
        binding.imgBtnBackBlock.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnSignout.setOnClickListener(this)
    }

    private fun observeFormfields() {

        binding.editTextFName.doOnTextChanged{text, start, before, count ->
            toggleSaveButton(text.toString(), firstName, binding.outlinedTextField)

        }
        binding.editTextLName.doOnTextChanged { text, start, before, count ->
            toggleSaveButton(text.toString(), lastName, binding.outlinedTextField2)
        }
        binding.editTextEmail.doOnTextChanged { text, start, before, count ->
            onEmailChanged(text.toString(), email)
        }
        binding.editTextBio.doOnTextChanged{text, start, before, count ->
            toggleSaveButton(text.toString(), bioStr, binding.outlinedTextField4)
        }
    }

    private fun onEmailChanged(text: String, emailInDb: String?) {
        if(text!= emailInDb){
            showSaveUpdateBtn()
            binding.outlinedTextField3.isCounterEnabled = true
        }else {
            binding.outlinedTextField3.isCounterEnabled = false
            hideSaveUpdateBtn()
        }
//        if (binding.editTextFName.text.toString().trim() == firstName &&
//            binding.editTextLName.text.toString().trim() == lastName  &&
//            binding.editTextEmail.text.toString().trim() == email
//
//        ){
//            binding.btnUpdate.beInvisible()
//        }
    }



    private fun toggleSaveButton(text: String, nameInDb: String?, editTextField: TextInputLayout) {
//        if(!text){
            if(text!= nameInDb){
                showSaveUpdateBtn()
                editTextField.isCounterEnabled = true
            }else if(text == nameInDb){
                editTextField.isCounterEnabled = false
            }

            if (binding.editTextFName.text.toString().trim() == firstName &&
                binding.editTextLName.text.toString().trim() == lastName  &&
                binding.editTextEmail.text.toString().trim() == email &&
                binding.editTextBio.text.toString().trim() == bioStr
            ){
                hideSaveUpdateBtn()
            }

//        }

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
            R.id.imgBtnBackBlock -> {
                if(isFormValueNotChanged())
                    finishAfterTransition()
                else
                    toast("Show saved edited change alert")
            }
            R.id.btnUpdate ->{
                updateUserInfo()
            }
            R.id.btnGoogle -> {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
            R.id.btnSignout -> {

                val addPhotoBasicBottomDialogFragment: BasicBottomSheetfragment =
                    BasicBottomSheetfragment.newInstance()!!
                addPhotoBasicBottomDialogFragment.show(
                    supportFragmentManager,
                    "email_edit"
                )

//                bottomSheetDialog.show()
////                editTextEmail?.requestFocus()
//                lifecycleScope.launchWhenStarted {
////                    delay(1000L)
//                    editTextEmail?.post {
//                        editTextEmail?.requestFocus()
//                        val imm: InputMethodManager =
//                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                        imm.showSoftInput(editTextEmail!!, InputMethodManager.SHOW_IMPLICIT)
//
//
//                    }
//                }


//                googleSignInClient.signOut()

            }
        }
    }

    private fun isFormValueNotChanged(): Boolean {
    return  binding.editTextEmail.text.toString().trim() == email &&
            binding.editTextFName.text.toString().trim() == firstName  &&
            binding.editTextLName.text.toString().trim() == lastName
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
                binding.editTextFName.error = null
                binding.editTextLName.error = null
                binding.editTextEmail.error = null
                binding.editTextBio.error = null

                val firstName = binding.editTextFName.text.toString().trim()
                val lastName = binding.editTextLName.text.toString().trim()
                val email = binding.editTextEmail.text.toString().trim()
                val bio = binding.editTextBio.text.toString().trim()

                val isValid = validateInput(firstName,
                    lastName,
                    binding.outlinedTextField,
                    binding.outlinedTextField2,
                )
                validateEmailAndBio()

                Log.d(TAG, "updateUserInfo:input valid $isValid")
                if(isImageAvatarChosenFromGoogle){
                    //profile photo is chosen from google auth
                    viewModel.updateUserWithGoogleProfile(
                        firstName,
                        lastName,
                        rcfirebaseAuth?.currentUser?.photoUrl.toString()
                    )
                }else {
                    //profile photo is chosen from gallery
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
                }

            }else{
                toast("No internet")
            }
//        }




    }

    private fun validateEmailAndBio() {
        val email = binding.editTextEmail.text.toString().trim()
        val bio = binding.editTextBio.text.toString().trim()

    }

    private fun update(userInfo: UserInfoDTO, imgMultiPart: MultipartBody.Part?){
        binding.pgBar.beVisible()
        viewModel.updateUserInfoInServer(userInfo, imgMultiPart).observe(this, Observer {response->
            when (response.code()) {
                HttpStatusCodes.STATUS_OK,STATUS_CREATED -> {

                   viewModel.updateUserInfoInDb(response.body()?.data?.firstName,
                       response.body()?.data?.lastName, response.body()?.data?.image).observe(this, Observer { status ->
                           when(status){
                               OPERATION_COMPLETED ->{
                                   binding.pgBar.beGone()
//                                   binding.btnUpdate.text = "Save"
                                   binding.btnUpdate.isEnabled = true
                               }
                           }
                   })
                }
                else ->{
                    binding.pgBar.beGone()
//                    binding.btnUpdate.text = "Save"
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
                    showSaveUpdateBtn()
                    val selectedImageUri: Uri? = data.data
                    binding.ivAvatar.setImageURI(selectedImageUri)
                    viewModel.processImage(this, selectedImageUri, imagePickerHelper)
                    isImageAvatarChosenFromGoogle = false
                }
                RC_SIGN_IN -> {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)!!
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e)
                    }
                }
            }
        }
    }
    private fun hideSaveUpdateBtn() {
        binding.btnUpdate.beInvisible()
        binding.imgBtnBackBlock.setImageDrawable(getDrawable(R.drawable.ic_baseline_arrow_back_white))
    }
    fun showSaveUpdateBtn(){
        binding.btnUpdate.beVisible()
        binding.imgBtnBackBlock.setImageDrawable(getDrawable(R.drawable.ic_close_line))

    }
    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        rcfirebaseAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = rcfirebaseAuth?.currentUser
                    val size = user?.providerData?.size
                    val fullName = user?.displayName?:""
                    val nameArray = fullName.split(" ")
                    if(nameArray.size >=2 && user?.isEmailVerified == true){
                        val firstName:String = nameArray[0]
                        val lastName = nameArray[1]
                        binding.editTextFName.setText(firstName)
                        if(lastName.isNotEmpty()){
                            binding.editTextLName.setText(lastName)
                        }
                        if(rcfirebaseAuth?.currentUser?.photoUrl != null){
                            Glide.with(this).load(rcfirebaseAuth?.currentUser?.photoUrl)
                                .into(binding.ivAvatar)
                            binding.tvFirstLetterMain.beInvisible()
                            isImageAvatarChosenFromGoogle = true
                        }

//                         binding.ivAvatar.setImageURI(rcfirebaseAuth?.currentUser?.photoUrl)
//                        loadImage(this,binding.ivAvatar, null, rcfirebaseAuth?.currentUser?.photoUrl,  )
                    }
                    val photoUrl =  rcfirebaseAuth?.currentUser?.photoUrl
                    Log.d(TAG, "firebaseAuthWithGoogle: photourl $photoUrl")
//                    FirebaseAuth.getInstance().currentUser.photoUrl
                    Log.d(TAG, "firebaseAuthWithGoogle: $size")
                    
//                    user.providerData[0].
//                    user?.providerData.forEach{ profile ->
//                        profile.
//                    };
//                    Log.d(TAG, "firebaseAuthWithGoogle: $user")
//                    Log.d(TAG, "firebaseAuthWithGoogle: ${user?.displayName}")
//                    Log.d(TAG, "firebaseAuthWithGoogle: ${user?.email}")
//                    Log.d(TAG, "firebaseAuthWithGoogle: ${user?.isEmailVerified}")
//                    Log.d(TAG, "firebaseAuthWithGoogle: ${currentUser}")
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    updateUI(null)
                }
            }
    }
    override fun onBackPressed() {
        if(!isFormValueNotChanged())
            finishAfterTransition()
        else
            toast("finish updates")
    }

}