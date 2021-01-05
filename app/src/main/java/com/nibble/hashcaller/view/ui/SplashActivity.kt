package com.nibble.hashcaller.view.ui


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.user.EUserResponse
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.network.user.Status
import com.nibble.hashcaller.network.user.UserUploadHelper
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.auth.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.PermissionRequestActivity
import com.nibble.hashcaller.view.ui.auth.testauth
import com.nibble.hashcaller.view.ui.auth.utils.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.PERMISSION_REQUEST_CODE
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import retrofit2.Response
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

//TODO I should check this in each activity whether the use is logged in or not,
//make seperate helper class for this checking

class SplashActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 1

    private val TAG = "__SplashActivity"


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
    private lateinit var userInfoViewModel:UserInfoViewModel
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
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
//            // The SafetyNet Attestation API is available.
//            Log.d(TAG, "onCreate: csafetynet attstation api available")
//
//            SafetyNet.getClient(this).attest("12345678911234567".toByteArray(), "AIzaSyDkpiDiYb84psGet7KKcqNG3DN3dgfyy18")
//                .addOnSuccessListener(this) {
//                    // Indicates communication with the service was successful.
//                    // Use response.getJwsResult() to get the result data.
//                    Log.d(TAG, "onCreate: success safety net")
//                }
//                .addOnFailureListener(this) { e ->
//                    // An error occurred while communicating with the service.
//                    if (e is ApiException) {
//                        // An error with the Google Play services API contains some
//                        // additional details.
//                        val apiException = e as ApiException
//
//                        // You can retrieve the status code using the
//                        // apiException.statusCode property.
//                    } else {
//                        // A different, unknown type of error occurred.
//                        Log.d(TAG, "Error: " + e.message)
//                    }
//                }
//        } else {
//            // Prompt user to update Google Play services.
//            Log.d(TAG, "onCreate: csafetynet attstation api not available")
//
//        }
        rcfirebaseAuth = FirebaseAuth.getInstance()

        userInfoViewModel = ViewModelProvider(this, UserInfoInjectorUtil.provideUserInjectorUtil(this)).get(UserInfoViewModel::class.java)

        //Start home activity
//        startActivity(new Intent(SplashActivity.this, MainActivity.class));
//         close splash activity
        if (checkPermission()) {
            firebaseAuthListener()
        } else {
            val i = Intent(this@SplashActivity, PermissionRequestActivity::class.java)
            startActivityForResult(i, PERMISSION_REQUEST_CODE)

//            startActivity(Intent(this, ActivityRequestPermission::class.java))
//            finish()
//            Log.i("SplashActivity", "permission requesting on  progress")
        }
//        rcfirebaseAuth.addAuthStateListener(rcAuthStateListener);
//
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
//        finish();
    }

    private fun firebaseAuthListener() {
        rcAuthStateListener =
            AuthStateListener { firebaseAuth ->
                user = firebaseAuth.currentUser
                //                    Task<GetTokenResult> idToken = FirebaseUser.getIdToken();
                if (user != null) {
                    //user is signed in
                    //                        Toast.makeText(this, "You are now signed in", Toast.LENGTH_SHORT).show();
                    onSignedInInitialize()


                } else {
                    // user is signed out
                    onSingnedOutcleanUp()


                    val i = Intent(this@SplashActivity, ActivityPhoneAuth::class.java)
                    startActivityForResult(i, RC_SIGN_IN)
                }
            }
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


    private fun initCrypto() {
        encryptor = EnCryptor()
        try {
            decryptor = Decryptor()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun onSignedInInitialize() {
//        getAuthToken()

        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var idToken = task.result?.token
                    // Send token to your backend via HTTPS

                    // ...

                    saveToken(idToken)

                    //check if we have a loggedInstatus in sharedPreference
                    val loggedIn = sharedPreferences.getBoolean("IS_LOGGEDIN", false)

                     if(!loggedIn){
                         //go to the activity after saving the token
                         val isNewUserInFirebase = isNewUserInFirebase()
                         if(isNewUserInFirebase){
                             Log.d(TAG, "onSignedInInitialize:checkIfNewUser Returned true")
                             startGetUserInfoAcitvity()
                         }else{

                             if(isNewUserInServer()){

                                 val i = Intent(this, GetInitialUserInfoActivity::class.java)
                                 startActivity(i)
                             }else{
                                 //already existing user in server
                                 saveToSharedPref(true)
                                 val i = Intent(this, MainActivity::class.java)
                                 startActivity(i)
                             }
                         }

                     }else{
                         //user logged in
                         Log.d(TAG, "onSignedInInitialize: userLogged in  ")
                         val i = Intent(this, MainActivity::class.java)
                         startActivity(i)
                     }

//                    generateEncryptedKey()
                } else {
                    // Handle error -> task.getException();
                }
            }


    }

    private fun onSingnedOutcleanUp() {}

    //This is called after OTP verification
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        var phoneNumber: String? = ""
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

//            IdpResponse response = IdpResponse.fromResultIntent(data);
//            if (resultCode == Activity.RESULT_OK) {
                phoneNumber = rcfirebaseAuth?.currentUser!!.phoneNumber
                 user = rcfirebaseAuth?.currentUser
                val uid = user!!.uid
                //                IdpResponse idpResponse = IdpResponse.fromResultIntent(data);


                //determine if the user who just signed in is an existing or new one
//               if(checkIfNewUser()){
//                   Log.d(TAG, "startGetUserInfoActivity")
//                   startGetUserInfoAcitvity()
//               }else{
                   onSignedInInitialize()


//               }

//                TODO crete new user in the firestore with the unique phone,firstName,lastName or google signin
//                Log.d(TAG, phoneNumber)
//                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show()
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(this, "Signed in cancelled", Toast.LENGTH_SHORT).show()
//                finish()
//            }
        }
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

    private fun isNewUserInServer(): Boolean {
        var status = false
        val userInfo = UserInfoDTO()

        userInfoViewModel.upload(userInfo).observe(this, Observer {
            it?.let { resource: Resource<Response<NetWorkResponse>?> ->
                val resMessage = resource.data?.body()?.message
                when (resource.status) {

                    Status.SUCCESS -> {

                        if (resMessage.equals(EUserResponse.NO_SUCH_USER)) { // there is no such user in server
                            Log.d(TAG, "checkIfNewUser: no such user")
                            //This is a new user
//                            val i = Intent(this, GetInitialUserInfoActivity::class.java)
//                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            //set userLoggedIn = false in shared preference

                            saveToSharedPref(false)

//                            startActivity(i)

                        }else if(resMessage.equals(EUserResponse.EXISTING_USER)){
                            Log.d(UserUploadHelper.TAG, "upload: user already exist")
//                            val i  = Intent(applicationContext, MainActivity::class.java)
//                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            //set userLogedIn = true in shared preferecce
                            saveToSharedPref(true)
                            status = true
//                            applicationContext.startActivity(i)

                        }
                        Log.d(TAG, "checkIfNewUser: success ${resource.data?.body()?.message}")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "checkIfNewUser: Loading")
                    }
                    else -> {
                        Log.d(TAG, "checkIfNewUser: else $resource")
                        Log.d(TAG, "checkIfNewUser:error ")
                    }


                }

            }
        })
        return status
    }

    private fun saveToSharedPref(b: Boolean) {
        sharedPreferences = getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGEDIN", b)
        editor.commit()
    }

    private fun startGetUserInfoAcitvity() {

        val i = Intent(this, GetInitialUserInfoActivity::class.java)
        startActivity(i)
//        finish()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        this.user = rcfirebaseAuth!!.currentUser
    }

    private fun checkPermission(): Boolean {
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        return sharedPreferences.getBoolean("isPermissionGiven",false)

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

    override fun onPostResume() {
        super.onPostResume()

        if (checkPermission()) {
            if(::rcAuthStateListener.isInitialized)
                 rcfirebaseAuth?.addAuthStateListener(rcAuthStateListener)
        }
    }
}