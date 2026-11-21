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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ContactsRepositoryImpl(
    private val contactDao: ContactDao,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : ContactsRepository {
    init {
        coroutineScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    override fun getContacts(): Flow<List<Contact>> = contactDao.observeContacts().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getPendingRequests(): Flow<List<ConnectionRequest>> = contactDao.observeRequests().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: ContactCircle,
    ): Result<Unit> = runCatching {
        val request =
            contactDao.getRequestById(requestId)
                ?: throw IllegalArgumentException("Lời mời không tồn tại hoặc đã xử lý")

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
        contactDao.getAllContacts()
            .find { it.phone.replace("\\s".toRegex(), "") == clean }
            ?.toDomain()
    }

    override fun getMyInviteLink(): Flow<String> = flowOf("diu.vn/linh/7K2M9P")

    private suspend fun seedInitialDataIfNeeded() {
        if (contactDao.getCount() == 0) {
            contactDao.insertAll(createInitialContacts())
        }
        if (contactDao.getRequestCount() == 0) {
            contactDao.insertAllRequests(createInitialRequests())
        }
    }

    private fun ContactEntity.toDomain(): Contact = Contact(
        id = id,
        name = name,
        initials = initials,
        phone = phone,
        circle = ContactCircle.entries.find { it.name == circle } ?: ContactCircle.FRIENDS,
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
        proposedCircle = ContactCircle.entries.find { it.name == proposedCircle } ?: ContactCircle.FRIENDS,
        createdAt = createdAt,
    )

    companion object {
        private fun createInitialContacts(): List<ContactEntity> = listOf(
            ContactEntity(
                id = "c1",
                name = "Minh An",
                initials = "MA",
                phone = "0912 345 678",
                circle = ContactCircle.LOVE.name,
                moodColorHex = "#F5A894",
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
                moodColorHex = "#F1C574",
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
                moodColorHex = "#A6C5A2",
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
                moodColorHex = "#C1B3DC",
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
                senderMoodColorHex = "#A6C5A2",
                proposedCircle = ContactCircle.FRIENDS.name,
                createdAt = "Hôm nay, 10:15",
            ),
        )
    }
}
