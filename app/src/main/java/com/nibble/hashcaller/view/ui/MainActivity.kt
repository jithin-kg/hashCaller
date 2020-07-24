package com.nibble.hashcaller.view.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.blockConfig.BlockConfigFragment
import com.nibble.hashcaller.view.ui.contacts.ContactsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var toolbar: Toolbar
    private lateinit var  tabLayout: TabLayout
    private val viewPager: ViewPager? = null
    var fab: FloatingActionButton? = null

//    private lateinit var callFragment: CallFragment
//    private lateinit var messagesFragment: MessagesFragment
    private lateinit var blockConfigFragment: BlockConfigFragment
    private lateinit var contactFragment: ContactsFragment
    private lateinit var ft: FragmentTransaction
//    var layoutBottomSheet: ConstraintLayout

    //    MainActivityHelper firebaseHelper;
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
//    var contactsUploadWorkManager: ContactsUploadWorkManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

//        firebaseHelper = new MainActivityHelper();

//        firebaseHelper.intializeFirebaseLogin(this);
        hideKeyboard(this)
        setContentView(R.layout.activity_main)
//        this.applicationContext
//                .contentResolver
//                .registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        true, ContactObserver(Handler()))
        if (savedInstanceState == null) {
//            callFragment = CallFragment()
//            messagesFragment = MessagesFragment()
            blockConfigFragment = BlockConfigFragment()
            contactFragment = ContactsFragment()
            //            callFragment.setSelectedItemId(R.id.callFragment)
        }
        //        Intent intent = new Intent(MainActivity.this, CreateCustomFilter2.class);
//        startActivity(intent);
//        toolbar = findViewById(R.id.toolbar)
        //        tabLayout =  findViewById(R.id.tabLayout);
//        viewPager = findViewById(R.id.viewPager);
//        fab = findViewById(R.id.fabBtn);
//        fab.setOnClickListener(this);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

//        setSupportActionBar(toolbar);
//        setupViewPager(viewPager);
//        tabLayout.setupWithViewPager(viewPager);
//        getSupportActionBar().hide();

        //After nested fragments

        //        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            var fragment: Fragment
            val selectedFragment = ""
            when (menuItem.itemId) {
//                R.id.bottombaritem_messages -> {
//                    showMessagesFragment()
//                    return@OnNavigationItemSelectedListener true
//                }
//                R.id.bottombaritem_calls -> {
//                    showCallFragment()
//                    return@OnNavigationItemSelectedListener true
//                }
                R.id.bottombaritem_contacts -> {
                    showContactsFragment()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottombaritem_spam -> {
                    showBlockConfigFragment()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        addAllFragments()
        /**
         * Managing contacts uploading/Syncing by ContactsUPloadWorkManager
         */
//        val request = OneTimeWorkRequest.Builder(ContactsUploadWorkManager::class.java)
//                .build()
//        WorkManager.getInstance(applicationContext).enqueue(request)


        //TODO check if contacts are uploaded
        //check internet connection
//        contactsUploadWorkManager = new ContactsUploadWorkManager(getApplicationContext(), );

//        contactsUploadWorkManager.uploadContacts();
//        boolean contactsUploaded = false;

//        if(!contactsUploaded) {
//            ContactsUploder contactsUploder = new ContactsUploder(getApplicationContext());
//            contactsUploder.uploadContacts();
//        }

//        NetworkChecker networkChecker = new NetworkChecker(getApplicationContext());
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
    private fun addAllFragments() {
        ft = supportFragmentManager.beginTransaction()
//        ft.add(R.id.frame_fragmentholder, messagesFragment)
//        ft.hide(messagesFragment)
//        ft.add(R.id.frame_fragmentholder, callFragment)
//        //        ft.hide(callFragment);
//        bottomNavigationView!!.selectedItemId = R.id.bottombaritem_calls
        ft.add(R.id.frame_fragmentholder, contactFragment)
//        ft.hide(contactFragment)
        ft.add(R.id.frame_fragmentholder, blockConfigFragment)
        ft.hide(blockConfigFragment)
        ft.commit()
    }

    private fun showBlockConfigFragment() {
        val ft = supportFragmentManager.beginTransaction()
        if (blockConfigFragment.isAdded) { // if the fragment is already in container
            ft.show(blockConfigFragment)
        }
        // Hide fragment contact
        if (contactFragment.isAdded) {
            ft.hide(contactFragment)
        }
//        // Hide fragment call
//        if (callFragment.isAdded) {
//            ft.hide(callFragment)
//        }
//        if (messagesFragment.isAdded) {
//            ft.hide(messagesFragment)
//        }
        // Commit changes
        ft.commit()
    }

    private fun showContactsFragment() {
//        showDialPad()
        val ft = supportFragmentManager.beginTransaction()
        if (contactFragment.isAdded) { // if the fragment is already in container
            ft.show(contactFragment)
        }
//        // Hide fragment B
        if (blockConfigFragment.isAdded) {
            ft.hide(blockConfigFragment)
        }
//        // Hide fragment C
//        if (callFragment.isAdded) {
//            ft.hide(callFragment)
//        }
//        if (messagesFragment.isAdded) {
//            ft.hide(messagesFragment)
//        }
//        // Commit changes
        ft.commit()
    }

    //    @Override
    //    public void onBackPressed() {
    //        Log.d(TAG, "onBackPressed: MainActivity");
    //        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.callFragment);
    //        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onKeyDown()) {
    //            super.onBackPressed();
    //        }
    //        super.onBackPressed();
    //    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//         super.onKeyDown(keyCode, event);
        Log.d(TAG, "key down")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: back button key down")
            val fragments = supportFragmentManager.fragments
            for (f in fragments) {
//                if (f != null && f is CallFragment) f.onKeyDown(keyCode, event)
            }
            return true
        }
        Log.d(TAG, "returning")
        return false
    }

    private fun showDialPad() {}
//    private fun showCallFragment() {
//        val ft = supportFragmentManager.beginTransaction()
//        if (callFragment.isAdded) { // if the fragment is already in container
//            ft.show(callFragment)
//        }
//        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//        }
//        // Hide fragment C
//        if (contactFragment.isAdded) {
//            ft.hide(contactFragment)
//        }
//        if (messagesFragment.isAdded) {
//            ft.hide(messagesFragment)
//        }
//
//        // Commit changes
//        ft.commit()
//    }

//    private fun showMessagesFragment() {
//        val ft = supportFragmentManager.beginTransaction()
//        if (messagesFragment.isAdded) { // if the fragment is already in container
//            ft.show(messagesFragment)
//        }
//        // Hide fragment B
//        if (blockConfigFragment.isAdded) {
//            ft.hide(blockConfigFragment)
//        }
//        // Hide fragment C
//        if (contactFragment.isAdded) {
//            ft.hide(contactFragment)
//        }
//        if (callFragment.isAdded) {
//            ft.hide(callFragment)
//        }
//        // Commit changes
//        ft.commit()
//    }

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
        super.onPostResume()
        //        firebaseHelper.addFirebaseAuthListener();
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
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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

    companion object {
        private const val TAG = "MainActivity"
        fun hideKeyboard(activity: Activity) {
            try {
                val inputManager = activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val currentFocusedView = activity.currentFocus
                if (currentFocusedView != null) {
                    inputManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}