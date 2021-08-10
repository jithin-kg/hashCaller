package com.hashcaller.view.ui.hashworker

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HashedDataRepository(private val hashedNumDao: IHashedNumbersDAO) {

     fun getLivedata(): LiveData<List<HashedNumber>?>{
       return hashedNumDao.getLivedata()
    }

    suspend fun getAllData(): List<HashedNumber> = withContext(Dispatchers.IO) {
        return@withContext hashedNumDao.getAll()
    }
}