package com.nibble.hashcaller.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.local.db.sms.SMSOutBox
import com.nibble.hashcaller.local.db.sms.SmsOutboxListDAO

/**
 * Created by Jithin KG on 03,July,2020
 * important we need to pass the newly created tables in the @Database
 */
@Database(entities = arrayOf(BlockedListPattern::class, ContactTable::class, SMSOutBox::class), version = 1, exportSchema = false)
abstract class HashCallerDatabase: RoomDatabase() {
        abstract fun blocklistDAO() : BlockedLIstDao
        abstract fun contactInformationDAO() : IContactIformationDAO
        abstract fun smsDAO(): SmsOutboxListDAO

    companion object{

        //singleton prevents multiple instances of database opening at same time
        @Volatile
        private var INSTANCE : HashCallerDatabase?= null
        fun getDatabaseInstance(context : Context) : HashCallerDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            //passing this to synchronized function ensure that this function will
            //only be accessible to one thread at a time, ie synchronised access
            synchronized(this){
               val instance =  Room.databaseBuilder(context, HashCallerDatabase::class.java,
                    "hash_caller_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}