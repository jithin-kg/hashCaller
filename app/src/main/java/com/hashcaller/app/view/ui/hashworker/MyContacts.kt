package com.hashcaller.app.view.ui.hashworker

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table containing contacts that is synced with server for maintaining list of contacts that is collected from
 * user
  */
@Entity(tableName = "my_synced_contacts")
data class MyContacts(
    @PrimaryKey() val number: String,

    ) {

}