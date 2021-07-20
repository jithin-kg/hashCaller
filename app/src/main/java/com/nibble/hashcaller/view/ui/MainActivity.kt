package com.nibble.hashcaller.view.ui

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityMainBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.datastore.DataStoreViewmodel.Companion.PERMISSION__ONLY_GIVEN
import com.nibble.hashcaller.datastore.DataStoreViewmodel.Companion.USER_INFO_AND_PERMISSION_GIVEN
import com.nibble.hashcaller.datastore.DataStoreViewmodel.Companion.USER_INFO_ONLY_GIVEN
import com.nibble.hashcaller.datastore.PreferencesKeys.Companion.USER_INFO_AVIALABLE_IN_DB
import com.nibble.hashcaller.utils.PermisssionRequestCodes
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_CONTACTS
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_SMS
import com.nibble.hashcaller.utils.PermisssionRequestCodes.Companion.ROLE_SCREENING_APP_REQUEST_CODE
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.view.ui.auth.PermissionRequestActivity
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.blockConfig.BlockConfigFragment
import com.nibble.hashcaller.view.ui.call.CallFragment
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import com.nibble.hashcaller.view.ui.call.spam.SpamCallsActivity
import com.nibble.hashcaller.view.ui.contacts.ContactsContainerFragment
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.extensions.startPermissionRequestActivity
import com.nibble.hashcaller.view.ui.getstarted.GetStartedActivity
import com.nibble.hashcaller.view.ui.hashworker.HasherViewmodel
import com.nibble.hashcaller.view.ui.manageblock.BlockManageActivity
import com.nibble.hashcaller.view.ui.notifications.ManageNotificationsActivity
import com.nibble.hashcaller.view.ui.profile.ProfileActivity
import com.nibble.hashcaller.view.ui.settings.SettingsActivity
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.view.ui.sms.search.SMSSearchFragment
import com.nibble.hashcaller.view.ui.sms.spam.SpamSMSActivity
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.DefaultFragmentManager
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.getDecodedBytes
import com.nibble.hashcaller.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.coroutines.launch
import java.security.*


/**
 * This is a extension function which set the default fragment
 * for dynamically hiding / showing a fragment
 */


class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    // flag that restarts checking capabilities dialog, after user enables manifest permissions
    // via app settings page
    private var checkCapabilitiesOnResume = false
    private lateinit var toolbar: Toolbar
    private lateinit var  tabLayout: TabLayout
    private val viewPager: ViewPager? = null
    var fab: FloatingActionButton? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var hashedNumbersViewmodel : HasherViewmodel
    private lateinit var callFragment: CallFragment
    private lateinit var smsFragment: SMSContainerFragment
    private lateinit var fullScreenFragment: FullscreenFragment
    //    private lateinit var blockConfigFragment: BlockConfigFragment
    private lateinit var contactFragment: ContactsContainerFragment
//    private lateinit var searchFragment: SearchFragment
    private lateinit var blockListFragment: BlockConfigFragment
    private lateinit var ft: FragmentTransaction
    private lateinit var dialerFragment: DialerFragment
    private lateinit var smsSearchFragment: SMSSearchFragment

    private lateinit var header:View
    private lateinit var headerImgView:de.hdodenhof.circleimageview.CircleImageView
    private lateinit var firstLetterView:TextView
    private lateinit var menu:Menu
    private lateinit var  menuMessage:MenuItem
    private lateinit var  menuContacts:MenuItem
    private lateinit var  menuCalls:MenuItem
    private lateinit var  menuSearch:MenuItem
//    private lateinit var activeFragment:Fragment

//    var  searchFragment: SearchFragment? = null


    //    private lateinit var drawerLayout: DrawerLayout
    //    private lateinit var navigationView:NavigationView
    private lateinit var actionbarDrawertToggle: ActionBarDrawerToggle
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var savedState:Bundle? = null
    private var metadata:FirebaseUserMetadata?= null
    private var dataStoreViewModel : DataStoreViewmodel? = null
    ///////////////////////////splash ////////////////////////////
    private val RC_SIGN_IN = 1




//    private  var _rcfirebaseAuth: FirebaseAuth? = null
//    private  val rcfirebaseAuth get() =  _rcfirebaseAuth!!
//    private  var _rcAuthStateListener: FirebaseAuth.AuthStateListener? = null
//    private  val  rcAuthStateListener get() =  _rcAuthStateListener!!

    private  var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private var isDarkThemeOn = false
    private lateinit var mainViewmodel: MainViewmodel

    ///////////////////////////// end //////////////////////////////////////////
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var hashCallerViewModel: HashCallerViewModel

//    var contactsUploadWorkManager: ContactsUploadWorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        mainViewmodel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(MainViewmodel::class.java)
        isDarkThemeOn =  isDarkThemeOn()
        savedState = savedInstanceState
//        setTheme(R.style.splashScreenTheme)
        initDataStoreViewmodel()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        initMainActivityComponents()

        manageSavedInstanceState(savedInstanceState)
        if(savedInstanceState!= null){
//            addAllFragments()
            setFragmentsFromSavedInstanceState(savedInstanceState)
        }else {
//            setFragmentsFromSavedInstanceState(savedInstanceState)
        }
//
//        rcfirebaseAuth = FirebaseAuth.getInstance()
//        initViewModel()
        checkUserInfoAvaialbleInDb(savedInstanceState)

//        observeHashedNumbersTable()


    }

    private fun observeHashedNumbersTable() {
        hashedNumbersViewmodel.hashedNumbersLiveData.observe(this, Observer {
//            hashedNumbersViewmodel.doWork()
        })
    }

    private fun startHashWorker() {
//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//
//        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(HashWorker::class.java)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
    }

    private fun checkUserInfoAvaialbleInDb(savedInstanceState: Bundle?) {
        dataStoreViewModel?.getPermissionAndUserInfo(USER_INFO_AVIALABLE_IN_DB, this)?.observe(this, Observer {
           when(it){
               USER_INFO_AND_PERMISSION_GIVEN ->{

                   firebaseAuthListener()
//                   val ft = supportFragmentManager.beginTransaction()
////                    ft.hide(fullScreenFragment)
//                   ft.remove(fullScreenFragment)
//                   ft.show(callFragment)
//                   ft.commit()

                   binding = ActivityMainBinding.inflate(layoutInflater)
                    setContentView(binding.root)
                   initMainActivityComponents()

                   if(savedInstanceState==null){
                       addAllFragments()
                   }else{
                       val ft = supportFragmentManager.beginTransaction()
                       setDefaultFragment(DefaultFragmentManager.id)
                       ft.detach(callFragment)
                       ft.attach(callFragment)
                       ft.detach(smsFragment)
                       ft.attach(smsFragment)
                       ft.detach(contactFragment)
                       ft.attach(contactFragment)
//                       ft.detach(searchFragment)
//                       ft.attach(searchFragment)
                       ft.detach(blockListFragment)
                       ft.attach(blockListFragment)
                       ft.detach(smsSearchFragment)
                       ft.attach(smsSearchFragment)
                       ft.commit()
//                       setFragmentsFromSavedInstanceState(savedInstanceState)
//                       addAllFragments()

                   }
//                   binding.bottomNavigationView.beVisible()
               }
               PERMISSION__ONLY_GIVEN -> {
                   onSingnedOutcleanUp()
               }
               USER_INFO_ONLY_GIVEN -> {
                  startPermissionRequestActivity()
                   finish()
               }
               else -> {
                   onSingnedOutcleanUp()
               }
           }

//            if(it){
//                if(!checkPermission()){
//                    val i = Intent(this, PermissionRequestActivity::class.java)
////            startActivityForResult(i, PERMISSION_REQUEST_CODE)
//                    startActivity(i)
//                    finish()
//                }else {
//
////                    initMainActivityComponents()
////                    setTheme(R.style.AppTheme)
//                    firebaseAuthListener()
//                    val ft = supportFragmentManager.beginTransaction()
////                    ft.hide(fullScreenFragment)
//                    ft.remove(fullScreenFragment)
//                    ft.show(callFragment)
//                    ft.commit()
//                    binding.bottomNavigationView.beVisible()
//                }
//            }else{
//                onSingnedOutcleanUp()
//            }
        })
//        userInfoViewModel.getUserInfoFromDb().observe(this, Observer {
//            if(it!=null){
//                /**
//                 * important set theme only after user info is avialable in db, (so then only the view will show)
//                 */
//
//                if(!checkPermission()){
//                    val i = Intent(this, PermissionRequestActivity::class.java)
////            startActivityForResult(i, PERMISSION_REQUEST_CODE)
//                    startActivity(i)
//                    finish()
//                }else {
//
////                    initMainActivityComponents()
////                    setTheme(R.style.AppTheme)
//                    firebaseAuthListener()
//                    val ft = supportFragmentManager.beginTransaction()
////                    ft.hide(fullScreenFragment)
//                    ft.remove(fullScreenFragment)
//                    ft.show(callFragment)
//                    ft.commit()
//                    binding.bottomNavigationView.beVisible()
//                }
//
//            }else{
//                onSingnedOutcleanUp()
//            }
//        } )
//        dataStoreViewModel?.getToken()?.observe(this, Observer {
//            if(!it.isNullOrEmpty()){
////                initMainActivityComponents(savedState)
//                callback(true)
//            }else{
//                callback(false)
//            }
//        })
    }


    private fun firebaseAuthListener() {
        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        if(user ==null){
            onSingnedOutcleanUp()
        }else{
            tokenHelper = TokenHelper(user)
        }
//        _rcAuthStateListener =
//            FirebaseAuth.AuthStateListener { firebaseAuth ->
//                user = firebaseAuth.currentUser
//                //                    Task<GetTokenResult> idToken = FirebaseUser.getIdToken();
//                if (user != null) {
//                    //user is signed in
//                    checkUserInfoInDb()
//
//                } else {
//                    // user is signed out
//                    onSingnedOutcleanUp()
//
//                }
//            }
    }

    private fun saveTokenIfConnected() {
//        user?.getIdToken(true)
//            ?.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    var token = task.result?.token
//                    // Send token to your backend via HTTPS
//                    if(!token.isNullOrEmpty()){
//                        dataStoreViewModel?.getEncryptedStr(token.toString())?.observe(this, Observer {encodeTokenString ->
//                            dataStoreViewModel?.saveTokenViewmodelScope(encodeTokenString)
//                        })
//
//                    }
//
//                }else{
//                    Log.d(ActivityVerifyOTP.TAG, "onSignedInInitialize:${task.exception}")
//                }
//            }
    }

    private fun onSingnedOutcleanUp() {
        
        val i = Intent(this, GetStartedActivity::class.java)
//        startActivityForResult(i, RC_SIGN_IN)
        startActivity(i)
        finish()
    }
    private fun checkUserInfoInDb() {
        dataStoreViewModel?.getToken()?.observe(this, Observer {
            if(!it.isNullOrEmpty()){
//                initMainActivityComponents(savedState)

            }else{
                onSingnedOutcleanUp()
            }
        })
    }
    private fun initMainActivityComponents() {

        hideKeyboard(this)
//        setStatusBarColor(this)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        initHashCallerViewmodel()
        initColors()
        setAllMenuItems()


//        listenUiEvents()
//        requestAlertWindowPermission()
        Log.d(TAG, "onCreate: is dark theme on ${isDarkThemeOn()}")
        val c = ContextCompat.getColor(applicationContext, R.color.textColor);

//        initViewModel()
        setupNavigationDrawer()
        initHeaderView()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if(!this. isScreeningRoleHeld()){
//                requestScreeningRole()
//
//            }
//        }
        setBottomSheetListener()

//        mangeCipherInSharedPref()
        observeUserInfoLiveData()
//        setupContactUploadWork()
        initListeners()

        setupBottomMenuIconsBasedOnTheme()
        initViewModel()
        observeUserInfo()


    }

    private fun initColors() {
        hashCallerViewModel.initColors()

    }

    private fun initHashCallerViewmodel() {
//        hashCallerViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(HashCallerViewModel::class.java)
        hashCallerViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(HashCallerViewModel::class.java)

    }

    private fun initDataStoreViewmodel() {
        dataStoreViewModel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(
            DataStoreViewmodel::class.java)
    }

    private fun initListeners() {
        headerImgView.setOnClickListener(this)
    }

    private fun initHeaderView() {
        header = binding.navView.getHeaderView(0)

        headerImgView =  header.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgViewAvatarDrawer)
        firstLetterView = header.findViewById<TextView>(R.id.tvFirstLetterMain)

    }

    private fun observeUserInfo() {

        userInfoViewModel.userInfoLivedata.observe(this, Observer {
            if (it != null) {
                    try {
                        if(it.firstname.isNotEmpty()){
                            val fLetter = formatPhoneNumber(it.firstname)[0].toString()
                        }

                val fullName = header.findViewById<TextView>(R.id.tvNavDrawerName)
                fullName.text = "${it.firstname} ${it.lastName}"
                if(!it.photoURI.isNullOrEmpty()){
                    headerImgView.setImageBitmap(getDecodedBytes(it.photoURI))
                    firstLetterView.beInvisible()
                }else{
                    firstLetterView.beVisible()
                }
                    }catch (e:Exception){
                        Log.d(TAG, "observeUserInfo: $e")
                        toast("Unable to get user name")
                    }

            }
        })
    }


    private fun requestAlertWindowPermission() {
        // Show alert dialog to the user saying a separate permission is needed
        if(!canDrawOverlays(applicationContext)){
            val myIntent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(myIntent)
        }

    }

    override fun onDestroy() {
        viewModelStore.clear()
//        if(_rcfirebaseAuth!=null && _rcAuthStateListener!=null){
//            _rcfirebaseAuth!!.removeAuthStateListener(_rcAuthStateListener!!)
//        }
//        _rcAuthStateListener = null
//        _rcfirebaseAuth = null
        dataStoreViewModel = null
//        _userInfoViewModel = null
        super.onDestroy()


    }
    private fun listenUiEvents() {
//       uiEvent.observe(this, {
//            when (it) {
//                is PermissionDenied -> {
//                    checkCapabilitiesOnResume = true
//                    // This will display a dialog directing them to enable the permission in app settings.
//                    AppSettingsDialog.Builder(this).build().show()
//                }
//                is PhoneManifestPermissionsEnabled -> {
//                    // now we can load phone dialer capabilities requests
//                    capabilitiesRequestor.invokeCapabilitiesRequest()
//                }
//                else -> {
//                    // NOOP
//                }
//            }
//        })
    }


//    override fun onCreateContextMenu(menu: ContextMenu, v: View,
//                                     menuInfo: ContextMenu.ContextMenuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.sms_container_menu, menu)
//    }

    @SuppressLint("SetTextI18n")
    private fun observeUserInfoLiveData() {
//
//        this.userInfoViewModel.userInfo.observe(this, Observer {
//            Log.d(TAG, "observeUserInfoLiveData: userinfo is $it")
//            when (it) {
//                null -> {
//                    userInfoViewModel.getUserInfoFromServer().observe(this, Observer {
////                        userInfoViewModel.insertUserInfo(it)
//                    })
//                }
//
//            }
////            if (it != null)
////                if (!it.firstname.isNullOrEmpty()) {
//////                val header =navigationView.getHeaderView(0)
//////                header.tvNavDrawerName.text = it.firstname + " " + it.lastName
////                }
//
//        })
    }

    fun showSnackBar(message: String){
        val sbar = Snackbar.make(cordinateLyoutMainActivity, message, Snackbar.LENGTH_SHORT)
        sbar.setAction("Action", null)
        sbar.anchorView = binding.bottomNavigationView
        sbar.show()

    }
    private fun setupContactUploadWork() {
//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
//
//        val request2 = OneTimeWorkRequest.Builder(ContactsAddressLocalWorker::class.java)
//            .build()
//        WorkManager.getInstance().enqueue(request2)
//
//        val request =
//            OneTimeWorkRequest.Builder(ContactsUploadWorker::class.java)
//                .setConstraints(constraints)
//                .build()
//        WorkManager.getInstance().enqueue(request)

    }

    private fun manageSavedInstanceState(savedInstanceState: Bundle?) {
//        this.fullScreenFragment = FullscreenFragment()
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: savedInstanceState is null")
            ft = supportFragmentManager.beginTransaction()

            this.smsFragment = SMSContainerFragment()
//            this.blockConfigFragment = BlockConfigFragment()
            this.contactFragment = ContactsContainerFragment()
            this.callFragment = CallFragment()

            this.dialerFragment = DialerFragment()
//            this.searchFragment = SearchFragment()
            this.blockListFragment = BlockConfigFragment()
            smsSearchFragment = SMSSearchFragment.newInstance()
//            this.searchFragment =  SearchFragment.newInstance()
//            setInstancesInApp()

//            fabBtnShowDialpad.visibility = View.GONE
//            syncSpamList()


            //set the default fragment
            setTheDefaultFragment()
//            addAllFragments()

        }else{

//            setFragmentsFromSavedInstanceState(savedInstanceState)
//            this.ft = supportFragmentManager.beginTransaction()

        }
    }

    private fun initViewModel() {
        userInfoViewModel = ViewModelProvider(
            this, MainActivityInjectorUtil.provideUserInjectorUtil(
                applicationContext,
                tokenHelper
            )
        ).get(
            UserInfoViewModel::class.java
        )
        hashedNumbersViewmodel = ViewModelProvider(this,
            MainActivityInjectorUtil.provideHashINjectorUtil(applicationContext,
                tokenHelper)).get(HasherViewmodel::class.java)


    }



    private fun setupNavigationDrawer() {
        actionbarDrawertToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.start, R.string.close)
//
        binding.drawerLayout.addDrawerListener(actionbarDrawertToggle)
        actionbarDrawertToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)


    }

    private fun mangeCipherInSharedPref() {
//        if(!isCipherInSharedPreferences()){
////            KeyManager.setCipherInSharedPreferences(this)
//        }
    }


    private fun isCipherInSharedPreferences(): Boolean {
        val isKeyAvailable = KeyManager.isKeyStored(this)
        if(isKeyAvailable) Log.d(TAG, "isCipherInSharedPreferences: key availabl")
        else
            Log.d(TAG, "isCipherInSharedPreferences: key not available")
        return  isKeyAvailable

    }

    private fun setFragmentsFromSavedInstanceState(savedInstanceState: Bundle) {

        Log.d(TAG, "setFragmentsFromSavedInstanceState: ")
//        this.fullScreenFragment = supportFragmentManager.getFragment(savedInstanceState, "fullScreenFragment") as FullscreenFragment
        this.callFragment = supportFragmentManager.getFragment(savedInstanceState, "callFragment") as CallFragment
        this.smsFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "messagesFragment"
        ) as SMSContainerFragment
//        this.blockConfigFragment = supportFragmentManager.getFragment(savedInstanceState,"blockConfigFragment") as BlockConfigFragment
        this.contactFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "contactFragment"
        ) as ContactsContainerFragment
        this.dialerFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "dialerFragment"
        ) as DialerFragment

//        this.searchFragment = supportFragmentManager.getFragment(
//            savedInstanceState,
//            "searchFragment"
//        ) as SearchFragment
        this.blockListFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "blockListFragment"
        ) as BlockConfigFragment

        this.smsSearchFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "smsSearchFragment"
        ) as SMSSearchFragment




//        if(supportFragmentManager.getFragment(savedInstanceState, "searchFragment") !=null){
//            this.searchFragment = supportFragmentManager.getFragment(savedInstanceState, "searchFragment") as SearchFragment
//
//        }else{
//            this.searchFragment = SearchFragment.newInstance()
//        }

    }

    private fun setBottomSheetListener(){
        binding. bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            var fragment: Fragment
            val selectedFragment = ""
            when (menuItem.itemId) {
                R.id.bottombaritem_messages -> {
                    showMessagesFragment()
                    Log.d(TAG, "setBottomSheetListener: show sms clicked")
//                    fabBtnShowDialpad.visibility = View.GONE
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottombaritem_calls -> {
                    showCallFragment()
//                    fabBtnShowDialpad.visibility = View.VISIBLE
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottombaritem_contacts -> {
                    showContactsFragment()
//                    fabBtnShowDialpad.visibility = View.GONE
                    return@OnNavigationItemSelectedListener true
                }
//                R.id.bottombaritem_search -> {
////                    showBlockConfigFragment()
////                    fabBtnShowDialpad.visibility = View.GONE
//                    showSearchFragment()
//                    return@OnNavigationItemSelectedListener true
//                }
                R.id.bottombaritem_blockList -> {
                    showBlockConfigFragment()
                    return@OnNavigationItemSelectedListener true
                }
//
            }
            false
        })
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: ")

        supportFragmentManager.putFragment(outState, "callFragment", this.callFragment)
//        supportFragmentManager.putFragment(outState, "fullScreenFragment", this.fullScreenFragment)
        supportFragmentManager.putFragment(outState, "contactFragment", this.contactFragment)
        supportFragmentManager.putFragment(outState, "dialerFragment", this.dialerFragment)
        supportFragmentManager.putFragment(outState, "messagesFragment", this.smsFragment)
//        supportFragmentManager.putFragment(outState, "searchFragment", this.searchFragment)
        supportFragmentManager.putFragment(outState, "blockListFragment", this.blockListFragment)
        supportFragmentManager.putFragment(outState, "smsSearchFragment", this.smsSearchFragment)




//        supportFragmentManager.putFragment(outState,"blockConfigFragment", this.blockConfigFragment)
//        if(this.searchFragment!=null)
//            if(this.searchFragment?.isAdded!!)
//                supportFragmentManager.putFragment(outState,"searchFragment", this.searchFragment!!)
//        outState.putInt("AStringKey", )
////        outState.putString("AStringKey2", variableData2)
//        val p: Parcelable? = callFragment.saveAllState()
//        if (p != null) {
////            outState.putParcelable(FragmentActivity.FRAGMENTS_TAG, p)
//        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "onRestoreInstanceState: ")
//        variableData = savedInstanceState.getInt("AStringKey")
//        variableData2 = savedInstanceState.getString("AStringKey2")
    }
    private fun syncSpamList() {
        val list = CountrycodeHelper(applicationContext).getCountrycode()
//        val spamSyncRepository = SpamSyncRepository()
//        SpamSyncManager.sync(list, spamSyncRepository, this)

    }



    /**
     * This function set the default fragment status of each fragment
     */
    private fun setTheDefaultFragment() {
//        contactFragment.isDefaultFgmnt = true
//        if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_FULL_FRAGMENT){
//            fullScreenFragment.isDefaultFgmnt = true
//        }else

            if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_CALL_FRAGMENT){
            callFragment.isDefaultFgmnt = true
        }
        else if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_MESSAGES_FRAGMENT){
            smsFragment.isDefaultFgmnt = true
        }
        else if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_CONTACT_FRAGMENT){
            contactFragment.isDefaultFgmnt = true
        }




//        else if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_BLOCK_FRAGMENT){
//            blockConfigFragment.isDefaultFgmnt = true
//        }

        else{
            dialerFragment.isDefaultFgmnt = true
        }
    }

    fun showDialerFragment() {

        val ft = supportFragmentManager.beginTransaction()
        callFragment.clearMarkeditems()
        smsFragment.clearMarkeditems()

        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }

        if (dialerFragment.isAdded) { // if the fragment is already in container

            ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            mainViewmodel.setActiveFragment(dialerFragment)
            ft.show(dialerFragment)
            dialerFragment.showDialPad()
            bottomNavigationView.beGone()
        }
        // Commit changes
//        ft.addToBackStack("test")
        ft.commit()
    }
    //called from dialerfragment, important call this function only from dialer fragment
    fun hideBottomNav(){
//        Log.d(TAG, "hideBottomNav: saved instance state is $savedState")
        if(savedState!=null && dialerFragment.isVisible){
            Log.d(TAG, "hideBottomNav: dialer frargment is visible")
            bottomNavigationView.beGone()
        }

    }


    //    private void onSingnedOutcleanUp() {
    //        mUserName = "Anonymous";
    //
    //    }
    //    private void onSignedInInitialize(String displayName) {
    //        mUserName = displayName;
    //       //TODO load main activiy content only after succesfull login
    //        checkPermission();
    //        loadMainActivity();
    //
    //    }
    private fun loadMainActivity() {}
    fun removeSearchFragment(){
//        ft = supportFragmentManager.beginTransaction()
//        ft.hide(this.searchFragment)
//        ft.remove(this.searchFragment).commit()
//        ft.commit()

    }

    /**
     * adds fragment to frame layout after search fragment closes
     */
    fun addFragmentsAgain(){
//        setTheDefaultFragment()
//        DefaultFragmentManager.defaultFragmentToShow = 2
//        addAllFragments()
//        val actionRestart =
//            findViewById<View>(R.id.bottombaritem_calls)




    }
    fun addAllFragments() {


        ft = supportFragmentManager.beginTransaction()

        setDefaultFragment(DefaultFragmentManager.id)

//        ft.add(R.id.frame_fragmentholder, fullScreenFragment)
//        hideThisFragment(ft, callFragment, fullScreenFragment)

        ft.add(R.id.frame_fragmentholder, callFragment)
        hideThisFragment(ft, callFragment, callFragment)

        ft.add(R.id.frame_fragmentholder, dialerFragment)
        hideThisFragment(ft, dialerFragment, dialerFragment)

        ft.add(R.id.frame_fragmentholder, smsFragment)
        hideThisFragment(ft, smsFragment, smsFragment)


//        bottomNavigationView!!.selectedItemId = R.id.bottombaritem_calls
        ft.add(R.id.frame_fragmentholder, contactFragment)
        hideThisFragment(ft, contactFragment, contactFragment)

//        ft.add(R.id.frame_fragmentholder, searchFragment)
//        hideThisFragment(ft, searchFragment, searchFragment)

        ft.add(R.id.frame_fragmentholder, blockListFragment)
        hideThisFragment(ft, blockListFragment, blockListFragment)

        ft.add(R.id.frame_fragmentholder, smsSearchFragment)
        hideThisFragment(ft, smsSearchFragment, smsSearchFragment)





//        ft.add(R.id.frame_fragmentholder, blockConfigFragment)
//        hideThisFragment(ft, blockConfigFragment, blockConfigFragment)

//        fabBtnShowDialpad.visibility = View.VISIBLE

        ft.commit()

    }

    private fun setAllMenuItems() {
        menu = binding.bottomNavigationView.menu
        menuMessage = menu.findItem(R.id.bottombaritem_messages)
        menuContacts = menu.findItem(R.id.bottombaritem_contacts)
        menuCalls = menu.findItem( R.id.bottombaritem_calls )
//        menuSearch = menu.findItem(R.id.bottombaritem_search)


//        menuMessage = menu.findItem(R.id.bottombaritem_messages)

    }

    /**
     * This function hides a fragment if it is set as default fragment
     */
    private fun hideThisFragment(
        ft: FragmentTransaction,
        fragment: Fragment,
        fmnt: IDefaultFragmentSelection
    ) {
        if(!fmnt.isDefaultFgmnt){
            ft.hide(fragment)
        }else {
           mainViewmodel.setActiveFragment(fragment)
        }

    }

    private fun setDefaultFragment(idValue: Int) {
//        bottomNavigationView.selectedItemId = R.id.bottombaritem_calls
        binding.bottomNavigationView.selectedItemId = idValue
    }

    private fun showBlockConfigFragment() {
        val ft = supportFragmentManager.beginTransaction()
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        if(blockListFragment.isAdded){
            ft.show(blockListFragment)
            mainViewmodel.setActiveFragment(blockListFragment)
        }

        ft.commit()
    }

    fun showContactsFragment() {
        toggleBottomMenuIcons(showContactsFragment = true)


//        showDialPad()
        val ft = supportFragmentManager.beginTransaction()

        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        if (contactFragment.isAdded) { // if the fragment is already in container
            ft.show(contactFragment)
            mainViewmodel.setActiveFragment(contactFragment)
//            unMarkItems()

            smsFragment.showSearchView()
        }
//         if(searchFragment!=null)
//             if(searchFragment!!.isAdded){
//                 ft.hide(searchFragment!!)
//             }
//        // Commit changes
        /**
         * Managing contacts uploading/Syncing by ContactsUPloadWorkManager
         */
        val intent = intent
        intent.getByteArrayExtra("key")
//        val request = OneTimeWorkRequest.Builder(ContactsUploadWorker::class.java)
//            .build()
//        WorkManager.getInstance().enqueue(request)
//
        ft.commit()
    }
    fun showSMSSearchFragment() {
        val ft = supportFragmentManager.beginTransaction()
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }

        if (smsSearchFragment.isAdded) { // if the fragment is already in container
            ft.show(smsSearchFragment)
            mainViewmodel.setActiveFragment(smsSearchFragment)
        }
//
        ft.commit()
        binding.bottomNavigationView.beGone()
        binding.navView.beGone()
    }
    fun showSearchFragment() {
        toggleBottomMenuIcons(showSearchFragment = true)
        val ft = supportFragmentManager.beginTransaction()

        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }

//        if (searchFragment.isAdded) { // if the fragment is already in container
//            ft.show(searchFragment)
//            mainViewmodel.setActiveFragment(searchFragment)
//        }
        val intent = intent
        intent.getByteArrayExtra("key")
//
        ft.commit()
    }

    fun showCallFragment() {
        toggleBottomMenuIcons(showCallsFragment = true)

        val ft = supportFragmentManager.beginTransaction()
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        if(callFragment.isAdded){
//            fabBtnShowDialpad.visibility = View.VISIBLE

            ft.show(callFragment)
            mainViewmodel.setActiveFragment(callFragment)
            binding.bottomNavigationView.beVisible()
        }

        // Commit changes
        ft.commit()
    }

     fun showMessagesFragment() {

//        toggleBottomMenuIcons(showMessageFragment = true)
        val ft = supportFragmentManager.beginTransaction()
//        if( mainViewmodel.getActiveFragment() == smsSearchFragment){
//            smsSearchFragment.
//        }
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        if (smsFragment.isAdded) { // if the fragment is already in container
//            ft.addToBackStack(messagesFragment.javaClass.name)
            ft.show(smsFragment)
            mainViewmodel.setActiveFragment(smsFragment)


//            setDefaultFragment(R.id.bottombaritem_messages)
        binding.bottomNavigationView.beVisible()
        binding.navView.beVisible()
        }
        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//            unMarkItems()
//            messagesFragment.showSearchView()
//
//        }
        // Hide fragment C

//        if (contactFragment.isAdded) {
//            ft.hide(contactFragment)
//        }
//        if (searchFragment.isAdded) {
//            ft.hide(searchFragment)
//        }

//        if (callFragment.isAdded) {
////            callFragment.clearMarkeditems()
//            ft.hide(callFragment)
//        }

//        if (callFragment.isAdded) {
//            ft.hide(callFragment)
//        }
//        if(dialerFragment.isAdded){
//            ft.hide(dialerFragment)
//        }


        // Commit changes
        ft.commit()
    }
    private fun setupBottomMenuIconsBasedOnTheme() {
//        if (isDarkThemeOn) {
////            Log.d(TAG, "setupBottomMenuIconsBasedOnTheme: isDarkThemeOn:true")
////            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_home_4_line)
////            menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_line)
////            menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_line)
////            menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_line)
//
//
//        }else {
//            Log.d(TAG, "setupBottomMenuIconsBasedOnTheme: isDarkThemeOn:false")
//            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_home_4_line_primary)
//            menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_line_primary)
//            menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_line_primary)
//            menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_line_primary)
//
//        }
    }
    private fun toggleBottomMenuIcons(
        showMessageFragment: Boolean=false,
        showContactsFragment: Boolean=false,
        showCallsFragment: Boolean=false,
        showSearchFragment: Boolean=false ) {
//        if(isDarkThemeOn){

        if(showMessageFragment){
            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_message_3_fill)
        }else{
            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_message_3_line)
        }

        if(showContactsFragment){
            menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_fill)
        }else{
            menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_line)
        }

        if(showCallsFragment){
            menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_fill)
        }else{
            menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_line)
        }
//        if(showSearchFragment){
//            menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_line)
//        }else {
//            menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_line)
//        }
//        }
//        else {
//            if(showMessageFragment){
//                menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_home_4_fill_primary)
//            }else{
//                menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_home_4_line_primary)
//
//            }
//
//            if(showContactsFragment){
//                menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_fill_primary)
//            }else{
//                menuContacts.icon = ContextCompat.getDrawable(this, R.drawable.ic_contacts_book_line_primary)
//            }
//
//            if(showCallsFragment){
//                menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_fill_primary)
//            }else{
//                menuCalls.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone_line_primary)
//            }
//            if(showSearchFragment){
//                menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_fill_primary)
//            }else {
//                menuSearch.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_line_primary)
//            }
//        }

    }


    //        @Override
//        public void onBackPressed() {
//            Log.d(TAG, "onBackPressed: MainActivity");
//            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.callFragment);
//            if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onKeyDown()) {
//                super.onBackPressed();
//            }
//            super.onBackPressed();
//        }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        if(dialerFragment.isVisible){
            val ft = supportFragmentManager.beginTransaction()
            ft.hide(dialerFragment)
            ft.show(callFragment)
            binding.bottomNavigationView.beVisible()

//            fabBtnShowDialpad.visibility = View.VISIBLE
            ft.commit()
        }else if(smsFragment.isVisible){
            if(smsFragment.getMarkedItemsSize() > 0){
//                unMarkItems()
                lifecycleScope.launchWhenCreated {
                    smsFragment.clearMarkeditems()
                }
//                markingStarted = false
            }
            else{
//                super.onBackPressed()
                finishAfterTransition()
            }

        }
        else if(callFragment.isVisible){
            if(callFragment.getMarkedItemsSize() > 0){
                lifecycleScope.launchWhenCreated {
                    callFragment.clearMarkeditems()
                }
//                callFragment.showSearchView()
//                callFragment.updateSelectedItemCount()
        }

            else{
//                super.onBackPressed()
                finishAfterTransition()
            }
        }
        else if(smsSearchFragment.isVisible){
            showMessagesFragment()

        }

        else{

            //for hiding search fragment
//            if(this::searchFragment !=null){
//                if(this.searchFragment?.isAdded!! and this.searchFragment?.isVisible!!){
//                    Log.d(TAG, "onBackPressed: searchfragment is visible")
//                }
//            }
//            if(this.searchFragment !=null)
//                if(this.searchFragment!!.isVisible){
//
//                }

//            super.onBackPressed()
            finishAfterTransition()

        }

    }
//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
////         super.onKeyDown(keyCode, event);
//        Log.d(TAG, "key down")
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Log.d(TAG, "onKeyDown: back button key down")
//            val fragments = supportFragmentManager.fragments
//            for (f in fragments) {
////                if (f != null && f is CallFragment) f.onKeyDown(keyCode, event)
//            }
//            return true
//        }
//        Log.d(TAG, "returning")
//        return false
//    }

    private fun showDialPad() {}


//    private fun checkPermission() {
//        val permissionsUtil = PermissionsUtil(this)
//        if (!permissionsUtil.checkPermissions()) {
//            startActivity(Intent(this, ActivityRequestPermission::class.java))
//            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
//            return
//        }
//    }

    override fun onPostResume() {
        super.onPostResume()
        Log.i(TAG, "Onresume")
//        val iExtra = intent.getIntExtra(IntentKeys.SHOW_BLOCK_LIST, 0)
//        when(iExtra){
//            IntentKeys.SHOW_BLOCK_LIST_VALUE -> {
//                binding.bottomNavigationView.selectedItemId = R.id.bottombaritem_blockList
//            }
//        }

//        saveTokenIfConnected()

        //        checkPermission();

//        if(getCurrentTheme() == 1){
//            setcurrentThemeInSharedPref()
//        }
//        checkPermission()
//        if (checkPermission()) {
//            if(_rcAuthStateListener!=null && _rcAuthStateListener !=null)
//                _rcfirebaseAuth?.addAuthStateListener(_rcAuthStateListener!!)
//        }
        //        firebaseHelper.addFirebaseAuthListener();

    }

    private fun setcurrentThemeInSharedPref() {
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
        val currentTheme = getCurrentTheme()
        var isDarkTheme = false
        if(currentTheme == 1){
            isDarkTheme = true
        }
        lifecycleScope.launch {
            val editor = sharedPreferences.edit()

            editor.putBoolean("isDarkTheme", isDarkTheme)
            editor.commit()
        }
    }
    private fun getCurrentTheme(): Int {
        val currentNightMode = getResources().getConfiguration().uiMode and  Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                Log.d(TAG, "checkTheme: white")
                return 0

            }
            // Night mode is not active, we're in day time
            Configuration.UI_MODE_NIGHT_YES -> {
                Log.d(TAG, "checkTheme: dark")
                return 1
            }
            // Night mode is active, we're at night!
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                Log.d(TAG, "checkTheme: undefined")
                return 2
            }else->{
            return 2

        }

            // We don't know what mode we're in, assume notnight
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "OnRestart")
        //        checkPermission();
    }

    override fun onStart() {
        Log.i(TAG, "OnStart")
        super.onStart()
//        this.user = rcfirebaseAuth!!.currentUser

        //        checkPermission();
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick: ")
        when(v.id){
            R.id.imgViewAvatarDrawer ->{
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            }
//        R.id.fabBtnShowDialpad->{
//            showDialerFragment()
//           GlobalScope.launch {
//               val callersInfo = HashCallerDatabase.getDatabaseInstance(this@MainActivity).callersInfoFromServerDAO()
//               callersInfo.deleteAll()
//           }
//        }
        }
//        val i = Intent(baseContext, ActivityAddNewPattern::class.java)
        //        i.putExtra("PersonID", personID);
//        startActivity(i)
    }

    //    private val phoneNumFromViewModel: String?
//        get() {
//            val phoneNumberViewModel: PhoneNumber = ViewModelProvider(this).get(PhoneNumber::class.java)
//            val no = phoneNumberViewModel.phoneNumber
//            return no?.value
//        }
//
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {

        Log.d(TAG, "onRequestPermissionsResult: ")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PermisssionRequestCodes.REQUEST_CODE_CALL_LOG ->{
                detachAndAttachFragment(callFragment)
            }

            REQUEST_CODE_READ_CONTACTS ->{

            }
            REQUEST_CODE_READ_SMS ->{
                fetchSMSOnCreate = true
                detachAndAttachFragment(smsFragment)
            }


        }
//        if (requestCode == 100) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                call()
//            } else {
//                Toast.makeText(this, "call permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    /**
     * This function is called when a permission is granted by user
     * @param fragment the fragment that have got requested permission
     * Re attaches the fragment will result in restarting the fragment life cycle from start.
     * So the  data will be requested again from content provider
     */
    private fun detachAndAttachFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.detach(fragment)
        fragmentTransaction.attach(fragment)
        fragmentTransaction.commit()
    }

    //
    fun makeCall(view: View?) {
        Log.d(TAG, "callDude: ")
        call()
    }

    private fun call() {
        val callIntent = Intent(Intent.ACTION_CALL)
//        callIntent.data = Uri.parse("tel:$phoneNumFromViewModel")
        if (ActivityCompat.checkSelfPermission(
                this,
                CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        startActivity(callIntent)
    }

    //    @Override
    //    protected void onStart() {
    //        super.onStart();
    ////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    //            PermissionsUtil permissionsUtil = new PermissionsUtil(this);
    //
    //            if (!permissionsUtil.checkPermissions()) {
    //                startActivity(new Intent(this, ActivityRequestPermission.class));
    //            }
    //                       }
    //    }
    //after nested frags
    internal inner class ContactObserver(handler: Handler?) : ContentObserver(handler) {
        //        @Override
        //        public void onChange(boolean selfChange) {
        //            this.onChange(selfChange, null);
        //            Log.e("", "~~~~~~" + selfChange);
        //            // Override this method to listen to any changes
        //        }
        override fun onChange(selfChange: Boolean, uri: Uri) {
            // depending on the handler you might be on the UI
            // thread, so be cautious!
            Log.d("ContactObserver", "onChange: ")
        }

        // left blank below constructor for this com.nibble.hashcaller.network.user.Contact observer example to work
        // or if you want to make this work using Handler then change below registering  //line
        init {
            Log.d("ContactObserver", "ContactObserver constructor ")
        }
    }

    fun isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    override fun onPause() {
        super.onPause()

    }
    companion object {
        private const val TAG = "__MainActivity"

        var fetchSMSOnCreate = false

        private const val KEY_ALIAS = "MYKeyAlias"
        private const val KEY_STORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/NoPadding"

        private  const val SHARED_PREFERENCE_TOKEN_KEY = "tokenKey"
        fun hideKeyboard(activity: Activity) {
            try {
                val inputManager = activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocusedView = activity.currentFocus
                if (currentFocusedView != null) {
                    inputManager.hideSoftInputFromWindow(
                        currentFocusedView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ROLE_SCREENING_APP_REQUEST_CODE -> {

                if (resultCode == Activity.RESULT_OK) {
                    //The user set you as the default screening app!
//                        dataStoreViewModel.userSelectedAsScreeningApp()
                    callFragment.activtyResultisDefaultScreening()
                    Log.d(TAG, "onActivityResult: user set as as the defaul screening app")
                } else {
                    //the user didn't set you as the default screening app...
                    Log.d(TAG, "onActivityResult: user does not set as the defaul screening app")
                }
            }
            PERMISSION_REQUEST_CODE ->{
                firebaseAuthListener()

            }

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.spamSms -> {
                val intent = Intent(this, SpamSMSActivity::class.java)
                startActivity(intent)

            }
            R.id.settingsMenuItem ->{
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.spamCalls ->{
                val intent = Intent(this, SpamCallsActivity::class.java)
                startActivity(intent)
            }
            R.id.drawerConfigureBlocking ->{
                val intent = Intent(this, BlockManageActivity::class.java)
                startActivity(intent)
            }
//            R.id.myBlockLIst ->{
//                val intent = Intent(this, BlockListActivity::class.java)
//                startActivity(intent)
//            }
            R.id.notifications ->{
                val intent = Intent(this, ManageNotificationsActivity::class.java)
                startActivity(intent)
            }

            R.id.inviteMenuItem ->{
            }

        }
        return false
    }

    fun showDrawer() {
//    actionbarDrawertToggle.onOptionsItemSelected(it as MenuItem)
        lifecycleScope.launchWhenStarted {
            binding.drawerLayout.openDrawer(Gravity.LEFT)

        }

    }

    fun setContactsHashMap(){
//        this.userInfoViewModel.setContactsHashMap()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
//        if(!isDefaultSMSHandler()){
//            //this is to make sure that when BlockManageActivity starts the
//                //switch will be in accordance with DefaultSMsHandlerPermission
//            dataStoreViewModel?.setBoolean(PreferencesKeys.DO_NOT_RECIEVE_SPAM_SMS, false)
//        }

    }

    fun getBottomNavView(): BottomNavigationView {
        return binding.bottomNavigationView
    }

    fun getCorinateLayout(): CoordinatorLayout {
        return binding.cordinateLyoutMainActivity
    }
}

