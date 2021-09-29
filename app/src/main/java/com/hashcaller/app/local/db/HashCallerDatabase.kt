package com.hashcaller.app.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hashcaller.app.local.db.blocklist.*
import com.hashcaller.app.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.hashcaller.app.local.db.blocklist.mutedCallers.MutedCallers
import com.hashcaller.app.local.db.contactInformation.ContactLastSyncedDate
import com.hashcaller.app.local.db.contactInformation.ContactTable
import com.hashcaller.app.local.db.contactInformation.IContactIformationDAO
import com.hashcaller.app.local.db.contactInformation.IContactLastSycnedDateDAO
import com.hashcaller.app.local.db.contacts.ContactAddresses
import com.hashcaller.app.local.db.contacts.IContactAddressesDao
import com.hashcaller.app.local.db.sms.Converters
import com.hashcaller.app.local.db.sms.SMSOutBox
import com.hashcaller.app.local.db.sms.SmsOutboxListDAO
import com.hashcaller.app.local.db.sms.block.BlockedOrSpamSenders
import com.hashcaller.app.local.db.sms.block.IBlockedOrSpamSendersDAO
import com.hashcaller.app.local.db.sms.mute.IMutedSendersDAO
import com.hashcaller.app.local.db.sms.mute.MutedSenders
import com.hashcaller.app.local.db.sms.search.ISmsQueriesDAO
import com.hashcaller.app.local.db.sms.search.SmsSearchQueries
import com.hashcaller.app.local.db.update.IUpdateAndPriorityDao
import com.hashcaller.app.local.db.update.UpdateAndPriority
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.IUserHashedNumDao
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserHashedNumber
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfo
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.db.ICallLogDAO
import com.hashcaller.app.view.ui.hashworker.MyContacts
import com.hashcaller.app.view.ui.hashworker.HashedNumber
import com.hashcaller.app.view.ui.hashworker.IHashedContactsDAO
import com.hashcaller.app.view.ui.hashworker.IHashedNumbersDAO
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable

/**
 * Created by Jithin KG on 03,July,2020
 * important we need to pass the newly created tables in the @Database
 */
@Database(entities = arrayOf(BlockedListPattern::class,
    ContactTable::class,
    SMSOutBox::class,
    SpammerInfo::class,
    ContactLastSyncedDate::class,
    CallersInfoFromServer::class,
    UserInfo::class,
    MutedSenders::class,
    BlockedOrSpamSenders::class,
    SmsSearchQueries::class,
    MutedCallers::class,
    ContactAddresses::class,
    CallLogTable::class,
    SmsThreadTable::class,
    UserHashedNumber::class,
    HashedNumber::class,
    MyContacts::class,
    SpamThresholdUpdatedDate::class,
    UpdateAndPriority::class

), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HashCallerDatabase: RoomDatabase() {
        abstract fun blocklistDAO() : BlockedLIstDao
        abstract fun contactInformationDAO() : IContactIformationDAO
        abstract fun smsDAO(): SmsOutboxListDAO
        abstract fun spamListDAO(): SpamListDAO
        abstract fun callersInfoFromServerDAO(): CallersInfoFromServerDAO
        abstract fun  contactLastSyncedDateDAO():IContactLastSycnedDateDAO
        abstract fun userInfoDAo(): UserInfoDAO
        abstract fun mutedSendersDAO(): IMutedSendersDAO
        abstract fun mutedCallersDAO(): IMutedCallersDAO
        abstract fun blockedOrSpamSendersDAO(): IBlockedOrSpamSendersDAO
        abstract fun smsSearchQueriesDAO(): ISmsQueriesDAO
        abstract fun contactAddressesDAO(): IContactAddressesDao
        abstract fun callLogDAO(): ICallLogDAO
        abstract fun smsThreadsDAO() : ISMSThreadsDAO
        abstract fun userHashedNumDAO() : IUserHashedNumDao
        abstract fun hashedNumDAO() : IHashedNumbersDAO
        abstract fun hashedContactsDAO() : IHashedContactsDAO
        abstract fun spamThresholdUpdateDAO() : ISpamThresholdLastUpdatedDao
        abstract fun updateAndPriorityDao() : IUpdateAndPriorityDao


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