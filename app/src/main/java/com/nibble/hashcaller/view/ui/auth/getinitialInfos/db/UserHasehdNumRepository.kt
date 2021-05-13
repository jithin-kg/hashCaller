package com.nibble.hashcaller.view.ui.auth.getinitialInfos.db

import android.content.Context
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserHasehdNumRepository(
    private val userHashedNumDAO: IHashedNumDao,
    private val countryCodeHelper: CountrycodeHelper
) {

    /**
     * function to save hashed phone num to db, phone number hashed by secret algorithm
     *
     */
    suspend fun saveUserPhoneHash(hashedPhoneNum: String, formattedPhoneNum: String) = withContext(Dispatchers.IO) {
        userHashedNumDAO?.deleteAll()
        userHashedNumDAO?.insert(UserHashedNumber(hashedPhoneNum, formattedPhoneNum))
    }

    suspend fun getHasehedNumOfuser(): UserHashedNumber?  = withContext(Dispatchers.IO){
        return@withContext userHashedNumDAO?.getHash()
    }

    fun getCoutryCode(): String {
       return countryCodeHelper.getCountrycode()
    }

    fun getCoutryISO(): String {
        return countryCodeHelper.getCountryISO()
    }

    fun getCoutryusinLibPhonenumber(context: Context){
//        val util = PhoneNumberUtil.createInstance(context)
//        val numProto = util.parse("917012289206", "")
//        numProto.countryCode
    }
}