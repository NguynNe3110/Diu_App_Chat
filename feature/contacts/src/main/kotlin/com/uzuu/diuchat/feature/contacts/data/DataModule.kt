package com.uzuu.diuchat.feature.contacts.data

import androidx.room.Room
import com.uzuu.diuchat.feature.contacts.data.datasource.api.service.ContactsSupabaseService
import com.uzuu.diuchat.feature.contacts.data.datasource.database.DiuContactsDatabase
import com.uzuu.diuchat.feature.contacts.data.mapper.ConnectionRequestMapper
import com.uzuu.diuchat.feature.contacts.data.mapper.ContactMapper
import com.uzuu.diuchat.feature.contacts.data.repository.ContactsRepositoryImpl
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import io.github.jan.supabase.SupabaseClient
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

        single<ContactsSupabaseService> {
            ContactsSupabaseService(
                supabaseClient = getOrNull<SupabaseClient>(),
            )
        }

        single { ContactMapper() }
        single { ConnectionRequestMapper() }

        single<ContactsRepository> {
            ContactsRepositoryImpl(
                contactDao = get(),
                contactsSupabaseService = get(),
                contactMapper = get(),
                connectionRequestMapper = get(),
            )
        }
    }
