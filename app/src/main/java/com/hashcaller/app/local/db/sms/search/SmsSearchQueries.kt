package com.hashcaller.app.local.db.sms.search

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 *  Table to store sms search history
 *  new item is inserted when user click on a search result
 */
@Keep
@Entity(tableName = "sms_search_queries")
data class SmsSearchQueries(@PrimaryKey() val query: String) {

}