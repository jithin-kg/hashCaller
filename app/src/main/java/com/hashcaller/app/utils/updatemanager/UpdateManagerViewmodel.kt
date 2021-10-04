package com.hashcaller.app.utils.updatemanager

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.hashcaller.app.network.HttpStatusCodes
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_FAILED
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

class UpdateManagerViewmodel(private val context: Context, private val repository: UpdateManagerRepository) : ViewModel() {
    private lateinit var  appUpdateManager: AppUpdateManager

    /**
     * function checks for update
     * @return -1 if no update avaialble
     *  -1 to 5
     *  5 if update priority is immediate -> IMMEDIATE_UPDATE
     */
      fun checkForUpdate(): LiveData<Int> = liveData {
//        return suspendCoroutine<Int> { continuation ->
            try {
                appUpdateManager = AppUpdateManagerFactory.create(context)
                val appUpdateInfoTask = appUpdateManager.appUpdateInfo
                
                
//                val resServer = repository.getPriorityFromServer(41)
//                Log.d(TAG, "checkForUpdate: $resServer")
                
                
                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    viewModelScope.launch {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                            val versionCode = appUpdateInfo.availableVersionCode()
                            Log.d(TAG, "checkForUpdate: versionCode : $versionCode")
                            val priorityInLocal = repository.getPriorityByVersionCodeDb(versionCode)
                            if(priorityInLocal== null){
                               val resServer = repository.getPriorityFromServer(versionCode)
                                Log.d(TAG, "checkForUpdate:resServer $resServer")
                                if(resServer!= null){
                                    if(resServer.code() == HttpStatusCodes.STATUS_OK){
                                        resServer.body()?.data?.let {
                                            repository.setPriorityInDb(it)
                                            emit(it.priority)
                                        }
                                    }
                                }
                            }else {
                                emit(priorityInLocal.priority)
                            }
                            //check if the version code in server is this version code priority is immediate
//                       continuation.resumeWith(Result.success(IMMEDIATE_UPDATE))
                        }
                    }
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                }
            }catch (e:Exception){
                Log.d(TAG, "checkForUpdate:exception $e")
                emit(-1)
//               continuation.resumeWith(Result.success(FAILURE_CHECKING_UPDATE))
            }


//        }

    }
   companion object {
       const val NO_UPDATE_AVAILABLE = -1
       const val FLEXIBLE_UPDATE = 2
       const val IMMEDIATE_UPDATE = 5
       const val FAILURE_CHECKING_UPDATE = -2
       private const val TAG = "__UpdateManagerHelper"
   }
}