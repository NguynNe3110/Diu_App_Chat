package com.uzuu.diuchat.feature.chat.data.repository

import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ChatRepositoryImplTest {
    private val conversationDao: ConversationDao = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private val outboxDao: OutboxDao = mockk(relaxed = true)

    private val sut = ChatRepositoryImpl(
        conversationDao = conversationDao,
        messageDao = messageDao,
        outboxDao = outboxDao,
        database = null,
    )

    @Test
    fun `sendMessage creates message with PENDING status initially`() = runTest {
        val result = sut.sendMessage(
            conversationId = "conv_1",
            content = "Chào buổi sáng",
            mood = MoodType.BINHYEN,
        )

        result.status shouldBeEqualTo MessageStatus.PENDING
        result.content shouldBeEqualTo "Chào buổi sáng"
        result.conversationId shouldBeEqualTo "conv_1"

        coVerify(exactly = 1) {
            messageDao.insert(match { it.status == MessageStatus.PENDING.value })
        }
        coVerify(exactly = 1) {
            outboxDao.insert(match { it.status == MessageStatus.PENDING.value })
        }
        // Ensure no premature status flip to SENT occurs before real ACK
        coVerify(exactly = 0) {
            messageDao.updateStatus(any(), MessageStatus.SENT.value)
        }
    }

    @Test
    fun `sendPhotoMessage creates photo message with PENDING status`() = runTest {
        val result = sut.sendPhotoMessage(
            conversationId = "conv_1",
            photoUri = "content://media/photos/1",
            caption = "Ảnh chụp hoàng hôn",
            mood = MoodType.THUONG,
        )

        result.status shouldBeEqualTo MessageStatus.PENDING
        result.caption shouldBeEqualTo "Ảnh chụp hoàng hôn"

        coVerify(exactly = 1) {
            messageDao.insert(match { it.status == MessageStatus.PENDING.value })
        }
        coVerify(exactly = 0) {
            messageDao.updateStatus(any(), MessageStatus.SENT.value)
        }
    }

    @Test
    fun `retryFailedMessage resets status to PENDING and inserts outbox operation`() = runTest {
        coEvery { messageDao.getMessage("msg_1") } returns mockk(relaxed = true) {
            coEvery { conversationId } returns "conv_1"
        }

        sut.retryFailedMessage("msg_1")

        coVerify(exactly = 1) {
            messageDao.updateStatus("msg_1", MessageStatus.PENDING.value)
        }
        coVerify(exactly = 1) {
            outboxDao.insert(match { it.status == MessageStatus.PENDING.value })
        }
    }
}
