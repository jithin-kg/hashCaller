package com.nibble.hashcaller.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nibble.hashcaller.local.db.dao.BlockedLIstDao

/**
 * Created by Jithin KG on 03,July,2020
 */
@Database(entities = arrayOf(BlockedListPattern::class), version = 1, exportSchema = false)
abstract class HashCallerDatabase: RoomDatabase() {
        abstract fun blocklistDAO() : BlockedLIstDao

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