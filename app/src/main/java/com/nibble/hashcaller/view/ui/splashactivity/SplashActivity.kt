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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.datastore.PreferencesKeys
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.utils.auth.FirebaseListnerHelper
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.PermissionRequestActivity
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

class SplashActivity : AppCompatActivity(), FirebaseListnerHelper.IFirebaseAuthStateListener {

    private val TAG = "__SplashActivity"

    private val RC_SIGN_IN = 1

    private lateinit var rcfirebaseAuth: FirebaseAuth
//    private lateinit var rcAuthStateListener: AuthStateListener
    //    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private val userCollectionRef: CollectionReference = db.collection("Users")
    var user: FirebaseUser? = null
    private lateinit var firebaseAuthListenerHelper : FirebaseListnerHelper
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
        private  var __userInfoViewModel: SplashActivityViewModel? = null
        private  val userInfoViewModel get() =  __userInfoViewModel!!
        private  var __dataStoreViewmodel: DataStoreViewmodel? = null
        private val dataStoreViewmodel get() =  __dataStoreViewmodel!!
//    private lateinit var skey:SecretKey
    }
    override fun onPause() {
        super.onPause()
        if(::rcfirebaseAuth.isInitialized && ::firebaseAuthListenerHelper.isInitialized){
            rcfirebaseAuth.removeAuthStateListener(firebaseAuthListenerHelper.rcAuthStateListener)    }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                initViewModel()
        rcfirebaseAuth = FirebaseAuth.getInstance()
        firebaseAuthListenerHelper = FirebaseListnerHelper(this )
        observetoken()
        if (checkPermission()) {
            firebaseAuthListenerHelper.firebaseAuthListener()
        } else {
            val i = Intent(this@SplashActivity, PermissionRequestActivity::class.java)
            startActivityForResult(i, PERMISSION_REQUEST_CODE)
        }
    }

    private fun observetoken() {
//        dataStoreViewmodel.getToken().observe(this, Observer {
//            if(!it.isNullOrEmpty()){
//                startMainActivity()
//            }
//        })


        dataStoreViewmodel.getToken().observe(this, Observer {
         if(!it.isNullOrEmpty()){
             startMainActivity()
         }
        })

    }
    private fun initViewModel() {
        __dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(
            DataStoreViewmodel::class.java
        )

        __userInfoViewModel = ViewModelProvider(this, SplashActivityInjectorUtil.provideViewModelFactory(applicationContext)).get(
            SplashActivityViewModel::class.java)
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
            dataStoreViewmodel.saveToken(encodeTokenString)

//        Base64.decode(encodeTokenString, Base64.DEFAULT)



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


    private fun startMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun getUserInfoFromServer() {
        userInfoViewModel.getUserInfoFromServer().observe(this, Observer {
            if(it!=null){
                if(it.result!=null){
                    if(!it.result.firstName.isNullOrEmpty()){
                        userInfoViewModel.saveUserInfo(it.result).observe(this, Observer {
                            when(it){
                                OPERATION_COMPLETED ->{

                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isUserInfoAvailable", true)
                                    editor.commit()
                                    startMainActivity()
                                }
                            }
                        })
                    }else{
                        startGetUserInfoAcitvity()
                    }
                }
            }
        })



    }

    override fun onSignedInInitialize(currentUser: FirebaseUser) {
        user = currentUser
        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var idToken = task.result?.token
                    // Send token to your backend via HTTPS
                    saveToken(idToken)

                    if(!isUserInfoExistInDb()){
                        getUserInfoFromServer()

                    }else{

                        startMainActivity()
                    }
                }
            }
    }

     override fun onSingnedOutcleanUp() {
         val i = Intent(this@SplashActivity, ActivityPhoneAuth::class.java)
         startActivityForResult(i, RC_SIGN_IN)
     }

    //This is called after OTP verification
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        var phoneNumber: String? = ""
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: note this ")
//            IdpResponse response = IdpResponse.fromResultIntent(data);
//            if (resultCode == Activity.RESULT_OK) {
            if(rcfirebaseAuth.currentUser!=null){
                phoneNumber = rcfirebaseAuth?.currentUser?.phoneNumber
                user = rcfirebaseAuth?.currentUser
                val uid = user!!.uid

//                onSignedInInitialize()
            }


        }
        if(requestCode == PERMISSION_REQUEST_CODE ){
            Log.d(TAG, "onActivityResult: Permission given")
            firebaseAuthListenerHelper.firebaseAuthListener()
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
        Log.d(TAG, "isNewUserInServer: ")
        return sharedPreferences.getBoolean("isUserInfoAvailable", false)
    }

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
//        viewModelStore.clear()
                super.onDestroy()
        __userInfoViewModel = null
        __dataStoreViewmodel = null
        Log.d(TAG, "onDestroy: ")
        if(::rcfirebaseAuth.isInitialized && ::firebaseAuthListenerHelper.isInitialized){

            rcfirebaseAuth.removeAuthStateListener(firebaseAuthListenerHelper.rcAuthStateListener)
        }
//        if(::rcfirebaseAuth.isInitialized && ::rcAuthStateListener.isInitialized){
//            rcfirebaseAuth.removeAuthStateListener(rcAuthStateListener)
//        }



    }

    override fun onPostResume() {
        super.onPostResume()

        if (checkPermission()) {
//            if(::rcAuthStateListener.isInitialized)
//                rcfirebaseAuth?.addAuthStateListener(rcAuthStateListener)
        }
    }
}