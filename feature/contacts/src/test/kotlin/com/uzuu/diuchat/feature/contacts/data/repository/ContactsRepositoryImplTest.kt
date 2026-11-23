package com.uzuu.diuchat.feature.contacts.data.repository

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.InviteLinkDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.response.ContactsSyncResponse
import com.uzuu.diuchat.feature.contacts.data.datasource.api.service.ContactsSupabaseService
import com.uzuu.diuchat.feature.contacts.data.datasource.database.dao.ContactDao
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import com.uzuu.diuchat.feature.contacts.data.mapper.ConnectionRequestMapper
import com.uzuu.diuchat.feature.contacts.data.mapper.ContactMapper
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactsRepositoryImplTest {
    private val contactDao: ContactDao = mockk(relaxed = true)
    private val contactsSupabaseService: ContactsSupabaseService = mockk(relaxed = true)
    private val contactMapper = ContactMapper()
    private val connectionRequestMapper = ConnectionRequestMapper()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val testContactEntity = ContactEntity(
        id = "c1",
        name = "Minh An",
        initials = "MA",
        phone = "0912 345 678",
        circle = "LOVE",
        moodColorHex = "#F5A894",
        nickname = "Anh thương",
        anniversary = "24/11",
        isQuiet = false,
        lastSeen = "Vừa xong",
    )

    private val testRequestEntity = ConnectionRequestEntity(
        id = "req_1",
        senderId = "u_hoang",
        senderName = "Hoàng Nam",
        senderInitials = "HN",
        senderPhone = "0933 555 777",
        senderMoodColorHex = "#A6C5A2",
        proposedCircle = "FRIENDS",
        createdAt = "Hôm nay, 10:15",
    )

    private lateinit var sut: ContactsRepositoryImpl

    @BeforeEach
    fun setUp() {
        every { contactsSupabaseService.isConfigured() } returns false
        coEvery { contactDao.getCount() } returns 1
        coEvery { contactDao.getRequestCount() } returns 1
        coEvery { contactsSupabaseService.syncContacts() } returns null
        coEvery { contactsSupabaseService.fetchMyInviteLink() } returns null
        every { contactDao.observeContacts() } returns flowOf(listOf(testContactEntity))
        every { contactDao.observeRequests() } returns flowOf(listOf(testRequestEntity))

        sut = ContactsRepositoryImpl(
            contactDao = contactDao,
            contactsSupabaseService = contactsSupabaseService,
            contactMapper = contactMapper,
            connectionRequestMapper = connectionRequestMapper,
            coroutineScope = testScope,
        )
    }

    @Test
    fun `getContacts maps entities to domain models`() = runTest {
        val contacts = sut.getContacts().first()

        contacts.size shouldBeEqualTo 1
        contacts.first().id shouldBeEqualTo "c1"
        contacts.first().circle shouldBeEqualTo ContactCircle.LOVE
    }

    @Test
    fun `getPendingRequests maps entities to domain models`() = runTest {
        val requests = sut.getPendingRequests().first()

        requests.size shouldBeEqualTo 1
        requests.first().id shouldBeEqualTo "req_1"
        requests.first().proposedCircle shouldBeEqualTo ContactCircle.FRIENDS
    }

    @Test
    fun `respondToRequest with accept delegates to service and acceptConnectionRequest`() = runTest {
        coEvery { contactDao.getRequestById("req_1") } returns testRequestEntity

        val result = sut.respondToRequest("req_1", accept = true, circle = ContactCircle.LOVE)

        result.isSuccess.shouldBeTrue()
        coVerify(exactly = 1) {
            contactsSupabaseService.respondToRequest("req_1", true, "LOVE", "u_hoang")
            contactDao.acceptConnectionRequest(
                "req_1",
                match {
                    it.id == "u_hoang" && it.circle == "LOVE"
                },
            )
        }
    }

    @Test
    fun `respondToRequest with decline delegates to service and deleteRequest`() = runTest {
        coEvery { contactDao.getRequestById("req_1") } returns testRequestEntity

        val result = sut.respondToRequest("req_1", accept = false, circle = ContactCircle.FRIENDS)

        result.isSuccess.shouldBeTrue()
        coVerify(exactly = 1) {
            contactsSupabaseService.respondToRequest("req_1", false, "FRIENDS", "u_hoang")
            contactDao.deleteRequest("req_1")
        }
    }

    @Test
    fun `respondToRequest fails when request does not exist`() = runTest {
        coEvery { contactDao.getRequestById("unknown") } returns null

        val result = sut.respondToRequest("unknown", accept = true, circle = ContactCircle.FRIENDS)

        result.isFailure.shouldBeTrue()
    }

    @Test
    fun `searchUserByPhone finds matching local contact`() = runTest {
        coEvery { contactDao.getAllContacts() } returns listOf(testContactEntity)

        val result = sut.searchUserByPhone("0912345678")

        result.isSuccess.shouldBeTrue()
        result.getOrNull()?.id shouldBeEqualTo "c1"
    }

    @Test
    fun `searchUserByPhone falls back to remote service when not found locally`() = runTest {
        coEvery { contactDao.getAllContacts() } returns emptyList()
        coEvery { contactsSupabaseService.searchUserByPhone("0999888777") } returns ContactDto(
            id = "remote_c",
            name = "Remote Friend",
            initials = "RF",
            phone = "0999888777",
            circle = "FRIENDS",
        )

        val result = sut.searchUserByPhone("0999 888 777")

        result.isSuccess.shouldBeTrue()
        result.getOrNull()?.id shouldBeEqualTo "remote_c"
    }

    @Test
    fun `getMyInviteLink returns remote url when available and falls back otherwise`() = runTest {
        sut.getMyInviteLink().first() shouldBeEqualTo "https://diu-chat.vercel.app/invite/7K2M9P"

        coEvery { contactsSupabaseService.fetchMyInviteLink() } returns InviteLinkDto(
            code = "ABC123",
            url = "https://diu-chat.vercel.app/invite/ABC123",
        )
        sut.refreshFromRemote()
        sut.getMyInviteLink().first() shouldBeEqualTo "https://diu-chat.vercel.app/invite/ABC123"
    }

    @Test
    fun `syncOrSeedInitialData inserts remote contacts and requests when sync succeeds`() = runTest {
        coEvery { contactsSupabaseService.syncContacts() } returns ContactsSyncResponse(
            contacts = listOf(
                ContactDto(
                    id = "c_remote",
                    name = "Lan",
                    initials = "L",
                    phone = "0911000111",
                    circle = "FAMILY",
                ),
            ),
            pendingRequests = listOf(
                ConnectionRequestDto(
                    id = "req_remote",
                    senderId = "u_remote",
                    senderName = "Tùng",
                    senderInitials = "T",
                    senderPhone = "0922000222",
                ),
            ),
        )

        sut.syncOrSeedInitialData()

        coVerify(exactly = 1) {
            contactDao.replaceAll(
                contacts = match { it.size == 1 && it.first().id == "c_remote" },
                requests = match { it.size == 1 && it.first().id == "req_remote" },
            )
        }
    }

    @Test
    fun `ContactsSupabaseService handles null supabaseClient safely`() = runTest {
        val service = ContactsSupabaseService(supabaseClient = null)

        service.syncContacts().shouldBeNull()
        service.fetchContacts().shouldBeNull()
        service.fetchPendingRequests().shouldBeNull()
        service.respondToRequest("req_1", true, "FRIENDS").shouldBeFalse()
        service.searchUserByPhone("0912345678").shouldBeNull()
        service.fetchMyInviteLink().shouldBeNull()
    }
}
