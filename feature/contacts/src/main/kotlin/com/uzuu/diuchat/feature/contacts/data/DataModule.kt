package com.uzuu.diuchat.feature.contacts.data

import androidx.room.Room
import com.uzuu.diuchat.feature.contacts.data.datasource.database.DiuContactsDatabase
import com.uzuu.diuchat.feature.contacts.data.repository.ContactsRepositoryImpl
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import org.koin.dsl.module

internal val dataModule =
    module {
        single {
            Room.databaseBuilder(
                get(),
                DiuContactsDatabase::class.java,
                "diu_contacts.db",
            ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        single { get<DiuContactsDatabase>().contactDao() }

        single<ContactsRepository> {
            ContactsRepositoryImpl(
                contactDao = get(),
            )
        }
    }
