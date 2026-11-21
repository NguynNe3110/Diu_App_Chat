package com.uzuu.diuchat.feature.home.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeRepositoryImplTest {
    private val sut = HomeRepositoryImpl()

    @Test
    fun `getStars returns seeded stars with group member count`() = runTest {
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
    fun `getRecentChats returns seeded chats`() = runTest {
        val chats = sut.getRecentChats().first()

        assertTrue(chats.isNotEmpty())
        assertEquals("minh", chats.first().id)
    }
}
