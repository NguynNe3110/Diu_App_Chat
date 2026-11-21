package com.uzuu.diuchat.feature.contacts.data.repository

import com.uzuu.diuchat.feature.contacts.data.datasource.database.dao.ContactDao
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsRepositoryImpl(
    private val contactDao: ContactDao,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : ContactsRepository {

    init {
        coroutineScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        if (contactDao.getCount() == 0) {
            contactDao.insertAll(createInitialContacts().map { it.toEntity() })\n        }
        if (contactDao.getRequestCount() == 0) {
            contactDao.insertAllRequests(createInitialRequests().map { it.toEntity() })
        }
    }

    override fun getContacts(): Flow<List<Contact>> = contactDao.observeContacts()
        .onStart { seedInitialDataIfNeeded() }
        .map { entities -> entities.map { it.toDomain() } }

    override fun getPendingRequests(): Flow<List<ConnectionRequest>> = contactDao.observeRequests()
        .onStart { seedInitialDataIfNeeded() }
        .map { entities -> entities.map { it.toDomain() } }

    override fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: ContactCircle,
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val requestEntity = contactDao.getRequestById(requestId)
                ?: throw IllegalArgumentException("Lời mời không tồn tại hoặc đã xử lý")

            if (accept) {
                val newContact = ContactEntity(
                    id = requestEntity.senderId,
                    name = requestEntity.senderName,
                    initials = requestEntity.senderInitials,
                    phone = requestEntity.senderPhone,
                    circle = circle.name,
                    moodColorHex = requestEntity.senderMoodColorHex,
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
    }

    override suspend fun searchUserByPhone(phone: String): Result<Contact?> = runCatching {
        withContext(Dispatchers.IO) {
            val clean = phone.replace("\\s".toRegex(), "")
            contactDao.getAllContacts()
                .map { it.toDomain() }
                .find { it.phone.replace("\\s".toRegex(), "") == clean }
        }
    }

    override fun getMyInviteLink(): Flow<String> = flowOf("diu.vn/linh/7K2M9P")

    private fun ContactEntity.toDomain(): Contact = Contact(
        id = id,
        name = name,
        initials = initials,
        phone = phone,
        circle = runCatching { ContactCircle.valueOf(circle) }.getOrDefault(ContactCircle.FRIENDS),
        moodColorHex = moodColorHex,
        nickname = nickname,
        anniversary = anniversary,
        isQuiet = isQuiet,
        lastSeen = lastSeen,
    )

    private fun Contact.toEntity(): ContactEntity = ContactEntity(
        id = id,
        name = name,
        initials = initials,
        phone = phone,
        circle = circle.name,
        moodColorHex = moodColorHex,
        nickname = nickname,
        anniversary = anniversary,
        isQuiet = isQuiet,
        lastSeen = lastSeen,
    )

    private fun ConnectionRequestEntity.toDomain(): ConnectionRequest = ConnectionRequest(
        id = id,
        senderId = senderId,
        senderName = senderName,
        senderInitials = senderInitials,
        senderPhone = senderPhone,
        senderMoodColorHex = senderMoodColorHex,
        proposedCircle = runCatching { ContactCircle.valueOf(proposedCircle) }.getOrDefault(ContactCircle.FRIENDS),
        createdAt = createdAt,
    )

    private fun ConnectionRequest.toEntity(): ConnectionRequestEntity = ConnectionRequestEntity(
        id = id,
        senderId = senderId,
        senderName = senderName,
        senderInitials = senderInitials,
        senderPhone = senderPhone,
        senderMoodColorHex = senderMoodColorHex,
        proposedCircle = proposedCircle.name,
        createdAt = createdAt,
    )

    companion object {
        fun createInitialContacts(): List<Contact> = listOf(
            Contact(
                id = "c1",
                name = "Minh An",
                initials = "MA",
                phone = "0912 345 678",
                circle = ContactCircle.LOVE,
                moodColorHex = "#F5A894",
                nickname = "Anh thương",
                anniversary = "24/11",
                isQuiet = false,
                lastSeen = "Vừa xong",
            ),
            Contact(
                id = "c2",
                name = "Mẹ",
                initials = "M",
                phone = "0903 111 222",
                circle = ContactCircle.FAMILY,
                moodColorHex = "#F1C574",
                nickname = "Mẹ yêu",
                anniversary = "15/08",
                isQuiet = true,
                lastSeen = "Đang nghỉ ngơi",
            ),
            Contact(
                id = "c3",
                name = "Bảo Vy",
                initials = "BV",
                phone = "0988 777 666",
                circle = ContactCircle.FRIENDS,
                moodColorHex = "#A6C5A2",
                nickname = "Vy bạn thân",
                anniversary = "01/06",
                isQuiet = false,
                lastSeen = "10 phút trước",
            ),
            Contact(
                id = "c4",
                name = "Bà Ngoại",
                initials = "BN",
                phone = "0909 333 444",
                circle = ContactCircle.FAMILY,
                moodColorHex = "#C1B3DC",
                nickname = "Ngoại",
                anniversary = null,
                isQuiet = true,
                lastSeen = "20:00 hôm qua",
            ),
        )

        fun createInitialRequests(): List<ConnectionRequest> = listOf(
            ConnectionRequest(
                id = "req_1",
                senderId = "u_hoang",
                senderName = "Hoàng Nam",
                senderInitials = "HN",
                senderPhone = "0933 555 777",
                senderMoodColorHex = "#A6C5A2",
                proposedCircle = ContactCircle.FRIENDS,
                createdAt = "Hôm nay, 10:15",
            ),
        )
    }
}
