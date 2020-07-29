package com.nibble.hashcaller.view.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.utils.auth.DeCryptor
import com.nibble.hashcaller.utils.auth.EnCryptor
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class SplashActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 1
    private val TAG = "__SplashActivity"

    private var rcfirebaseAuth: FirebaseAuth? = null
    private var rcAuthStateListener: AuthStateListener? = null
//    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private val userCollectionRef: CollectionReference = db.collection("Users")
    var user: FirebaseUser? = null
    private lateinit var encryptor: EnCryptor
    private lateinit var decryptor: DeCryptor
    private val SAMPLE_ALIAS = "MYALIAS"
    var sharedPreferences: SharedPreferences? = null
companion object{
    private const val KEY_ALIAS = "MYKeyAlias"
    private const val KEY_STORE = "AndroidKeyStore"
    private const val CIPHER_TRANSFORMATION = "AES/CBC/NoPadding"
}
    override fun onPause() {
        super.onPause()
        rcfirebaseAuth!!.removeAuthStateListener(rcAuthStateListener!!)
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
                        getAuthToken()
                        Log.i("SplashActivity", "logged in")
                    } else {
                        // user is signed out
                        onSingnedOutcleanUp()

                        //TODO automatic signin auto detecting the phone number and comparing if the perticular user exist in firestore
                        //                        startActivityForResult(
                        //                                AuthUI.getInstance()
                        //                                        .createSignInIntentBuilder()
                        //                                        .setIsSmartLockEnabled(false)
                        //                                        .setLogo(R.drawable.real_caller_logo)
                        //                                        .setTheme(R.style.AppTheme)
                        //                                        .setTosAndPrivacyPolicyUrls(
                        //                                                "https://joebirch.co/terms.html",
                        //                                                "https://joebirch.co/privacy.html")
                        //                                        .setAvailableProviders(Arrays.asList(
                        //                                                new AuthUI.IdpConfig.PhoneBuilder().build()))
                        //                                        .build(),
                        //                                RC_SIGN_IN);
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

    private fun getAuthToken() {
        user!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result!!.token
                    // Send token to your backend via HTTPS
                    Log.d(TAG, "onComplete: $idToken")
                    // ...

                    saveToken(idToken)

//                    generateEncryptedKey()
                } else {
                    // Handle error -> task.getException();
                }
            }
    }

    private fun saveToken(idToken: String?) {
        initCrypto()
        val encryptedToken = encryptText(idToken)
        Log.d(TAG, "saveToken: $encryptedToken")
        sharedPreferences = applicationContext.getSharedPreferences(
            "TOKEN",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences?.edit()
        editor?.putString("token", encryptedToken)
        editor?.apply()

    }

    private fun initCrypto() {
        encryptor = EnCryptor()
        try {
            decryptor = DeCryptor()
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
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
//        finish();
    }

    private fun onSingnedOutcleanUp() {}

    //This is called after OTP verification
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        Log.d("SplashActivity", "onactivity Result")
        var phoneNumber: String? = ""
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("SplashActivity", "requestCode $requestCode")
        Log.d("SplashActivity", "resultCode $resultCode")
        if (requestCode == RC_SIGN_IN) {


//            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == Activity.RESULT_OK) {
                phoneNumber = rcfirebaseAuth!!.currentUser!!.phoneNumber
                val user = rcfirebaseAuth!!.currentUser
                val uid = user!!.uid
                //                IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
                Log.d("SplashActiviy", "The UID is $uid")

                //determine if the user who just signed in is an existing or new one
                val metadata = rcfirebaseAuth!!.currentUser!!.metadata
                Log.d("META_DATA", "metaData: " + metadata!!.creationTimestamp)
                Log.d("META_DATA", "metaData: " + metadata.lastSignInTimestamp)
                if (metadata.creationTimestamp == metadata.lastSignInTimestamp) {
                    // The user is new, show them a fancy intro screen!
                    Log.d("SplashActiviy", "new user signin")
                    //                    onSignedInInitialize();
                    startGetUserInfoAcitvity()
                } else {
                    // This is an existing user, show them a welcome back screen.
                    Log.d("SplashActiviy", "existing user signin")
                    onSignedInInitialize()
                }

//                TODO crete new user in the firestore with the unique phone,firstName,lastName or google signin
                Log.d("SplashActiviy", phoneNumber)
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Signed in cancelled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startGetUserInfoAcitvity() {
//        val i = Intent(this, GetInitialUserInfoActivity::class.java)
//        startActivity(i)
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


    fun encryptText(token: String?):String {
        var encryptedToken = ""
        try {
            val encryptedText: ByteArray? = token?.let {
                encryptor
                    .encryptText(SAMPLE_ALIAS, it)
            }

            encryptedToken= Base64.encodeToString(encryptedText, Base64.DEFAULT)
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
        return encryptedToken
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.d("SplashActivity", "postResume")
        if (checkPermission()) {
            rcfirebaseAuth!!.addAuthStateListener(rcAuthStateListener!!)
        }
    }
}