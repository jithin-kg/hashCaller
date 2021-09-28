package com.hashcaller.app.view.ui.call.db

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.google.gson.Gson
@Keep
data class CallLogAndInfoFromServer(
  @Embedded val callLogTable: CallLogTable,
  @Relation(parentColumn = "number", entityColumn = "contact_address")val callersInfoFromServer: CallersInfoFromServer?
)
{

  fun deepCopy() : CallLogAndInfoFromServer {
    return Gson().fromJson(Gson().toJson(this), this.javaClass)
  }
}
