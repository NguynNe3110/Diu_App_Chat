package com.uzuu.diuchat.feature.contacts.presentation.screen

import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetContactsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetPendingRequestsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.RespondConnectionRequestUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import com.uzuu.diuchat.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ContactsViewModelTest {
    private val mockGetContactsUseCase: GetContactsUseCase = mockk()
    private val mockGetPendingRequestsUseCase: GetPendingRequestsUseCase = mockk()
    private val mockRespondConnectionRequestUseCase: RespondConnectionRequestUseCase = mockk()
    private val mockContactsRepository: ContactsRepository = mockk()

    private val testContacts =
        listOf(
            Contact(
                id = "c1",
                name = "Minh An",
                initials = "MA",
                phone = "0912 345 678",
                circle = ContactCircle.LOVE,
                moodColorHex = "#F7A3A9",
                nickname = "Anh thương",
            ),
            Contact(
                id = "c2",
                name = "Mẹ",
                initials = "M",
                phone = "0903 111 222",
                circle = ContactCircle.FAMILY,
                moodColorHex = "#F4CA84",
            ),
        )

    private val testRequests =
        listOf(
            ConnectionRequest(
                id = "req_1",
                senderId = "u_hoang",
                senderName = "Hoàng",
                senderInitials = "H",
                senderPhone = "0933 555 777",
                senderMoodColorHex = "#A6C5A2",
                proposedCircle = ContactCircle.FRIENDS,
                createdAt = "Hôm nay",
            ),
        )

    private lateinit var sut: ContactsViewModel

    @BeforeEach
    fun setUp() {
        every { mockGetContactsUseCase() } returns flowOf(testContacts)
        every { mockGetPendingRequestsUseCase() } returns flowOf(testRequests)

        sut =
            ContactsViewModel(
                mockGetContactsUseCase,
                mockGetPendingRequestsUseCase,
                mockRespondConnectionRequestUseCase,
                mockContactsRepository,
            )
    }

    @Test
    fun `initial load sets contacts and requests`() = runTest {
        advanceUntilIdle()

        val state = sut.uiStateFlow.value
        state.contacts shouldBeEqualTo testContacts
        state.pendingRequests shouldBeEqualTo testRequests
        state.filteredContacts.size shouldBeEqualTo 2
    }

    @Test
    fun `onSelectCircle filters contacts by circle`() = runTest {
        advanceUntilIdle()

        sut.onSelectCircle(ContactCircle.LOVE)

        val state = sut.uiStateFlow.value
        state.selectedCircle shouldBeEqualTo ContactCircle.LOVE
        state.filteredContacts.size shouldBeEqualTo 1
        state.filteredContacts.first().name shouldBeEqualTo "Minh An"
    }

    @Test
    fun `onSearchQueryChange filters contacts by name or nickname`() = runTest {
        advanceUntilIdle()

        sut.onSearchQueryChange("thương")

        val state = sut.uiStateFlow.value
        state.searchQuery shouldBeEqualTo "thương"
        state.filteredContacts.size shouldBeEqualTo 1
        state.filteredContacts.first().name shouldBeEqualTo "Minh An"
    }

    @Test
    fun `onAcceptRequest delegates to usecase`() = runTest {
        advanceUntilIdle()

        coEvery {
            mockRespondConnectionRequestUseCase("req_1", true, ContactCircle.FRIENDS)
        } returns Result.success(Unit)

        sut.onAcceptRequest("req_1", ContactCircle.FRIENDS)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockRespondConnectionRequestUseCase("req_1", true, ContactCircle.FRIENDS)
        }
    }

    @Test
    fun `onDeclineRequest delegates to usecase`() = runTest {
        advanceUntilIdle()

        coEvery {
            mockRespondConnectionRequestUseCase("req_1", false, any())
        } returns Result.success(Unit)

        sut.onDeclineRequest("req_1")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            mockRespondConnectionRequestUseCase("req_1", false, any())
        }
    }

    @Test
    fun `onSelectMood filters contacts by mood hex and toggles off`() = runTest {
        advanceUntilIdle()

        sut.onSelectMood("#F7A3A9")

        val filtered = sut.uiStateFlow.value
        filtered.selectedMoodHex shouldBeEqualTo "#F7A3A9"
        filtered.filteredContacts.size shouldBeEqualTo 1
        filtered.filteredContacts.first().name shouldBeEqualTo "Minh An"

        sut.onSelectMood("#F7A3A9")

        val cleared = sut.uiStateFlow.value
        cleared.selectedMoodHex shouldBeEqualTo null
        cleared.filteredContacts.size shouldBeEqualTo 2
    }

    @Test
    fun `onSubmitInviteCode handles blank and valid codes`() = runTest {
        advanceUntilIdle()

        sut.onSubmitInviteCode("   ")
        sut.uiStateFlow.value.scanResultMessage shouldBeEqualTo "Vui lòng nhập @username, số điện thoại hoặc mã QR"

        coEvery { mockContactsRepository.connectByInviteCode("diu.vn/minh/8X3K2P") } returns Result.success(testContacts.first())
        sut.onInviteCodeInputChange("diu.vn/minh/8X3K2P")
        sut.onSubmitInviteCode()
        advanceUntilIdle()

        val state = sut.uiStateFlow.value
        state.inviteCodeInput shouldBeEqualTo ""
        state.scanResultMessage shouldBeEqualTo "Đã kết nối với Minh An"
    }

    @Test
    fun `onSelectMood matches case-insensitive AppPalette mood hex`() = runTest {
        advanceUntilIdle()

        sut.onSelectMood("#f4ca84")

        val filtered = sut.uiStateFlow.value
        filtered.selectedMoodHex shouldBeEqualTo "#f4ca84"
        filtered.filteredContacts.size shouldBeEqualTo 1
        filtered.filteredContacts.first().name shouldBeEqualTo "Mẹ"
    }
}
