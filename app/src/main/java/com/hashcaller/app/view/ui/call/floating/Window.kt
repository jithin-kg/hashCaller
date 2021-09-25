package com.hashcaller.app.view.ui.call.floating

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.hashcaller.app.R
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.Constants.Companion.SIM_ONE
import com.hashcaller.app.utils.Constants.Companion.SIM_TWO
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.view.ui.contacts.toggleUserBadge
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.sms.individual.util.beGone
import com.hashcaller.app.view.ui.sms.individual.util.beInvisible
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.work.formatPhoneNumber
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*

class Window(
    private val context: Context,
    private val countryCodeHelper: LibPhoneCodeHelper?
) {

    private var phoneNumber:String = ""
    private var callerNameFoundFrom = NAME_SEARCHING
    private var callerImageFoundFrom = IMAGE_SEARCHING
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.window, null)
    private val tvName:TextView = rootView.findViewById<TextView>(R.id.txtVcallerNameWindow)
    private val tvFirstLetter:TextView = rootView.findViewById<TextView>(R.id.tvFistLetterWindow)
    private val tvPhoneNumIncomming: TextView = rootView.findViewById<TextView>(R.id.tvPhoneNumIncomming)
    private val tvLocation:TextView = rootView.findViewById(R.id.txtVLocaltionWindow)
    private val layoutInnerWindow: ConstraintLayout = rootView.findViewById(R.id.layoutInnerWindow)
    private val imgVAvatar : CircleImageView = rootView.findViewById(R.id.imgVAvatarIncomming)
    private val imgVUserBadgeBg: ImageView = rootView.findViewById(R.id.imgUserIconBg)
    private val imgVUserIcon: ImageView = rootView.findViewById(R.id.imgUserIcon)
//    private val imgVerifiedBadge: ImageView = rootView.findViewById(R.id.imgVerifiedBadge)
    private val imgVSimOne: ImageView = rootView.findViewById(R.id.imgVSimOne)
    private val imgVSimTwo: ImageView = rootView.findViewById(R.id.imgVSimTwo)
    private val windowParams = WindowManager.LayoutParams(
        0,
        0,
        0,
        0,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        PixelFormat.TRANSLUCENT
    )


    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm
    }

     fun setSimInView(simNum:Int){
         when(simNum){
             SIM_ONE -> {
                 imgVSimTwo.beInvisible()
                 imgVSimOne.beVisible()
             }
             SIM_TWO -> {
                 imgVSimOne.beInvisible()
                 imgVSimTwo.beVisible()
             }

         }
         FloatingService.setSimCard(simNum)
    }
    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {
        val dm = getCurrentDisplayMetrics()
        // We have to set gravity for which the calculated position is relative.
        //do not remove commented code for params.width and height
        params.gravity = Gravity.TOP or Gravity.LEFT
//        params.width = (widthInDp * dm.density).toInt()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
//        params.height = (heightInDp * dm.density).toInt()
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
//        params.x = (dm.widthPixels - params.width) / 2
        params.x = 0
//        params.horizontalMargin = 8f

        params.y = (dm.heightPixels - params.height) / 2
    }


    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, 300, 80)
    }


    private fun initWindow() {
        // Using kotlin extension for views caused error, so good old findViewById is used
        rootView.findViewById<View>(R.id.imgBtnCloseIncommin).setOnClickListener { closeManually() }
        rootView.findViewById<View>(R.id.layoutWindowParent).setOnClickListener {
//            Toast.makeText(context, "Adding notes to be implemented.", Toast.LENGTH_SHORT).show()
        }
//       rootView.findViewById<View>(R.id.layoutWindowParent).registerDraggableTouchListener()
        rootView.findViewById<View>(R.id.layoutWindowParent).registerDraggableTouchListener(
            initialPosition = { Point(windowParams.x, windowParams.y) },
            positionListener = { x, y -> setPosition(x, y) }
        )


    }
    private fun setPosition(x: Int, y: Int) {
//        windowParams.x = x
        windowParams.y = y
        update()
        if(rootView.findViewById<View>(R.id.layoutDragIndicator).visibility == View.VISIBLE){
            rootView.findViewById<View>(R.id.layoutDragIndicator).beGone()
        }

    }
    private fun update() {
        try {
            windowManager.updateViewLayout(rootView, windowParams)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }
    init {
        initWindowParams()
        initWindow()
        context.sendBroadcast(Intent(IntentKeys.CLOSE_INCOMMING_VIEW))

    }

    private  suspend fun setCountryCode() = withContext(Dispatchers.IO)  {
        if(phoneNumber.isNotEmpty()){
            val country = countryCodeHelper?.getCountryName(phoneNumber)
            setcountry(country)
        }
    }

    private suspend fun setcountry(country: String?) = withContext(Dispatchers.Main) {
        Log.d(TAG, "setCountryCode: $country")
        tvLocation.text = country
        tvPhoneNumIncomming.text = phoneNumber
    }


    fun open() {
        try {
            windowManager.addView(rootView, windowParams)
            FloatingService.setWindowOpened(true)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }


    fun close() {
        try {
            windowManager.removeView(rootView)
            FloatingService.setWindowClosedManually(true)

        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
//                context.toast("unable to close window")

            Log.d(TAG, "close: $e")
        }
    }

    fun closeManually() {
        try {
//             val windowParams = WindowManager.LayoutParams(
//                0,
//                0,
//                0,
//                0,
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//                } else {
//                    WindowManager.LayoutParams.TYPE_PHONE
//                },
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
//                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                PixelFormat.TRANSLUCENT
//            )
//            windowManager.updateViewLayout(rootView, windowParams)
            windowManager.removeView(rootView)
            FloatingService.setWindowClosedManually(true)
//            context.stopFloatingService(true)

        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
//                context.toast("unable to close window")

            Log.d(TAG, "close: $e")
        }
    }



    suspend fun updateWithServerInfo(resFromServer: CntctitemForView, phoneNumber: String) = withContext(Dispatchers.Main){

        var name:String = ""
        var  location:String? = ""
        if(resFromServer.firstName.isNotEmpty()){
            name += resFromServer.firstName
            if(resFromServer.lastName.isNotEmpty()){
                name += " "+ resFromServer.lastName
            }
            FloatingService.cntctForView.fullNameServer = name
        }else if(resFromServer.nameInPhoneBook.isNotEmpty()){
            name = resFromServer.nameInPhoneBook
            FloatingService.cntctForView.nameInPhoneBook = resFromServer.nameInPhoneBook
        }
        var firstLetter = ""
        if(name.isEmpty()){
            // no name has found yet set phone number first digit as first letter
            name = phoneNumber
            if(phoneNumber.isNotEmpty())
                firstLetter = formatPhoneNumber(phoneNumber)[0].toString()
        }else {
            firstLetter =  name[0].toString().uppercase()
        }

        if(callerImageFoundFrom != IMAGE_FOUND_FROM_CPROVIDER){
            //only udpate image view with server image if there is no image in cprovider
            if(resFromServer.thumbnailImgServer.isNotEmpty()){
                imgVAvatar.beVisible()
                imgVAvatar.setImageBitmap(getDecodedBytes(resFromServer.thumbnailImgServer))
                tvFirstLetter.beGone()
            }else if(resFromServer.avatarGoogle.isNotEmpty()){
                imgVAvatar.beVisible()
                Glide.with(context).load(resFromServer.avatarGoogle)
                    .into(imgVAvatar)
                tvFirstLetter.beGone()
            }
            else{
                imgVAvatar.beInvisible()
                imgVAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circular_avatar_main_background))
                tvFirstLetter.beVisible()
            }
        }

        if(callerNameFoundFrom!= NAME_FOUND_FROM_CPROVIDER){
            tvFirstLetter.text = firstLetter
            if(!resFromServer.location.isNullOrEmpty()){
                location = resFromServer.location
            }else if(!resFromServer.country.isNullOrEmpty()){
                location = resFromServer.country
            }
            if(name.isNotEmpty()){
                tvName.text = name
            }
        }

        if(resFromServer.isVerifiedUser){
            tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.ic_baseline_verified_2), null)
        }else {
            tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
        context.toggleUserBadge(imgVUserBadgeBg, imgVUserIcon, resFromServer.hUid)

        if(resFromServer.spammCount> SPAM_THRESHOLD_VALUE){
            layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background_spam )
        }else{
            layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background )
        }

        FloatingService.cntctForView.firstName = resFromServer.firstName
        FloatingService.cntctForView.lastName = resFromServer.lastName
        FloatingService.cntctForView.nameInLocalPhoneBook = resFromServer.nameInLocalPhoneBook
        FloatingService.cntctForView.avatarGoogle = resFromServer.avatarGoogle
        FloatingService.cntctForView.thumbnailImgServer = resFromServer.thumbnailImgServer
        FloatingService.cntctForView.spammCount = resFromServer.spammCount
        FloatingService.cntctForView.isVerifiedUser = resFromServer.isVerifiedUser
        FloatingService.cntctForView.hUid = resFromServer.hUid

    }
    fun updateWithDummyData(){
                tvName.text = "Thomas Morton"
        tvName.text = "Sales Spam"
//        tvPhoneNumIncomming.text = "+911234567890"
        tvPhoneNumIncomming.text = "+911404567890"
        tvLocation.text = "IN"
        imgVAvatar.beVisible()
        imgVAvatar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_block_24))
//        loadImage(context, imgVAvatar, "content://com.android.contacts/contacts/4826/photo" )
        layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background_spam )
        imgVAvatar.beVisible()
        tvFirstLetter.beInvisible()
        tvFirstLetter.text = "T"
        tvFirstLetter.beInvisible()

    }

    suspend fun updateWithcontentProviderInfo(contactInfoInCprovider: Contact) = withContext(Dispatchers.Main) {

        var firstName:String? = ""
        var firstLetter:String?   = ""
        var photoThumbnailUri:String? = ""
//        tvLocation.text = countryCodeHelper?.getCountryCode(phoneNumber)
        if(!contactInfoInCprovider.nameInLocalPhoneBook.isNullOrEmpty()){
            firstName = contactInfoInCprovider.nameInLocalPhoneBook
        }

        if(!contactInfoInCprovider.thumbnailInCprovider.isNullOrEmpty()){
            photoThumbnailUri = contactInfoInCprovider.photoThumnailServer
            loadImage(context, imgVAvatar, contactInfoInCprovider.thumbnailInCprovider)
            setCallerImageFoundFrom(IMAGE_FOUND_FROM_CPROVIDER)
            tvFirstLetter.beInvisible()
        }
        if(!firstName.isNullOrEmpty()){
            tvName.text = firstName
            firstLetter = firstName[0].toString()
            tvFirstLetter.text = firstLetter
            setCallerNameFoundFrom(NAME_FOUND_FROM_CPROVIDER)
        }
        FloatingService.cntctForView.nameInLocalPhoneBook = contactInfoInCprovider.nameInLocalPhoneBook
        FloatingService.cntctForView.thumbnailImgCp = contactInfoInCprovider.thumbnailInCprovider
    }

    fun setCallerNameFoundFrom(foundFrom:Int){
        callerNameFoundFrom = foundFrom
    }

    fun setCallerImageFoundFrom(foundFrom:Int){
        callerNameFoundFrom = foundFrom
    }

    suspend fun setPhoneNum(num:String) = withContext(Dispatchers.IO) {
        phoneNumber = num
        setCountryCode()

    }

    fun setwindowSpamColor() {
        layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background_spam )
    }

    fun setSpamThreshold(spamThreshold: Int) {
        SPAM_THRESHOLD_VALUE= spamThreshold
    }

    companion object{
        var SPAM_THRESHOLD_VALUE = Constants.DEFAULT_SPAM_THRESHOLD

        const val TAG = "__Window"
        const val NAME_SEARCHING = 0 // caller name still not identified
        const val NAME_FOUND_FROM_CPROVIDER = 1
        const val NAME_FOUND_FROM_SERVER = 2

        const val IMAGE_SEARCHING = -1
        const val IMAGE_FOUND_FROM_CPROVIDER = 3
        const val IMAGE_FOUND_FROM_SERVER = 4


    }

}
