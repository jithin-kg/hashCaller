package com.hashcaller.app.utils.updatemanager

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.network.RetrofitClient
import com.hashcaller.app.network.user.IuserService
import com.hashcaller.app.utils.auth.TokenHelper

object UpdateMangerInjectorUtil {
        fun providerViewmodelFactory(context: Context) : UpdateManagerViewmodelFactory {
            val updateAndPriorityDao = HashCallerDatabase.getDatabaseInstance(context).updateAndPriorityDao()
            var retrofitService: IUpdateAndPriorityService = RetrofitClient.createaService(IUpdateAndPriorityService::class.java)
            val repository:UpdateManagerRepository = UpdateManagerRepository(updateAndPriorityDao, retrofitService, TokenHelper(FirebaseAuth.getInstance().currentUser))
            return UpdateManagerViewmodelFactory(context, repository)
        }
}