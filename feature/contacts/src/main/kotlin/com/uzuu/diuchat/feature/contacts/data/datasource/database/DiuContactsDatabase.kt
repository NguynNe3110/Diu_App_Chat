package com.uzuu.diuchat.feature.contacts.data.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.uzuu.diuchat.feature.contacts.data.datasource.database.dao.ContactDao
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity

@Database(
    entities = [
        ContactEntity::class,
        ConnectionRequestEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DiuContactsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
