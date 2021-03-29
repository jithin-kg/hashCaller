package com.nibble.hashcaller.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nibble.hashcaller.local.db.blocklist.*
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.local.db.contactInformation.ContactLastSyncedDate
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.local.db.contactInformation.IContactLastSycnedDateDAO
import com.nibble.hashcaller.local.db.sms.Converters
import com.nibble.hashcaller.local.db.sms.SMSOutBox
import com.nibble.hashcaller.local.db.sms.SmsOutboxListDAO
import com.nibble.hashcaller.local.db.sms.block.BlockedOrSpamSenders
import com.nibble.hashcaller.local.db.sms.block.IBlockedOrSpamSendersDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.local.db.sms.mute.MutedSenders
import com.nibble.hashcaller.local.db.sms.search.ISmsQueriesDAO
import com.nibble.hashcaller.local.db.sms.search.SmsSearchQueries
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfo
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO

/**
 * Created by Jithin KG on 03,July,2020
 * important we need to pass the newly created tables in the @Database
 */
@Database(entities = arrayOf(BlockedListPattern::class,
    ContactTable::class,
    SMSOutBox::class,
    SpammerInfo::class,
    SMSSendersInfoFromServer::class,
    ContactLastSyncedDate::class,
    CallersInfoFromServer::class,
    UserInfo::class,
    MutedSenders::class,
    BlockedOrSpamSenders::class,
    SmsSearchQueries::class,
    MutedCallers::class
), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HashCallerDatabase: RoomDatabase() {
        abstract fun blocklistDAO() : BlockedLIstDao
        abstract fun contactInformationDAO() : IContactIformationDAO
        abstract fun smsDAO(): SmsOutboxListDAO
        abstract fun spamListDAO(): SpamListDAO
        abstract fun smsSenderInfoFromServerDAO(): SMSSendersInfoFromServerDAO
        abstract fun callersInfoFromServerDAO(): CallersInfoFromServerDAO
        abstract fun  contactLastSyncedDateDAO():IContactLastSycnedDateDAO
        abstract fun userInfoDAo(): UserInfoDAO
        abstract fun mutedSendersDAO(): IMutedSendersDAO
        abstract fun mutedCallersDAO(): IMutedCallersDAO
        abstract fun blockedOrSpamSendersDAO(): IBlockedOrSpamSendersDAO
        abstract fun smsSearchQueriesDAO(): ISmsQueriesDAO


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