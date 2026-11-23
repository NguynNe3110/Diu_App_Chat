package com.uzuu.diuchat.feature.chat.data.repository

import com.uzuu.diuchat.feature.base.data.media.ImageDownscaler
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.response.ChatMessagesResponse
import com.uzuu.diuchat.feature.chat.data.datasource.api.service.ChatSupabaseService
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.ConversationDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.MessageDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.dao.OutboxDao
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.MessageEntity
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ChatRepositoryImplTest {
    private val conversationDao: ConversationDao = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private val outboxDao: OutboxDao = mockk(relaxed = true)
    private val imageDownscaler: ImageDownscaler = mockk(relaxed = true) {
        coEvery { downscale(any(), any()) } returns null
    }
    private val supabaseService: ChatSupabaseService = mockk(relaxed = true) {
        every { currentUserId() } returns null
        every { currentUserDisplayName() } returns null
        every { observeRealtimeMessages(any()) } returns emptyFlow()
        coEvery { fetchMessages(any(), any(), any()) } returns Result.success(ChatMessagesResponse())
        coEvery { upsertMessage(any()) } returns Result.success(Unit)
        coEvery { upsertConversation(any()) } returns Result.success(Unit)
    }

    private val sut = ChatRepositoryImpl(
        conversationDao = conversationDao,
        messageDao = messageDao,
        outboxDao = outboxDao,
        database = null,
        supabaseService = supabaseService,
        imageDownscaler = imageDownscaler,
    )

    @Test
    fun `sendMessage transitions from PENDING to SENT and deletes outbox on ACK`() = runTest {
        every { supabaseService.currentUserId() } returns "auth_user_1"
        every { supabaseService.currentUserDisplayName() } returns "Minh"

        val result = sut.sendMessage(
            conversationId = "conv_1",
            content = "Chào buổi sáng",
            mood = MoodType.BINHYEN,
        )

        result.status shouldBeEqualTo MessageStatus.SENT
        result.senderId shouldBeEqualTo "auth_user_1"
        result.senderName shouldBeEqualTo "Minh"
        result.content shouldBeEqualTo "Chào buổi sáng"
        result.conversationId shouldBeEqualTo "conv_1"

        coVerify(exactly = 1) {
            messageDao.insertMessageWithOutbox(
                match { it.status == MessageStatus.PENDING.value && it.content == "Chào buổi sáng" },
                match { it.status == MessageStatus.PENDING.value },
            )
            supabaseService.upsertMessage(match { it.id == result.id && it.content == "Chào buổi sáng" })
            messageDao.updateStatus(result.id, MessageStatus.SENT.value)
            outboxDao.delete(any())
        }
    }

    @Test
    fun `sendMessage marks message and outbox as FAILED when upsertMessage fails`() = runTest {
        coEvery { supabaseService.upsertMessage(any()) } returns Result.failure(IllegalStateException("Network error"))

        val result = sut.sendMessage(
            conversationId = "conv_1",
            content = "Tin nhắn lỗi",
            mood = MoodType.BINHYEN,
        )

        result.status shouldBeEqualTo MessageStatus.FAILED

        coVerify(exactly = 1) {
            messageDao.insertMessageWithOutbox(
                match { it.status == MessageStatus.PENDING.value },
                match { it.status == MessageStatus.PENDING.value },
            )
            messageDao.updateStatus(result.id, MessageStatus.FAILED.value)
            outboxDao.updateStatus(any(), MessageStatus.FAILED.value)
        }
        coVerify(exactly = 0) {
            outboxDao.delete(any())
        }
    }

    @Test
    fun `sendPhotoMessage transitions to SENT and deletes outbox on ACK`() = runTest {
        val result = sut.sendPhotoMessage(
            conversationId = "conv_1",
            photoUri = "content://media/photos/1",
            caption = "Ảnh chụp hoàng hôn",
            mood = MoodType.THUONG,
        )

        result.status shouldBeEqualTo MessageStatus.SENT
        result.caption shouldBeEqualTo "Ảnh chụp hoàng hôn"

        coVerify(exactly = 1) {
            messageDao.insertMessageWithOutbox(
                match { it.status == MessageStatus.PENDING.value && it.caption == "Ảnh chụp hoàng hôn" },
                match { it.status == MessageStatus.PENDING.value },
            )
            messageDao.updateStatus(result.id, MessageStatus.SENT.value)
            outboxDao.delete(any())
        }
    }

    @Test
    fun `retryFailedMessage resets status to PENDING then marks SENT and deletes outbox on ACK`() = runTest {
        coEvery { messageDao.getMessage("msg_1") } returns MessageEntity(
            id = "msg_1",
            conversationId = "conv_1",
            senderId = "me",
            senderName = "Tôi",
            content = "Thử lại",
            mood = MoodType.BINHYEN.id,
            status = MessageStatus.FAILED.value,
            createdAtEpochMs = 100L,
            isFromMe = true,
        )

        sut.retryFailedMessage("msg_1")

        coVerify(exactly = 1) {
            messageDao.retryMessageWithOutbox(
                "msg_1",
                MessageStatus.PENDING.value,
                match { it.status == MessageStatus.PENDING.value },
            )
            supabaseService.upsertMessage(match { it.id == "msg_1" && it.content == "Thử lại" })
            messageDao.updateStatus("msg_1", MessageStatus.SENT.value)
            outboxDao.delete(any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeMessages syncs remote and realtime messages into messageDao with accurate isFromMe`() = runTest {
        every { supabaseService.currentUserId() } returns "user_me"
        coEvery {
            supabaseService.fetchMessages(
                conversationId = "conv_1",
                limit = ChatRepository.DEFAULT_PAGE_SIZE,
                beforeCreatedAt = null,
            )
        } returns Result.success(
            ChatMessagesResponse(
                conversationId = "conv_1",
                messages = listOf(
                    MessageDto(
                        id = "m1",
                        conversationId = "conv_1",
                        senderId = "user_me",
                        content = "Chào bạn",
                        createdAt = "2026-11-22T10:00:01Z",
                    ),
                    MessageDto(
                        id = "m2",
                        conversationId = "conv_1",
                        senderId = "user_other",
                        content = "Chào lại",
                        createdAt = "2026-11-22T10:00:02Z",
                    ),
                ),
                conversation = ConversationDto(
                    id = "conv_1",
                    title = "Bạn thân",
                ),
            ),
        )
        every { supabaseService.observeRealtimeMessages("conv_1") } returns flowOf(
            MessageDto(
                id = "m3",
                conversationId = "conv_1",
                senderId = "user_other",
                content = "Tin realtime",
                createdAt = "2026-11-22T10:00:03Z",
            ),
        )
        every { messageDao.observeLatestMessages("conv_1", ChatRepository.DEFAULT_PAGE_SIZE) } returns flowOf(emptyList())

        val collectJob = launch { sut.observeMessages("conv_1").collect {} }
        advanceUntilIdle()
        collectJob.cancel()

        coVerify(exactly = 1) {
            conversationDao.upsert(match { it.id == "conv_1" && it.title == "Bạn thân" })
            messageDao.insert(match { it.id == "m1" && it.isFromMe })
            messageDao.insert(match { it.id == "m2" && !it.isFromMe })
            messageDao.insert(
                match {
                    it.id == "m3" && !it.isFromMe && it.status == MessageStatus.SENT.value
                },
            )
        }
    }

    @Test
    fun `sendPhotoMessage uses downscaled bytes when uploading local photo`() = runTest {
        val compressedBytes = byteArrayOf(1, 2, 3, 4)
        coEvery { imageDownscaler.downscale("content://media/photos/large.jpg", any()) } returns compressedBytes
        coEvery { supabaseService.uploadMedia(any(), compressedBytes) } returns Result.success("https://cdn.example/signed.jpg")

        val result = sut.sendPhotoMessage(
            conversationId = "conv_1",
            photoUri = "content://media/photos/large.jpg",
            caption = "Hoàng hôn",
            mood = MoodType.THUONG,
        )

        result.mediaUri shouldBeEqualTo "https://cdn.example/signed.jpg"
        coVerify(exactly = 1) {
            imageDownscaler.downscale("content://media/photos/large.jpg", any())
            supabaseService.uploadMedia(match { it.startsWith("conv_1/") && it.endsWith(".jpg") }, compressedBytes)
        }
    }

    @Test
    fun `loadOlderMessages fetches older page with cursor and inserts into messageDao`() = runTest {
        every { supabaseService.currentUserId() } returns "user_me"
        coEvery {
            supabaseService.fetchMessages(
                conversationId = "conv_1",
                limit = 20,
                beforeCreatedAt = any(),
            )
        } returns Result.success(
            ChatMessagesResponse(
                conversationId = "conv_1",
                messages = listOf(
                    MessageDto(
                        id = "m_old",
                        conversationId = "conv_1",
                        senderId = "user_other",
                        content = "Tin cũ hơn",
                        createdAt = "2026-11-21T09:00:00Z",
                    ),
                ),
                hasMore = true,
            ),
        )
        coEvery { messageDao.countOlderMessages("conv_1", any()) } returns 0

        val hasMore = sut.loadOlderMessages("conv_1", beforeEpochMs = 1_763_805_600_000L, limit = 20)

        hasMore shouldBeEqualTo true
        coVerify(exactly = 1) {
            messageDao.insertAll(match { it.size == 1 && it.first().id == "m_old" })
        }
    }
}
