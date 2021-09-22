package com.hashcaller.app.view.ui.sms.individual.util

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat

import com.hashcaller.app.work.formatPhoneNumber

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)
//
//suspend fun Context.saveToken( key:String, value:String){
//    val wrapedKey =  stringPreferencesKey(key)
//    tokeDataStore.edit {
//        it[wrapedKey] = value
//    }
//}
//
// fun Context.getToken( key:String): Flow<String> {
//    val wrapedKey =  stringPreferencesKey(key)
//    val tokenFlow: Flow<String> = tokeDataStore.data.map {
//        it[wrapedKey]?:""
//    }
//    return tokenFlow
//}
/**
 * Request focus for searchview and show soft input focused on searchView
 * @param searchView Searchview
 * @param activity : Activity calling from
 */
fun Context.requestSearchViewFocus(
    searchView: SearchView,
    activity: AppCompatActivity ){
    val searchManager  =  getSystemService(AppCompatActivity.SEARCH_SERVICE) as SearchManager;
    searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName));
    searchView.setIconifiedByDefault(false);
    searchView.setFocusable(true);
    searchView.setIconified(false);
    searchView.requestFocusFromTouch();
}

fun Context.call(phoneNum:String){
    var formatedNum = "+" +formatPhoneNumber(phoneNum)
    val callIntent = Intent(Intent.ACTION_CALL)

        callIntent.data = Uri.parse("tel:$formatedNum")
    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
        return
    }
    startActivity(callIntent)
}
fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {

    }



}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

/**
 * method returns a pair <true,roleManage> if we need to request Screening role
 * else <false, null> or <false,roleManager >
 */
fun Context.shouldReqstScreeningRole(): Pair<Boolean, RoleManager?> {
    var shouldRequest = false
    var roleManager: RoleManager? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val isHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        if (!isHeld) {
            shouldRequest = true
            //ask the user to set your app as the default screening app
//            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
//            scrnRoleCallback.launch(intent)
//                startActivityForResult(intent, ROLE_SCREENING_APP_REQUEST_CODE)
        } else {
            //you are already the default screening app!
        }
    }
    return Pair(shouldRequest, roleManager)
}




