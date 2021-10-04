package com.hashcaller.app.utils.updatemanager

import android.util.Log
import com.hashcaller.app.local.db.update.IUpdateAndPriorityDao
import com.hashcaller.app.local.db.update.UpdateAndPriority
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.utils.GenericResponse
import com.hashcaller.app.utils.auth.TokenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Exception

class UpdateManagerRepository(
    private val updateAndPriorityDao: IUpdateAndPriorityDao,
    private val retrofitService: IUpdateAndPriorityService,
    private val tokenHelper: TokenHelper
) {
    suspend fun getPriorityByVersionCodeDb(versionCode: Int): UpdateAndPriority? {
      return updateAndPriorityDao.findByVersion(versionCode)
    }

    suspend fun getPriorityFromServer(versionCode: Int): Response<GenericResponse<GetPriorityDTO.Response?>>?  = withContext(Dispatchers.IO){
        var res: Response<GenericResponse<GetPriorityDTO.Response?>>?= null
        tokenHelper.getToken()?.let {
            res = retrofitService.getPriorityByUpdateVersionCode(token = it, GetPriorityDTO(versionCode) )
        }
        return@withContext res

    }

    suspend fun setPriorityInDb(data: GetPriorityDTO.Response) {
        updateAndPriorityDao.deleteAll()
        updateAndPriorityDao.insert(UpdateAndPriority(
            versionCode = data.versionCode,
            priority = data.priority
            ))
    }

}