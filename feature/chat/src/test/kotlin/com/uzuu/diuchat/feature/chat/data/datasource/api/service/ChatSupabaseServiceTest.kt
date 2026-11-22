package com.uzuu.diuchat.feature.chat.data.datasource.api.service

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ChatSupabaseServiceTest {
    private val sut = ChatSupabaseService(supabaseClient = null)

    @Test
    fun `when supabaseClient is null, auth helpers return null and realtime flow is empty`() = runTest {
        sut.currentUserId() shouldBeEqualTo null
        sut.currentUserDisplayName() shouldBeEqualTo null
        sut.observeRealtimeMessages("conv_1").toList() shouldBeEqualTo emptyList()
        sut.upsertMessage(
            MessageDto(
                id = "m1",
                conversationId = "conv_1",
                senderId = "u1",
                content = "hello",
            ),
        ).isSuccess shouldBeEqualTo true
    }
}
