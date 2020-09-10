package com.nibble.hashcaller.view.ui


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.nibble.hashcaller.network.user.UserUploadHelper
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.utils.auth.Decryptor
import com.nibble.hashcaller.utils.auth.EnCryptor

import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.auth.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.utils.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel

import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec


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
    private  const val SHARED_PREFERENCE_TOKEN_NAME = "com.nibble.hashCaller.prefs"
    private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
    private lateinit var userInfoViewModel:UserInfoViewModel
//    private lateinit var skey:SecretKey
}
    override fun onPause() {
        super.onPause()
        rcfirebaseAuth?.removeAuthStateListener(rcAuthStateListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

//        getSupportActionBar().hide();

//        setContentView(R.layout.dummy_splash);
        rcfirebaseAuth = FirebaseAuth.getInstance()



        //Start home activity
//        startActivity(new Intent(SplashActivity.this, MainActivity.class));
//         close splash activity
        if (checkPermission()) {
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
        } else {
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

            editor.apply()


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

                    //go to the activity after saving the token
                    if(checkIfNewUser()){
                        startGetUserInfoAcitvity()
                    }else{

//                        val i = Intent(this, MainActivity::class.java)
//        i.putExtra("key", key)
//                        startActivity(i)
//        finish();
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
    }

    private fun checkIfNewUser(): Boolean {
        metadata = rcfirebaseAuth?.currentUser!!.metadata

        if (metadata?.creationTimestamp == metadata?.lastSignInTimestamp) {
            // The user is new, show them a fancy intro screen!
            Log.d(TAG, "new user signin")


//            startGetUserInfoAcitvity()
            return true;
        } else {
            // This is an existing user, show them a welcome back screen.
            Log.d(TAG, "existing user signin")
            //check the user primary information such as username and other fields are in the database
            userInfoViewModel = ViewModelProvider(this, UserInfoInjectorUtil.provideUserInjectorUtil(this)).get(UserInfoViewModel::class.java)
            val helper = UserUploadHelper(userInfoViewModel, this, applicationContext)
            helper.upload(UserInfoDTO())
            return false;
        }


    }

    private fun startGetUserInfoAcitvity() {

        val i = Intent(this, GetInitialUserInfoActivity::class.java)
        startActivity(i)
//        finish()
    }



    private fun checkPermission(): Boolean {
//        val permissionsUtil = PermissionsUtil(this)
//        return if (!permissionsUtil.checkPermissions()) {
//            //            startActivity(new Intent(this, ActivityRequestPermission.class));
//            //            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
//            false
//        } else true
//
////        //TODO check if contacts are uploaded
////        //check internet connection
////        boolean contactsUploaded = false;
////        if(!contactsUploaded){
////            ContactsUploder contactsUploder = new ContactsUploder(context);
////            contactsUploder.uploadContacts();
////        }
        return true


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
            rcfirebaseAuth?.addAuthStateListener(rcAuthStateListener)
        }
    }
}