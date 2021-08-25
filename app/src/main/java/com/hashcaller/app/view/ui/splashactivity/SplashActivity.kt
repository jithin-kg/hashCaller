package com.hashcaller.app.view.ui.splashactivity


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.auth.Decryptor
import com.hashcaller.app.utils.auth.EnCryptor
import com.hashcaller.app.utils.notifications.blockPreferencesDataStore
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.getstarted.GetStartedActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.*

//TODO I should check this in each activity whether the use is logged in or not,
//make seperate helper class for this checking

class SplashActivity : AppCompatActivity() {

    private val TAG = "__SplashActivity"

    private val RC_SIGN_IN = 1

    private  var _rcfirebaseAuth: FirebaseAuth? = null
    private  val rcfirebaseAuth get() =  _rcfirebaseAuth!!
    private  var _rcAuthStateListener: AuthStateListener? = null
    private  val  rcAuthStateListener get() =  _rcAuthStateListener!!
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
        private var _dataStoreViewModel: DataStoreViewmodel? = null
    }
    override fun onPause() {
        super.onPause()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        checkUserInfoInDatastore()

    }

    private fun checkUserInfoInDatastore() {
        lifecycleScope.launchWhenCreated {
            val wrapedKey =  booleanPreferencesKey(PreferencesKeys.USER_INFO_AVIALABLE_IN_DB)
            val tokenFlow: Flow<Boolean> = blockPreferencesDataStore.data.map {
                it[wrapedKey]?:false
            }
            if(tokenFlow.first()){
                //user info available
                startMainActivity()

            }else {
                //user info not available
                onSingnedOutcleanUp()
            }
        }

    }

    private fun initViewModel() {
        _dataStoreViewModel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(
            DataStoreViewmodel::class.java)

    }


    private fun startMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i)
        finish()
    }

    private fun onSingnedOutcleanUp() {

        val i = Intent(this@SplashActivity, GetStartedActivity::class.java)
//        startActivityForResult(i, RC_SIGN_IN)
        startActivity(i)
        finish()
    }











    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
    }






    override fun onDestroy() {
        viewModelStore.clear()

        super.onDestroy()


    }

    override fun onPostResume() {
        super.onPostResume()

    }
}