package com.nibble.hashcaller.view.ui.call.floating

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Window(private val context: Context) {

    private var callerInfoFoundFrom = INFO_SEARCHING
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.window, null)
    private val tvName:TextView = rootView.findViewById<TextView>(R.id.txtVcallerNameWindow)
    private val tvFirstLetter:TextView = rootView.findViewById<TextView>(R.id.tvFistLetterWindow)
    private val tvLocation:TextView = rootView.findViewById(R.id.txtVLocaltionWindow)
    private val layoutInnerWindow: ConstraintLayout = rootView.findViewById(R.id.layoutInnerWindow)
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
        rootView.findViewById<View>(R.id.imgBtnCloseIncommin).setOnClickListener { close() }
        rootView.findViewById<View>(R.id.layoutWindowParent).setOnClickListener {
            Toast.makeText(context, "Adding notes to be implemented.", Toast.LENGTH_SHORT).show()
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
    }


    fun open() {
        try {
            windowManager.addView(rootView, windowParams)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }


    fun close() {
        try {
            windowManager.removeView(rootView)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
            Log.d(TAG, "close: $e")
        }
    }

    suspend fun updateWithServerInfo(resFromServer: CntctitemForView, phoneNumber: String) = withContext(Dispatchers.Main){
        if(callerInfoFoundFrom!= INFO_FOUND_FROM_CPROVIDER){
            //only update contents with server info iff info has not yet found from contentprovider
            //TODO check for image uri from server and local db, if img uri in db isNullOrEpty then show image from server if exists
            //todo cellular, country informations are to be shown, without this
            //country informations should be shown even if no internet, im using  io.michaelrocks:libphonenumbe, use it
            var name:String? = ""
            var  location:String? = ""
            name = (resFromServer.firstName + resFromServer.lastName).trim()
            if(name.isNullOrEmpty()){
                name = phoneNumber
            }

            var firstLetter = ""
            if(!resFromServer.firstName.isNullOrEmpty()){
                firstLetter = resFromServer.firstName[0].toString()
            }else if(!resFromServer.lastName.isNullOrEmpty()){
                firstLetter = resFromServer.lastName[0].toString()
            }else{
                firstLetter = formatPhoneNumber(phoneNumber)[0].toString()
            }

            if(!resFromServer.location.isNullOrEmpty()){
                location = resFromServer.location
            }else if(!resFromServer.country.isNullOrEmpty()){
                location = resFromServer.country
            }
            tvFirstLetter.text = firstLetter
            tvName.text = name
            tvLocation.text = location
        }
        if(resFromServer.spammCount> SPAM_THREASHOLD){
         layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background_spam )
        }else{
          layoutInnerWindow.background = ContextCompat.getDrawable(context,R.drawable.incomming_call_background )
        }

    }

    suspend fun updateWithcontentProviderInfo(contactInfoInCprovider: Contact) = withContext(Dispatchers.Main) {
        var firstName:String? = ""
        var firstLetter:String?   = ""
        var photoThumbnailUri:String? = ""
        if(!contactInfoInCprovider.name.isNullOrEmpty()){
            firstName = contactInfoInCprovider.name
        }

        if(!contactInfoInCprovider.photoThumnail.isNullOrEmpty()){
            photoThumbnailUri = contactInfoInCprovider.photoThumnail
        }
        if(!firstName.isNullOrEmpty()){
            tvName.text = firstName
            firstLetter = firstName[0].toString()
            tvFirstLetter.text = firstLetter
            setCallerInfoFoundFrom(INFO_FOUND_FROM_CPROVIDER)
        }

    }

fun setCallerInfoFoundFrom(foundFrom:Int){
    callerInfoFoundFrom = foundFrom
}
    companion object{
        const val TAG = "__Window"
        const val INFO_SEARCHING = 0
        const val INFO_FOUND_FROM_CPROVIDER = 1
        const val INFO_FOUND_FROM_SERVER = 2
    }

}