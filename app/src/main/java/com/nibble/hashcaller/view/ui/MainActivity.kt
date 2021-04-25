package com.nibble.hashcaller.view.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.database.ContentObserver
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityMainBinding
import com.nibble.hashcaller.repository.spam.SpamSyncRepository
import com.nibble.hashcaller.utils.crypto.KeyManager
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.call.CallFragment
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.contacts.ContactsContainerFragment
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.contacts.utils.markingStarted
import com.nibble.hashcaller.view.ui.contacts.utils.unMarkItems
import com.nibble.hashcaller.view.ui.extensions.isScreeningRoleHeld
import com.nibble.hashcaller.view.ui.extensions.requestScreeningRole
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.DefaultFragmentManager
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.work.ContactsAddressLocalWorker
import com.nibble.hashcaller.work.ContactsUploadWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.*
import kotlin.math.log


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
//    private lateinit var userInfoViewModel: UserInfoViewModel
    private lateinit var callFragment: CallFragment
    private lateinit var messagesFragment: SMSContainerFragment
    //    private lateinit var blockConfigFragment: BlockConfigFragment
    private lateinit var contactFragment: ContactsContainerFragment
    private lateinit var ft: FragmentTransaction
    private lateinit var dialerFragment: DialerFragment
//    var  searchFragment: SearchFragment? = null


    private lateinit var drawerLayout: DrawerLayout
    //    private lateinit var navigationView:NavigationView
    private lateinit var actionbarDrawertToggle: ActionBarDrawerToggle
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

//    var layoutBottomSheet: ConstraintLayout

    //    MainActivityHelper firebaseHelper;
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
//    var contactsUploadWorkManager: ContactsUploadWorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideKeyboard(this)
//        setStatusBarColor(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listenUiEvents()
        requestAlertWindowPermission()
        Log.d(TAG, "onCreate: is dark theme on ${isDarkThemeOn()}")
        val c = ContextCompat.getColor(this, R.color.textColor);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val decor = window.decorView
//            if (true) {
//                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            } else {
//                // We want to change tint color to white again.
//                // You can also record the flags in advance so that you can turn UI back completely if
//                // you have set other flags before, such as translucent or full screen.
//                decor.systemUiVisibility = 0
//            }
//        }

        initViewModel()
        setupNavigationDrawer()

//        if(this.searchFragment !=null)
//        if(this.searchFragment?.isAdded!!){
//            bottomNavigationView.visibility = View.GONE
//        }

        manageSavedInstanceState(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(!isScreeningRoleHeld()){
                requestScreeningRole()

            }
        }
//        fabBtnShowDialpad.setOnClickListener(this)
        setBottomSheetListener()

        mangeCipherInSharedPref()
        observeUserInfoLiveData()
        setupContactUploadWork()



    }



    private fun requestAlertWindowPermission() {
        // Show alert dialog to the user saying a separate permission is needed
        // Show alert dialog to the user saying a separate permission is needed
        if(!canDrawOverlays(this)){
            val myIntent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }

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
        val request2 = OneTimeWorkRequest.Builder(ContactsAddressLocalWorker::class.java)
            .build()
        WorkManager.getInstance().enqueue(request2)

        val request =
            OneTimeWorkRequest.Builder(ContactsUploadWorker::class.java)
                .build()
        WorkManager.getInstance().enqueue(request)

    }

    private fun manageSavedInstanceState(savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: savedInstanceState is null")
            ft = supportFragmentManager.beginTransaction()
            this.messagesFragment = SMSContainerFragment()
//            this.blockConfigFragment = BlockConfigFragment()
            this.contactFragment = ContactsContainerFragment()
            this.callFragment = CallFragment()
            this.dialerFragment = DialerFragment()
//            this.searchFragment =  SearchFragment.newInstance()
//            setInstancesInApp()

//            fabBtnShowDialpad.visibility = View.GONE
            syncSpamList()


            //set the default fragment
            setTheDefaultFragment()
            addAllFragments()

        }else{

            setFragmentsFromSavedInstanceState(savedInstanceState)
//            this.ft = supportFragmentManager.beginTransaction()

        }
    }

    private fun initViewModel() {
//        userInfoViewModel = ViewModelProvider(
//            this, MainActivityInjectorUtil.provideUserInjectorUtil(
//                this
//            )
//        ).get(
//            UserInfoViewModel::class.java
//        )
    }



    private fun setupNavigationDrawer() {
//        drawerLayout = findViewById(R.id.drawer_layout)
//        navigationView = findViewById(R.id.nav_view)
//        actionbarDrawertToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.start, R.string.close)

//        drawerLayout.addDrawerListener(actionbarDrawertToggle)
//        actionbarDrawertToggle.syncState()

//        navigationView.setNavigationItemSelectedListener(this)


    }

    private fun mangeCipherInSharedPref() {
        if(!isCipherInSharedPreferences()){
//            KeyManager.setCipherInSharedPreferences(this)
        }
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
        this.callFragment = supportFragmentManager.getFragment(savedInstanceState, "callFragment") as CallFragment
        this.messagesFragment = supportFragmentManager.getFragment(
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
                R.id.bottombaritem_search -> {
//                    showBlockConfigFragment()
//                    fabBtnShowDialpad.visibility = View.GONE
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
        supportFragmentManager.putFragment(outState, "contactFragment", this.contactFragment)
        supportFragmentManager.putFragment(outState, "dialerFragment", this.dialerFragment)
        supportFragmentManager.putFragment(outState, "messagesFragment", this.messagesFragment)
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
        val list = CountrycodeHelper(this).getCountrycode()
        val spamSyncRepository = SpamSyncRepository()
//        SpamSyncManager.sync(list, spamSyncRepository, this)

    }



    /**
     * This function set the default fragment status of each fragment
     */
    private fun setTheDefaultFragment() {
//        contactFragment.isDefaultFgmnt = true

        if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_CALL_FRAGMENT){
            callFragment.isDefaultFgmnt = true
        }else if(DefaultFragmentManager.defaultFragmentToShow == DefaultFragmentManager.SHOW_MESSAGES_FRAGMENT){
            messagesFragment.isDefaultFgmnt = true
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

        // Hide fragment contact
        if (contactFragment.isAdded) {
            ft.hide(contactFragment)
        }
//        // Hide fragment call
        if (callFragment.isAdded) {
            ft.hide(callFragment)
        }
//        if(blockConfigFragment.isAdded){
//            ft.hide(blockConfigFragment)
//        }
        if (messagesFragment.isAdded) {
            ft.hide(messagesFragment)
        }

        if (dialerFragment.isAdded) { // if the fragment is already in container

            ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)

            ft.show(dialerFragment)
            dialerFragment.showDialPad()

        }
        // Commit changes
//        ft.addToBackStack("test")
        ft.commit()
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
        binding. bottomNavigationView.visibility = View.VISIBLE

    }

    /**
     * adds fragment to frame layout after search fragment closes
     */
    fun addFragmentsAgain(){
        setTheDefaultFragment()
//        DefaultFragmentManager.defaultFragmentToShow = 2
        addAllFragments()
//        val actionRestart =
//            findViewById<View>(R.id.bottombaritem_calls)




    }
    fun addAllFragments() {
        setDefaultFragment(DefaultFragmentManager.id)

        ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame_fragmentholder, messagesFragment)
        hideThisFragment(ft, messagesFragment, messagesFragment)


//        bottomNavigationView!!.selectedItemId = R.id.bottombaritem_calls
        ft.add(R.id.frame_fragmentholder, contactFragment)
        hideThisFragment(ft, contactFragment, contactFragment)
//        ft.add(R.id.frame_fragmentholder, blockConfigFragment)
//        hideThisFragment(ft, blockConfigFragment, blockConfigFragment)

        ft.add(R.id.frame_fragmentholder, callFragment)
        hideThisFragment(ft, callFragment, callFragment)
//        fabBtnShowDialpad.visibility = View.VISIBLE

        ft.add(R.id.frame_fragmentholder, dialerFragment)
        hideThisFragment(ft, dialerFragment, dialerFragment)

        ft.commit()
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
        }

    }

    private fun setDefaultFragment(idValue: Int) {
//        bottomNavigationView.selectedItemId = R.id.bottombaritem_calls
        binding.bottomNavigationView.selectedItemId = idValue
    }

    private fun showBlockConfigFragment() {
        val ft = supportFragmentManager.beginTransaction()
//        if (blockConfigFragment.isAdded) { // if the fragment is already in container
//            ft.show(blockConfigFragment)
//            unMarkItems()
//            messagesFragment.showSearchView()
//
//        }
        // Hide fragment contact
        if (contactFragment.isAdded) {
            ft.hide(contactFragment)
        }
//        // Hide fragment call
        if (callFragment.isAdded) {
            ft.hide(callFragment)
        }
        if(dialerFragment.isAdded){
            ft.hide(dialerFragment)
        }
        if (messagesFragment.isAdded) {
            ft.hide(messagesFragment)
        }
//        if(searchFragment!=null)
//        if(searchFragment!!.isAdded){
//            ft.hide(searchFragment!!)
//        }
        // Commit changes
        ft.commit()
    }

    fun showContactsFragment() {

//        showDialPad()
        val ft = supportFragmentManager.beginTransaction()

//        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//        }
//        // Hide fragment C
        if (callFragment.isAdded) {
            callFragment.clearMarkeditems()
            ft.hide(callFragment)
        }
        if(dialerFragment.isAdded){
            ft.hide(dialerFragment)
        }
        if (messagesFragment.isAdded) {
            ft.hide(messagesFragment)
        }

        if (contactFragment.isAdded) { // if the fragment is already in container
            ft.show(contactFragment)
            unMarkItems()
            messagesFragment.showSearchView()

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

    fun showCallFragment() {
        val ft = supportFragmentManager.beginTransaction()
        if (messagesFragment.isAdded) { // if the fragment is already in container(callFragment)
            unMarkItems()
            messagesFragment.showSearchView()

        }
        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//        }
        // Hide fragment C
        if (contactFragment.isAdded) {
            ft.hide(contactFragment)
        }
        if (dialerFragment.isAdded) {
            ft.hide(dialerFragment)
        }
        if (messagesFragment.isAdded) {
            ft.hide(messagesFragment)
        }
        if(callFragment.isAdded){
//            fabBtnShowDialpad.visibility = View.VISIBLE

            ft.show(callFragment)
        }

        // Commit changes
        ft.commit()
    }

    private fun showMessagesFragment() {
        val ft = supportFragmentManager.beginTransaction()

        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//            unMarkItems()
//            messagesFragment.showSearchView()
//
//        }
        // Hide fragment C
        if (contactFragment.isAdded) {
            ft.hide(contactFragment)
        }
        if (callFragment.isAdded) {
            callFragment.clearMarkeditems()
            ft.hide(callFragment)
        }
//        if (callFragment.isAdded) {
//            ft.hide(callFragment)
//        }
        if(dialerFragment.isAdded){
            ft.hide(dialerFragment)
        }
        if (messagesFragment.isAdded) { // if the fragment is already in container
//            ft.addToBackStack(messagesFragment.javaClass.name)
            ft.show(messagesFragment)
//            setDefaultFragment(R.id.bottombaritem_messages)

        }else{
            Log.d(TAG, "showMessagesFragment:messagesFragment not added")
        }

        // Commit changes
        ft.commit()
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
//            fabBtnShowDialpad.visibility = View.VISIBLE
            ft.commit()
        }else if(messagesFragment.isVisible){
            if(markedItems.size > 0){
                unMarkItems()
                markingStarted = false
            }

            else{
                super.onBackPressed()
            }

        }
        else if(callFragment.isVisible){
            if(callFragment.getMarkedItemsSize() > 0){
                lifecycleScope.launchWhenCreated {
                    callFragment.clearMarkeditems()
                }
//                callFragment.showSearchView()
//                callFragment.updateSelectedItemCount()
            }else{
                super.onBackPressed()
            }
        }

        else{
            binding.bottomNavigationView.visibility = View.VISIBLE

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

            super.onBackPressed()

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
        Log.i(TAG, "Onresume")
        //        checkPermission();

//        if(getCurrentTheme() == 1){
//            setcurrentThemeInSharedPref()
//        }
        super.onPostResume()
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
        //        checkPermission();
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick: ")
        when(v.id){
//        R.id.fabBtnShowDialpad->{
//            showDialerFragment()
//           GlobalScope.launch {
//               val callersInfo = HashCallerDatabase.getDatabaseInstance(this@MainActivity).callersInfoFromServerDAO()
//               callersInfo.deleteAll()
//           }
//        }
        }
//        bottomNavigationView.visibility =View.GONE
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call()
            } else {
                Toast.makeText(this, "call permission denied", Toast.LENGTH_SHORT).show()
            }
        }
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
                Manifest.permission.CALL_PHONE
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

        // left blank below constructor for this Contact observer example to work
        // or if you want to make this work using Handler then change below registering  //line
        init {
            Log.d("ContactObserver", "ContactObserver constructor ")
        }
    }
    fun isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    companion object {
        private const val TAG = "__MainActivity"
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


    //    @RequiresApi(Build.VERSION_CODES.Q)
//    fun requestScreeningRole(){
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//           val roleManager =  getSystemService(Context.ROLE_SERVICE) as RoleManager
//            val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
//             if(!isHeld){
//                 //ask the user to set your app as the default screening app
//                 val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
//                 startActivityForResult(intent, 123)
//             } else {
//                 //you are already the default screening app!
//             }
//        }
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            123 -> {
                if (resultCode == Activity.RESULT_OK) {
                    //The user set you as the default screening app!
                    Log.d(TAG, "onActivityResult: user set as as the defaul screening app")
                } else {
                    //the user didn't set you as the default screening app...
                    Log.d(TAG, "onActivityResult: user does not set as the defaul screening app")
                }
            }
            else -> {}
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected: ")
        return true
    }

    fun showDrawer(it: View) {
//    actionbarDrawertToggle.onOptionsItemSelected(it as MenuItem)
//    drawerLayout.openDrawer(Gravity.LEFT)

    }

    fun setContactsHashMap(){
//        this.userInfoViewModel.setContactsHashMap()
    }

    override fun onResume() {
        super.onResume()

    }



}


