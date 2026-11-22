package com.uzuu.diuchat.feature.home.data.repository

import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import com.uzuu.diuchat.feature.home.data.datasource.api.response.HomeOverviewResponse
import com.uzuu.diuchat.feature.home.data.datasource.api.service.HomeSupabaseService
import com.uzuu.diuchat.feature.home.data.mapper.HomeMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HomeRepositoryImplTest {
    private val homeSupabaseService: HomeSupabaseService = mockk()
    private val homeMapper = HomeMapper()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var sut: HomeRepositoryImpl

    @BeforeEach
    fun setUp() {
        every { homeSupabaseService.isAuthenticated() } returns false
        coEvery { homeSupabaseService.fetchOverview() } returns null
        sut = HomeRepositoryImpl(
            homeSupabaseService = homeSupabaseService,
            homeMapper = homeMapper,
            coroutineScope = testScope,
        )
    }

    @Test
    fun `getStars returns seeded stars when remote overview is null`() = runTest {
        val stars = sut.getStars(isQuiet = false).first()

        assertTrue(stars.isNotEmpty())
        val groupStar = stars.first { it.id == "nha" }
        assertEquals(3, groupStar.memberCount)
        assertFalse(stars.first { it.id == "minh" }.isQuiet)
    }

    @Test
    fun `getStars reflects quiet mode on primary star`() = runTest {
        val stars = sut.getStars(isQuiet = true).first()

        assertTrue(stars.first { it.id == "minh" }.isQuiet)
    }

    @Test
    fun `getRecentChats returns seeded chats when remote overview is null`() = runTest {
        val chats = sut.getRecentChats().first()

        assertTrue(chats.isNotEmpty())
        assertEquals("minh", chats.first().id)
    }

    @Test
    fun `syncFromRemote maps remote overview into stars and recent chats`() = runTest {
        coEvery { homeSupabaseService.fetchOverview() } returns HomeOverviewResponse(
            stars = listOf(
                HomeStarDto(
                    id = "remote-1",
                    name = "Linh",
                    initials = "L",
                    moodColorHex = "#A2D7AC",
                    memberCount = 2,
                ),
            ),
            recentChats = listOf(
                RecentChatDto(
                    id = "remote-chat-1",
                    contactName = "Linh",
                    initials = "L",
                    moodColorHex = "#A2D7AC",
                    lastMessage = "Chào buổi sáng",
                    timestamp = "08:00",
                    unreadCount = 1,
                ),
            ),
        )

        sut.syncFromRemote()

        val stars = sut.getStars(isQuiet = false).first()
        val chats = sut.getRecentChats().first()

        assertEquals(1, stars.size)
        assertEquals("remote-1", stars.first().id)
        assertEquals(2, stars.first().memberCount)
        assertEquals(1, chats.size)
        assertEquals("remote-chat-1", chats.first().id)
        assertEquals("Chào buổi sáng", chats.first().lastMessage)
    }

    @Test
    fun `HomeSupabaseService returns null safely when supabaseClient is null`() = runTest {
        val service = HomeSupabaseService(supabaseClient = null)

        assertFalse(service.isAuthenticated())
        assertNull(service.fetchOverview())
    }
}
