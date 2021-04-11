package com.nibble.hashcaller.view.ui.call.db

import androidx.room.Embedded
import androidx.room.Relation

data class CallLogAndInfoFromServer(
  @Embedded val callLogTable: CallLogTable,
  @Relation(parentColumn = "number", entityColumn = "contact_address")val callersInfoFromServer: CallersInfoFromServer?
)
{

}
