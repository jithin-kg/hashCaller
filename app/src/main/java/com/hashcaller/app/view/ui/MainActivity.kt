package com.hashcaller.app.view.ui

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityMainBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.datastore.DataStoreViewmodel.Companion.PERMISSION__ONLY_GIVEN
import com.hashcaller.app.datastore.DataStoreViewmodel.Companion.USER_INFO_AND_PERMISSION_GIVEN
import com.hashcaller.app.datastore.DataStoreViewmodel.Companion.USER_INFO_ONLY_GIVEN
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.datastore.PreferencesKeys.Companion.USER_INFO_AVIALABLE_IN_DB
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_READ_CONTACTS
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.ROLE_SCREENING_APP_REQUEST_CODE
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.crypto.KeyManager
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.blockConfig.BlockConfigFragment
import com.hashcaller.app.view.ui.call.CallFragment
import com.hashcaller.app.view.ui.call.dialer.DialerFragment
import com.hashcaller.app.view.ui.call.spam.SpamCallsActivity
import com.hashcaller.app.view.ui.contacts.ContactsContainerFragment
import com.hashcaller.app.view.ui.contacts.startContactUploadWorker
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.extensions.startPermissionRequestActivity
import com.hashcaller.app.view.ui.getstarted.GetStartedActivity
import com.hashcaller.app.view.ui.getstarted.GettingStartedSliderActivity
import com.hashcaller.app.view.ui.hashworker.HasherViewmodel
import com.hashcaller.app.view.ui.manageblock.BlockManageActivity
import com.hashcaller.app.view.ui.notifications.ManageNotificationsActivity
import com.hashcaller.app.view.ui.profile.ProfileActivity
import com.hashcaller.app.view.ui.settings.SettingsActivity
import com.hashcaller.app.view.ui.sms.SMSContainerFragment
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.DefaultFragmentManager
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.work.formatPhoneNumber
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
    private lateinit var contactFragment: ContactsContainerFragment
    private lateinit var blockListFragment: BlockConfigFragment
    private lateinit var ft: FragmentTransaction
    private lateinit var dialerFragment: DialerFragment
//    private lateinit var smsSearchFragment: SMSSearchFragment

    private lateinit var header:View
    private lateinit var headerImgView:de.hdodenhof.circleimageview.CircleImageView
    private lateinit var firstLetterView:TextView
    private lateinit var menu:Menu
    //    private lateinit var  menuMessage:MenuItem
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
    private lateinit var scrnRoleCallback: ActivityResultLauncher<Intent>





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
        setDataStoreValues()
        lifecycleScope.launchWhenCreated {
            this@MainActivity.startContactUploadWorker()
        }
        regstrScreeningRoleResultCb()

    }

    private fun setDataStoreValues() {
        lifecycleScope.launchWhenCreated {
            SPAM_THRESHOLD_VALUE = DataStoreRepository(this@MainActivity.tokeDataStore).getInt(PreferencesKeys.SPAM_THRESHOLD)?: Constants.DEFAULT_SPAM_THRESHOLD
        }
    }



    private fun checkUserInfoAvaialbleInDb(savedInstanceState: Bundle?) {
        dataStoreViewModel?.getPermissionAndUserInfo(USER_INFO_AVIALABLE_IN_DB, this)?.observe(this, Observer {
            when(it){
                USER_INFO_AND_PERMISSION_GIVEN ->{

                    firebaseAuthListener()
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
//                       ft.detach(smsFragment)
//                       ft.attach(smsFragment)
                        ft.detach(contactFragment)
                        ft.attach(contactFragment)
//                       ft.detach(searchFragment)
//                       ft.attach(searchFragment)
                        ft.detach(blockListFragment)
                        ft.attach(blockListFragment)
//                       ft.detach(smsSearchFragment)
//                       ft.attach(smsSearchFragment)
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
        })

    }


    private fun firebaseAuthListener() {
        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        if(user ==null){
            onSingnedOutcleanUp()
        }else{
            tokenHelper = TokenHelper(user)
        }
    }

    private fun onSingnedOutcleanUp() {
        val i = Intent(this, GettingStartedSliderActivity::class.java)
        startActivity(i)
        finish()
    }
    private fun initMainActivityComponents() {

        hideKeyboard(this)
        initHashCallerViewmodel()
        initColors()
        setAllMenuItems()
        val c = ContextCompat.getColor(applicationContext, R.color.textColor);
        setupNavigationDrawer()
        initHeaderView()
        setBottomSheetListener()
        initListeners()
        initViewModel()
        observeUserInfo()
    }

    private fun initColors() {
        hashCallerViewModel.initColors()

    }

    private fun initHashCallerViewmodel() {
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
                    if(!it.googleProfileImgUrl.isNullOrEmpty()){
                        Glide.with(this).load(it.googleProfileImgUrl)
                            .into(headerImgView)
                        firstLetterView.beInvisible()
                    }else if(!it.photoURI.isNullOrEmpty()){
                        headerImgView.setImageBitmap(getDecodedBytes(it.photoURI))
                        firstLetterView.beInvisible()
                    }else{
                        firstLetterView.beVisible()
                    }
                }catch (e:Exception){
                    toast("Unable to get user name")
                }

            }
        })
    }


    override fun onDestroy() {
        viewModelStore.clear()
        dataStoreViewModel = null
        super.onDestroy()


    }




    fun showSnackBar(message: String){
        val sbar = Snackbar.make(cordinateLyoutMainActivity, message, Snackbar.LENGTH_SHORT)
        sbar.setAction("Action", null)
        sbar.anchorView = binding.bottomNavigationView
        sbar.show()

    }

    private fun manageSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            ft = supportFragmentManager.beginTransaction()
            this.contactFragment = ContactsContainerFragment()
            this.callFragment = CallFragment()
            this.dialerFragment = DialerFragment()
//            this.searchFragment = SearchFragment()
            this.blockListFragment = BlockConfigFragment()
            setTheDefaultFragment()
//            addAllFragments()

        }else{
            setFragmentsFromSavedInstanceState(savedInstanceState)

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
    private fun setFragmentsFromSavedInstanceState(savedInstanceState: Bundle) {
//        this.fullScreenFragment = supportFragmentManager.getFragment(savedInstanceState, "fullScreenFragment") as FullscreenFragment
        this.callFragment = supportFragmentManager.getFragment(savedInstanceState, "callFragment") as CallFragment

        this.contactFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "contactFragment"
        ) as ContactsContainerFragment
        this.dialerFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "dialerFragment"
        ) as DialerFragment

        this.blockListFragment = supportFragmentManager.getFragment(
            savedInstanceState,
            "blockListFragment"
        ) as BlockConfigFragment

//        this.smsSearchFragment = supportFragmentManager.getFragment(
//            savedInstanceState,
//            "smsSearchFragment"
//        ) as SMSSearchFragment




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
//                R.id.bottombaritem_messages -> {
//                    showMessagesFragment()
//                    Log.d(TAG, "setBottomSheetListener: show sms clicked")
////                    fabBtnShowDialpad.visibility = View.GONE
//                    return@OnNavigationItemSelectedListener true
//                }
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

        supportFragmentManager.putFragment(outState, "callFragment", this.callFragment)
//        supportFragmentManager.putFragment(outState, "fullScreenFragment", this.fullScreenFragment)
        supportFragmentManager.putFragment(outState, "contactFragment", this.contactFragment)
        supportFragmentManager.putFragment(outState, "dialerFragment", this.dialerFragment)
//        supportFragmentManager.putFragment(outState, "messagesFragment", this.smsFragment)
//        supportFragmentManager.putFragment(outState, "searchFragment", this.searchFragment)
        supportFragmentManager.putFragment(outState, "blockListFragment", this.blockListFragment)
//        supportFragmentManager.putFragment(outState, "smsSearchFragment", this.smsSearchFragment)

    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }
    /**
     * This function set the default fragment status of each fragment
     */
    private fun setTheDefaultFragment() {
        when (DefaultFragmentManager.defaultFragmentToShow) {
            DefaultFragmentManager.SHOW_CALL_FRAGMENT -> {
                callFragment.isDefaultFgmnt = true
            }
            DefaultFragmentManager.SHOW_CONTACT_FRAGMENT -> {
                contactFragment.isDefaultFgmnt = true
            }
            else -> {
                dialerFragment.isDefaultFgmnt = true
            }
        }
    }

    fun showDialerFragment() {
        val ft = supportFragmentManager.beginTransaction()
        callFragment.clearMarkeditems()
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }

        if (dialerFragment.isAdded) { // if the fragment is already in container

            ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            mainViewmodel.setActiveFragment(dialerFragment)
            ft.show(dialerFragment)
            dialerFragment.showDialPad()
            bottomNavigationView.beGone()
        }
        ft.commit()
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

//        ft.add(R.id.frame_fragmentholder, smsFragment)
//        hideThisFragment(ft, smsFragment, smsFragment)


//        bottomNavigationView!!.selectedItemId = R.id.bottombaritem_calls
        ft.add(R.id.frame_fragmentholder, contactFragment)
        hideThisFragment(ft, contactFragment, contactFragment)

//        ft.add(R.id.frame_fragmentholder, searchFragment)
//        hideThisFragment(ft, searchFragment, searchFragment)

        ft.add(R.id.frame_fragmentholder, blockListFragment)
        hideThisFragment(ft, blockListFragment, blockListFragment)
//        ft.add(R.id.frame_fragmentholder, smsSearchFragment)
//        hideThisFragment(ft, smsSearchFragment, smsSearchFragment)
//        ft.add(R.id.frame_fragmentholder, blockConfigFragment)
//        hideThisFragment(ft, blockConfigFragment, blockConfigFragment)
//        fabBtnShowDialpad.visibility = View.VISIBLE
        ft.commit()

    }

    private fun setAllMenuItems() {
        menu = binding.bottomNavigationView.menu
        menuContacts = menu.findItem(R.id.bottombaritem_contacts)
        menuCalls = menu.findItem( R.id.bottombaritem_calls )
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
        val ft = supportFragmentManager.beginTransaction()

        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        if (contactFragment.isAdded) { // if the fragment is already in container
            ft.show(contactFragment)
            mainViewmodel.setActiveFragment(contactFragment)
        }
        /**
         * Managing contacts uploading/Syncing by ContactsUPloadWorkManager
         */
        val intent = intent
        intent.getByteArrayExtra("key")
        ft.commit()
    }
    fun showSMSSearchFragment() {
//        val ft = supportFragmentManager.beginTransaction()
//        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
//
//        if (smsSearchFragment.isAdded) { // if the fragment is already in container
//            ft.show(smsSearchFragment)
//            mainViewmodel.setActiveFragment(smsSearchFragment)
//        }
////
//        ft.commit()
//        binding.bottomNavigationView.beGone()
//        binding.navView.beGone()
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
        val ft = supportFragmentManager.beginTransaction()
        mainViewmodel.getActiveFragment()?.let { ft.hide(it) }
        ft.commit()
    }

    private fun toggleBottomMenuIcons(
        showMessageFragment: Boolean=false,
        showContactsFragment: Boolean=false,
        showCallsFragment: Boolean=false,
        showSearchFragment: Boolean=false ) {
//        if(isDarkThemeOn){

//        if(showMessageFragment){
//            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_message_3_fill)
//        }else{
//            menuMessage.icon = ContextCompat.getDrawable(this, R.drawable.ic_message_3_line)
//        }

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

    override fun onBackPressed() {
        if(dialerFragment.isVisible){
            val ft = supportFragmentManager.beginTransaction()
            ft.hide(dialerFragment)
            ft.show(callFragment)
            binding.bottomNavigationView.beVisible()

            ft.commit()
        }
//        else if(smsFragment.isVisible){
//            if(smsFragment.getMarkedItemsSize() > 0){
////                unMarkItems()
//                lifecycleScope.launchWhenCreated {
//                    smsFragment.clearMarkeditems()
//                }
////                markingStarted = false
//            }
//            else{
////                super.onBackPressed()
//                finishAfterTransition()
//            }
//
//        }
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
//        else if(smsSearchFragment.isVisible){
//            showMessagesFragment()
//
//        }

        else{

            finishAfterTransition()

        }

    }

    private fun showDialPad() {}

    override fun onPostResume() {
        super.onPostResume()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser == null){
            onSingnedOutcleanUp()
        }
    }

    private fun getCurrentTheme(): Int {
        val currentNightMode = getResources().getConfiguration().uiMode and  Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                return 0

            }
            // Night mode is not active, we're in day time
            Configuration.UI_MODE_NIGHT_YES -> {
                return 1
            }
            // Night mode is active, we're at night!
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                return 2
            }else->{
            return 2

        }

            // We don't know what mode we're in, assume notnight
        }
    }

    override fun onRestart() {
        super.onRestart()
        //        checkPermission();
    }

    override fun onStart() {
        super.onStart()
//        this.user = rcfirebaseAuth!!.currentUser

        //        checkPermission();
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.imgViewAvatarDrawer ->{
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PermisssionRequestCodes.REQUEST_CODE_CALL_LOG ->{

                detachAndAttachFragment(callFragment)
            }

            REQUEST_CODE_READ_CONTACTS ->{

            }


        }
    }

    /**
     * This function is called when a permission is granted by user
     * @param fragment the fragment that have got requested permission
     * Re attaches the fragment will result in restarting the fragment life cycle from start.
     * So the  data will be requested again from content provider
     */
    private fun detachAndAttachFragment(fragment: Fragment) {
        val fragmentTransactionDetach = supportFragmentManager.beginTransaction()
//        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransactionDetach.detach(fragment)
        fragmentTransactionDetach.commit()
        val fragmentTransactionAttach = supportFragmentManager.beginTransaction()
        fragmentTransactionAttach.attach(fragment)
        fragmentTransactionAttach.commit()
    }

    //
    fun makeCall(view: View?) {
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


    fun isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    override fun onPause() {
        super.onPause()

    }
    companion object {
        private const val TAG = "__MainActivity"
        var SPAM_THRESHOLD_VALUE = DEFAULT_SPAM_THRESHOLD
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



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
//            R.id.spamSms -> {
//                val intent = Intent(this, SpamSMSActivity::class.java)
//                startActivity(intent)
//
//            }
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
    }

    fun getBottomNavView(): BottomNavigationView {
        return binding.bottomNavigationView
    }

    fun getCorinateLayout(): CoordinatorLayout {
        return binding.cordinateLyoutMainActivity
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun reqScreeningRole() {
        val res = shouldReqstScreeningRole()
        if(res.first){
            //we should request screening role
            val intent = res.second?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            scrnRoleCallback.launch(intent)
        }
    }
    fun regstrScreeningRoleResultCb() {
        scrnRoleCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                callFragment.activtyResultisDefaultScreening()
            }
        }
    }
}
