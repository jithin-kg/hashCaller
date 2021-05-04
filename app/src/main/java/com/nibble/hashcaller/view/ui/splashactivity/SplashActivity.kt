package com.nibble.hashcaller.view.ui.splashactivity


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.nibble.hashcaller.R
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.network.user.GetUserInfoResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.PermissionRequestActivity
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.PERMISSION_REQUEST_CODE
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.vmadalin.easypermissions.EasyPermissions
import java.io.IOException
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

//TODO I should check this in each activity whether the use is logged in or not,
//make seperate helper class for this checking

class SplashActivity : AppCompatActivity() {

    private val TAG = "__SplashActivity"

    private val RC_SIGN_IN = 1

    private lateinit var rcfirebaseAuth: FirebaseAuth
    private lateinit var rcAuthStateListener: AuthStateListener
    //    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private val userCollectionRef: CollectionReference = db.collection("Users")
    var user: FirebaseUser? = null
    private lateinit var encryptor: EnCryptor
    private lateinit var decryptor: Decryptor
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var key : ByteArray
    private var metadata:FirebaseUserMetadata?= null;
    companion object{
        private const val KEY_ALIAS = "MYKeyAlias"
        private const val KEY_STORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/NoPadding"

        private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
        private  var _userInfoViewModel: UserInfoViewModel? = null
        private  val userInfoViewModel get() = _userInfoViewModel!!

        private var _dataStoreViewModel: DataStoreViewmodel? = null
        private val dataStoreViewModel  get() = _dataStoreViewModel!!
//    private lateinit var skey:SecretKey
    }
    override fun onPause() {
        super.onPause()
        if(::rcfirebaseAuth.isInitialized && ::rcAuthStateListener.isInitialized){
            rcfirebaseAuth.removeAuthStateListener(rcAuthStateListener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rcfirebaseAuth = FirebaseAuth.getInstance()
        initViewModel()
//         close splash activity
        if (checkPermission()) {
            firebaseAuthListener()
        } else {
            val i = Intent(this@SplashActivity, PermissionRequestActivity::class.java)
            startActivityForResult(i, PERMISSION_REQUEST_CODE)
        }

    }

    private fun initViewModel() {
        _dataStoreViewModel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(this)).get(
            DataStoreViewmodel::class.java)

    }

    private fun firebaseAuthListener() {
        rcAuthStateListener =
            AuthStateListener { firebaseAuth ->
                user = firebaseAuth.currentUser
                //                    Task<GetTokenResult> idToken = FirebaseUser.getIdToken();
                if (user != null) {
                    //user is signed in
                  checkUserInfoInDb()

                } else {
                    // user is signed out
                    onSingnedOutcleanUp()

                }
            }
    }

    private fun checkUserInfoInDb() {
      dataStoreViewModel.getToken().observe(this, Observer {
          if(!it.isNullOrEmpty()){
              startMainActivity()
          }else{
              onSingnedOutcleanUp()
          }
      })
    }


    private fun  saveToken(idToken: String?) {
        try {
            encryptor = EnCryptor()
            val encryptedText = encryptor?.encryptText(SAMPLE_ALIAS,idToken.toString())

            /**
             * Base64.encode method helps to get the encrypted byte array to readable string
             * which we want to save in sharedPrefrences
             */

            val encodeTokenString = Base64.encodeToString(
                encryptedText,
                Base64.DEFAULT
            )
//        Base64.decode(encodeTokenString, Base64.DEFAULT)


//            Saving encryped token in sharedPreferences
            sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString(SHARED_PREFERENCE_TOKEN_KEY, encodeTokenString)

            editor.commit()


        } catch (e: UnrecoverableEntryException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchProviderException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: KeyStoreException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: IOException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchPaddingException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        /**
         * Managing contacts uploading/Syncing by ContactsUPloadWorkManager
         */
//        val request =
//            OneTimeWorkRequest.Builder(ContactsUploadWorker::class.java)
//                .build()
//        WorkManager.getInstance().enqueue(request)

    }

    @Throws(Exception::class)
    private fun encrypt(raw: ByteArray, clear: ByteArray): ByteArray? {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        return cipher.doFinal(clear)
    }


    private fun onSignedInInitialize() {
        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var idToken = task.result?.token
                    // Send token to your backend via HTTPS

                    saveToken(idToken) //save token to sharedpref

                    if(!isUserInfoExistInDb()){
                        //check in firebase

//                        val isNewUserInFirebase = isNewUserInFirebase()
//
//                        if(isNewUserInFirebase){
//                            Log.d(TAG, "onSignedInInitialize:checkIfNewUser Returned true")
//                            startGetUserInfoAcitvity()
//
//
//                        }
//                        else{
//                            if(!isUserInfoExistInDb()){

//                                 val i = Intent(this, GetInitialUserInfoActivity::class.java)
//                                 startActivity(i)
                        //check user exist in server
//                        getUserInfoFromServer()
//                                if(getUserInfoFromServer() == null){
//                                    startGetUserInfoAcitvity()
//                                }else {
//                                    val i = Intent(this, MainActivity::class.java)
//                                    startActivity(i)
//                                    finish()
//                                }


//                            }else{
//                                //already existing user in server
//                                saveToSharedPref(true)
//                                //if ther is no user info in local db navigate to getInitial info activity
//                                startMainActivity()
//
//                            }
//                        }

                    }else{
                        //user info exist in db
//                        startMainActivity()
                    }

//                     }

//                     else{
//                         //user logged in
//                         Log.d(TAG, "onSignedInInitialize: userLogged in  ")
//
//                         val i = Intent(this, MainActivity::class.java)
//                         startActivity(i)
//                         finish()
//                     }

//                    generateEncryptedKey()
                }
//                else {
//                    // Handle error -> task.getException();
//                }
            }


    }
    private fun startMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i)
        finish()
    }

    private fun getUserInfoFromServer() {
//        userInfoViewModel.getUserInfoFromServer().observe(this, Observer {
//            if(it!=null){
//                if(it.result!=null){
//                    if(!it.result.firstName.isNullOrEmpty()){
//                        userInfoViewModel.saveUserInfo(it.result).observe(this, Observer {
//                            when(it){
//                                OPERATION_COMPLETED ->{
//
//                                    val editor = sharedPreferences.edit()
//                                    editor.putBoolean("isUserInfoAvailable", true)
//                                    editor.commit()
//                                    startMainActivity()
//                                }
//                            }
//                        })
//                    }else{
//                        startGetUserInfoAcitvity()
//                    }
//                }
//            }
//        })



    }


    private fun onSingnedOutcleanUp() {

        val i = Intent(this@SplashActivity, ActivityPhoneAuth::class.java)
//        startActivityForResult(i, RC_SIGN_IN)
        startActivity(i)
        finish()
    }

    //This is called after OTP verification
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        var phoneNumber: String? = ""
        super.onActivityResult(requestCode, resultCode, data)

//        if (requestCode == RC_SIGN_IN) {
//            Log.d(TAG, "onActivityResult: note this ")
////            IdpResponse response = IdpResponse.fromResultIntent(data);
////            if (resultCode == Activity.RESULT_OK) {
//            if(rcfirebaseAuth.currentUser!=null){
//                phoneNumber = rcfirebaseAuth?.currentUser?.phoneNumber
//                user = rcfirebaseAuth?.currentUser
//                val uid = user!!.uid
//
////                onSignedInInitialize()
//            }
//
//
//
//
//        }
        if(requestCode == PERMISSION_REQUEST_CODE ){
            Log.d(TAG, "onActivityResult: Permission given")
            firebaseAuthListener()
        }
    }


    private fun isNewUserInFirebase(): Boolean {
        metadata = rcfirebaseAuth?.currentUser!!.metadata

        if (metadata?.creationTimestamp == metadata?.lastSignInTimestamp) {
            // The user is new, show them a fancy intro screen!
            Log.d(TAG, "new user signin")


//            startGetUserInfoAcitvity()
            return true;
        }
        return false


    }

    private fun isUserInfoExistInDb(): Boolean {
        return sharedPreferences.getBoolean("isUserInfoAvailable", false)
    }

//        var isUserInfoExists = false
//        val userInfo = UserInfoDTO()
//        lifecycleScope.launchWhenStarted {
//           val user =  userInfoViewModel.getUserInfo()
//            if(user !=null){
//                isUserInfoExists = true
//            }
//        }
//
//        return isUserInfoExists
//        userInfoViewModel.upload(userInfo).observe(this, Observer {
//            it?.let { resource: Resource<Response<NetWorkResponse>?> ->
//                val resMessage = resource.data?.body()?.message
//                when (resource.status) {
//
//                    Status.SUCCESS -> {
//                        if (resMessage.equals(EUserResponse.NO_SUCH_USER)) { // there is no such user in server
//                            Log.d(TAG, "checkIfNewUser: no such user")
//                            //This is a new user
////                            val i = Intent(this, GetInitialUserInfoActivity::class.java)
////                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            //set userLoggedIn = false in shared preference
//
//                            saveToSharedPref(false)
//
////                            startActivity(i)
//
//                        }else if(resMessage.equals(EUserResponse.EXISTING_USER)){
//                            Log.d(UserUploadHelper.TAG, "upload: user already exist")
////                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                            //set userLogedIn = true in shared preferecce
//                            saveToSharedPref(true)
//                            status = true
////                            applicationContext.startActivity(i)
//
//                        }
//                        Log.d(TAG, "checkIfNewUser: success ${resource.data?.body()?.message}")
//                    }
//                    Status.LOADING -> {
//                        Log.d(TAG, "checkIfNewUser: Loading")
//                    }
//                    else -> {
//                        Log.d(TAG, "checkIfNewUser: else $resource")
//                        Log.d(TAG, "checkIfNewUser:error ")
//                    }
//
//
//                }
//
//            }
//        })
//        return status
//    }

    private fun saveToSharedPref(b: Boolean) {
        sharedPreferences = getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGEDIN", b)
        editor.commit()
    }

    private fun startGetUserInfoAcitvity() {
        Log.d(TAG, "startGetUserInfoAcitvity: called")
        val i = Intent(this, GetInitialUserInfoActivity::class.java)
        startActivity(i)
        finish()
    }
    //todo if existing user get first and last name from server and save in db

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        this.user = rcfirebaseAuth!!.currentUser
    }

    private fun checkPermission(): Boolean {
        return EasyPermissions.hasPermissions(this,
            Manifest.permission.READ_CONTACTS
        )
    }


    fun   encryptText(token: String?):String {
        var encryptedToken:String = ""
        var encryptedText = ""
        try {
//            val encryptedText: ByteArray? = token?.let {
//                encryptor
//                    .encryptText(SAMPLE_ALIAS, it)
//            encryptedText = encryptor
//                .encryptText(SAMPLE_ALIAS, token.toString())
            val s = (encryptor.encryptText(SAMPLE_ALIAS, token.toString().trim())).toString().trim()
            encryptedText = s

//            encryptedToken = encryptedText
//            encryptedToken = Base64.encodeToString(encryptedText, Base64.DEFAULT)


//            encryptedToken= Base64.encodeToString(encryptedText, Base64.DEFAULT)
        } catch (e: UnrecoverableEntryException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchAlgorithmException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchProviderException) {
            Log.e(TAG, "onClick() called with: " + e.message, e)
        } catch (e: KeyStoreException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: IOException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: NoSuchPaddingException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: InvalidKeyException) {
            Log.d(TAG, "onClick() called with: " + e.message, e)
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }
        return encryptedText
    }

    override fun onDestroy() {
        viewModelStore.clear()
        if(::rcfirebaseAuth.isInitialized && ::rcAuthStateListener.isInitialized){
            rcfirebaseAuth.removeAuthStateListener(rcAuthStateListener)
        }

        _dataStoreViewModel = null
        _userInfoViewModel = null
        super.onDestroy()


    }

    override fun onPostResume() {
        super.onPostResume()

        if (checkPermission()) {
            if(::rcAuthStateListener.isInitialized)
                rcfirebaseAuth?.addAuthStateListener(rcAuthStateListener)
        }
    }
}