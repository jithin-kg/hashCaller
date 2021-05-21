package com.nibble.hashcaller.view.ui.hashworker

import androidx.lifecycle.LiveData
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.IUserHashedNumDao

class HashedDataRepository(private val hashedNumDao: IHashedNumbersDAO) {

     fun getLivedata(): LiveData<List<HashedNumber>?>{
       return hashedNumDao.getLivedata()
    }
}