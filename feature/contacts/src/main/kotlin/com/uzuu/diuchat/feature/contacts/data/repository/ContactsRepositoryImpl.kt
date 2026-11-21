package com.uzuu.diuchat.feature.contacts.data.repository

import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

class ContactsRepositoryImpl : ContactsRepository {

    private val contactsFlow = MutableStateFlow(createInitialContacts())
    private val pendingRequestsFlow = MutableStateFlow(createInitialRequests())

    override fun getContacts(): Flow<List<Contact>> = contactsFlow.asStateFlow()

    override fun getPendingRequests(): Flow<List<ConnectionRequest>> = pendingRequestsFlow.asStateFlow()

    override suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: ContactCircle,
    ): Result<Unit> = runCatching {
        val request = pendingRequestsFlow.value.find { it.id == requestId }
            ?: throw IllegalArgumentException("Lời mời không tồn tại hoặc đã xử lý")

        pendingRequestsFlow.update { current -> current.filterNot { it.id == requestId } }

        if (accept) {
            val newContact = Contact(
                id = request.senderId,
                name = request.senderName,
                initials = request.senderInitials,
                phone = request.senderPhone,
                circle = circle,
                moodColorHex = request.senderMoodColorHex,
                nickname = null,
                anniversary = "Hôm nay",
                isQuiet = false,
                lastSeen = "Vừa kết nối",
            )
            contactsFlow.update { current ->
                if (current.none { it.id == newContact.id }) current + newContact else current
            }
        }
    }

    override suspend fun searchUserByPhone(phone: String): Result<Contact?> = runCatching {
        val clean = phone.replace("\\s".toRegex(), "")
        contactsFlow.value.find { it.phone.replace("\\s".toRegex(), "") == clean }
    }

    override fun getMyInviteLink(): Flow<String> = flowOf("diu.vn/linh/7K2M9P")

    companion object {
        private fun createInitialContacts(): List<Contact> = listOf(
            Contact(
                id = "c1",
                name = "Minh An",
                initials = "MA",
                phone = "0912 345 678",
                circle = ContactCircle.LOVE,
                moodColorHex = "#F5A894", // MoodThuong
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
                moodColorHex = "#F1C574", // MoodVui
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
                moodColorHex = "#A6C5A2", // MoodBinhYen
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
                moodColorHex = "#C1B3DC", // MoodNho
                nickname = "Ngoại",
                anniversary = null,
                isQuiet = true,
                lastSeen = "20:00 hôm qua",
            ),
        )

        private fun createInitialRequests(): List<ConnectionRequest> = listOf(
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
