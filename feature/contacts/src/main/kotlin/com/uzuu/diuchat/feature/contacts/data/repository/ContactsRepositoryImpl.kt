package com.uzuu.diuchat.feature.contacts.data.repository

import com.uzuu.diuchat.feature.contacts.data.datasource.api.service.ContactsSupabaseService
import com.uzuu.diuchat.feature.contacts.data.datasource.database.dao.ContactDao
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import com.uzuu.diuchat.feature.contacts.data.mapper.ConnectionRequestMapper
import com.uzuu.diuchat.feature.contacts.data.mapper.ContactMapper
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ContactsRepositoryImpl(
    private val contactDao: ContactDao,
    private val contactsSupabaseService: ContactsSupabaseService,
    private val contactMapper: ContactMapper,
    private val connectionRequestMapper: ConnectionRequestMapper,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : ContactsRepository {
    init {
        coroutineScope.launch {
            syncOrSeedInitialData()
        }
    }

    override fun getContacts(): Flow<List<Contact>> = contactDao.observeContacts().map { entities ->
        entities.map(contactMapper::toDomain)
    }

    override fun getPendingRequests(): Flow<List<ConnectionRequest>> = contactDao.observeRequests().map { entities ->
        entities.map(connectionRequestMapper::toDomain)
    }

    override suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: ContactCircle,
    ): Result<Unit> = runCatching {
        val request =
            contactDao.getRequestById(requestId)
                ?: throw IllegalArgumentException("Lời mời không tồn tại hoặc đã xử lý")

        contactsSupabaseService.respondToRequest(
            requestId = requestId,
            accept = accept,
            circle = circle.name,
        )

        if (accept) {
            val newContact =
                ContactEntity(
                    id = request.senderId,
                    name = request.senderName,
                    initials = request.senderInitials,
                    phone = request.senderPhone,
                    circle = circle.name,
                    moodColorHex = request.senderMoodColorHex,
                    nickname = null,
                    anniversary = "Hôm nay",
                    isQuiet = false,
                    lastSeen = "Vừa kết nối",
                )
            contactDao.acceptConnectionRequest(requestId, newContact)
        } else {
            contactDao.deleteRequest(requestId)
        }
    }

    override suspend fun searchUserByPhone(phone: String): Result<Contact?> = runCatching {
        val clean = phone.replace("\\s".toRegex(), "")
        val localMatch =
            contactDao.getAllContacts()
                .find { it.phone.replace("\\s".toRegex(), "") == clean }
                ?.let(contactMapper::toDomain)

        localMatch ?: contactsSupabaseService.searchUserByPhone(clean)?.let(contactMapper::toDomain)
    }

    override fun getMyInviteLink(): Flow<String> = flow {
        val remoteUrl = contactsSupabaseService.fetchMyInviteLink()?.url
        emit(remoteUrl ?: DEFAULT_INVITE_LINK)
    }

    suspend fun syncOrSeedInitialData() {
        val remoteSync = contactsSupabaseService.syncContacts()
        if (remoteSync != null) {
            if (remoteSync.contacts.isNotEmpty()) {
                contactDao.insertAll(remoteSync.contacts.map(contactMapper::toEntity))
            }
            if (remoteSync.pendingRequests.isNotEmpty()) {
                contactDao.insertAllRequests(remoteSync.pendingRequests.map(connectionRequestMapper::toEntity))
            }
            return
        }
        seedInitialDataIfNeeded()
    }

    private suspend fun seedInitialDataIfNeeded() {
        if (contactDao.getCount() == 0) {
            contactDao.insertAll(createInitialContacts())
        }
        if (contactDao.getRequestCount() == 0) {
            contactDao.insertAllRequests(createInitialRequests())
        }
    }

    companion object {
        private const val DEFAULT_INVITE_LINK = "diu.vn/linh/7K2M9P"

        private fun createInitialContacts(): List<ContactEntity> = listOf(
            ContactEntity(
                id = "c1",
                name = "Minh An",
                initials = "MA",
                phone = "0912 345 678",
                circle = ContactCircle.LOVE.name,
                moodColorHex = "#F7A3A9",
                nickname = "Anh thương",
                anniversary = "24/11",
                isQuiet = false,
                lastSeen = "Vừa xong",
            ),
            ContactEntity(
                id = "c2",
                name = "Mẹ",
                initials = "M",
                phone = "0903 111 222",
                circle = ContactCircle.FAMILY.name,
                moodColorHex = "#F4CA84",
                nickname = "Mẹ yêu",
                anniversary = "15/08",
                isQuiet = true,
                lastSeen = "Đang nghỉ ngơi",
            ),
            ContactEntity(
                id = "c3",
                name = "Bảo Vy",
                initials = "BV",
                phone = "0988 777 666",
                circle = ContactCircle.FRIENDS.name,
                moodColorHex = "#A2D7AC",
                nickname = "Vy bạn thân",
                anniversary = "01/06",
                isQuiet = false,
                lastSeen = "10 phút trước",
            ),
            ContactEntity(
                id = "c4",
                name = "Bà Ngoại",
                initials = "BN",
                phone = "0909 333 444",
                circle = ContactCircle.FAMILY.name,
                moodColorHex = "#C5B3EA",
                nickname = "Ngoại",
                anniversary = null,
                isQuiet = true,
                lastSeen = "20:00 hôm qua",
            ),
        )

        private fun createInitialRequests(): List<ConnectionRequestEntity> = listOf(
            ConnectionRequestEntity(
                id = "req_1",
                senderId = "u_hoang",
                senderName = "Hoàng Nam",
                senderInitials = "HN",
                senderPhone = "0933 555 777",
                senderMoodColorHex = "#A2D7AC",
                proposedCircle = ContactCircle.FRIENDS.name,
                createdAt = "Hôm nay, 10:15",
            ),
        )
    }
}
