package com.hashcaller.view.ui.sms.db


/**
 * joined table of smsthread and server info
 */
//data class SMSThreadANDServerInfo(
//    @Embedded val smsThreadTable: SmsThreadTable,
//    @Relation(parentColumn = "contactAddress", entityColumn = "contact_address")val smsenderInfoFromServer: SMSSendersInfoFromServer?
//)
//{
//
//    fun deepCopy() : SMSThreadANDServerInfo {
//        return Gson().fromJson(Gson().toJson(this), this.javaClass)
//    }
//}