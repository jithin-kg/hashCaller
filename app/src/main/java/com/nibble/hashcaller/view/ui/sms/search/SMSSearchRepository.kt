package com.nibble.hashcaller.view.ui.sms.search

import com.nibble.hashcaller.local.db.sms.search.ISmsQueriesDAO
import com.nibble.hashcaller.local.db.sms.search.SmsSearchQueries

class SMSSearchRepository(private val smsQueriesDAO: ISmsQueriesDAO) {
    suspend fun insertSearchQueryToDB(queryText: String) {
            smsQueriesDAO.insert(SmsSearchQueries(queryText))
    }

    suspend fun getAllSearchHistory(): List<SmsSearchQueries> {
        return smsQueriesDAO.getAll()
    }

}