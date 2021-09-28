package com.hashcaller.app.view.ui.hashworker

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table containing contacts that is synced with server for maintaining list of contacts that is collected from
 * user
  */
@Keep
@Entity(tableName = "my_synced_contacts")
data class MyContacts(
    @PrimaryKey() val number: String,

    ) {

}