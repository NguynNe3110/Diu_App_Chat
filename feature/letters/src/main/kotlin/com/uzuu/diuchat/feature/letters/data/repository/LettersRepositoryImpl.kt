package com.uzuu.diuchat.feature.letters.data.repository

import com.uzuu.diuchat.feature.letters.data.datasource.api.service.LettersSupabaseService
import com.uzuu.diuchat.feature.letters.data.mapper.LetterMapper
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class LettersRepositoryImpl(
    private val letterMapper: LetterMapper = LetterMapper(),
    private val supabaseService: LettersSupabaseService = LettersSupabaseService(null),
) : LettersRepository {

    private val lettersFlow = MutableStateFlow(
        if (supabaseService.isRemoteEnabled) {
            emptyList()
        } else {
            listOf(
                SealedLetter(
                    id = "letter_1",
                    senderId = "user_me",
                    senderName = "Tôi",
                    recipientId = "user_linh",
                    recipientName = "Linh (Người thương)",
                    title = "Gửi em buổi sáng mai thức dậy",
                    scheduledUnlockEpochMs = System.currentTimeMillis() + 14 * 3600 * 1000L,
                    scheduledUnlockFormatted = "08:00 Sáng mai (22/11)",
                    status = LetterStatus.SEALED_PENDING,
                    isSender = true,
                    moodName = "Thương",
                    contentPayload = "Dậy sớm nhớ uống một ly nước ấm em nhé. Chúc em một ngày làm việc thật dịu dàng!",
                ),
                SealedLetter(
                    id = "letter_2",
                    senderId = "user_linh",
                    senderName = "Linh",
                    recipientId = "user_me",
                    recipientName = "Tôi",
                    title = "Hẹn ước ngày kỷ niệm 500 ngày",
                    scheduledUnlockEpochMs = System.currentTimeMillis() + 5 * 24 * 3600 * 1000L,
                    scheduledUnlockFormatted = "22:00 Ngày 26/11",
                    status = LetterStatus.SEALED_PENDING,
                    isSender = false,
                    moodName = "Bình yên",
                    contentPayload = "Cảm ơn anh vì 500 ngày vừa qua đã luôn là chốn bình yên dịu dàng của em. Yêu anh rất nhiều!",
                ),
                SealedLetter(
                    id = "letter_3",
                    senderId = "user_linh",
                    senderName = "Linh",
                    recipientId = "user_me",
                    recipientName = "Tôi",
                    title = "Thư mở khi thức giấc",
                    scheduledUnlockEpochMs = System.currentTimeMillis() - 3600 * 1000L,
                    scheduledUnlockFormatted = "06:00 Sáng nay",
                    status = LetterStatus.READY_TO_OPEN,
                    isSender = false,
                    moodName = "Vui",
                    contentPayload = "Tối qua anh ngủ có ngon không? Em vừa thức dậy và chuẩn bị cà phê cho cả hai chúng mình nè!",
                ),
                SealedLetter(
                    id = "letter_4",
                    senderId = "user_linh",
                    senderName = "Linh",
                    recipientId = "user_me",
                    recipientName = "Tôi",
                    title = "Bức thư mùa thu đầu tiên",
                    scheduledUnlockEpochMs = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                    scheduledUnlockFormatted = "14/11/2026",
                    status = LetterStatus.OPENED,
                    isSender = false,
                    moodName = "Nhớ",
                    contentPayload = "Mùa thu Hà Nội năm nay dịu dàng quá, giống hệt cách anh bước vào cuộc đời em vậy...",
                    openedAtEpochMs = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                    openedAtFormatted = "14/11/2026 lúc 22:15",
                ),
            )
        },
    )

    override fun getLetters(): Flow<List<SealedLetter>> = lettersFlow
        .asStateFlow()
        .onStart {
            if (supabaseService.isRemoteEnabled) {
                supabaseService.fetchLetters().onSuccess { response ->
                    lettersFlow.value = response.letters.map(letterMapper::dtoToDomain)
                }
            }
        }

    override suspend fun sendSealedLetter(letter: SealedLetter) {
        lettersFlow.update { current -> listOf(letter) + current }
        supabaseService.upsertLetter(letterMapper.domainToDto(letter))
    }

    override suspend fun updateSealedLetter(letter: SealedLetter) {
        lettersFlow.update { current ->
            current.map { existing ->
                if (existing.id == letter.id) letter else existing
            }
        }
        supabaseService.upsertLetter(letterMapper.domainToDto(letter))
    }

    override suspend fun openSealedLetter(letterId: String) {
        val now = System.currentTimeMillis()
        val targetLetter = lettersFlow.value.find { it.id == letterId }
            ?: throw IllegalArgumentException("Không tìm thấy thư với ID: $letterId")

        check(now >= targetLetter.scheduledUnlockEpochMs || targetLetter.status == LetterStatus.READY_TO_OPEN) {
            "Thư chưa đến thời điểm mở khoá"
        }

        val updatedLetter = targetLetter.copy(
            status = LetterStatus.OPENED,
            openedAtEpochMs = now,
            openedAtFormatted = "Vừa xong",
        )
        lettersFlow.update { current ->
            current.map { letter ->
                if (letter.id == letterId) updatedLetter else letter
            }
        }
        supabaseService.upsertLetter(letterMapper.domainToDto(updatedLetter))
    }

    override suspend fun cancelSealedLetter(letterId: String) {
        lettersFlow.update { current ->
            current.filterNot { it.id == letterId }
        }
        supabaseService.deleteLetter(letterId)
    }

    override suspend fun sendLetterImmediately(letterId: String) {
        var updatedLetter: SealedLetter? = null
        lettersFlow.update { current ->
            current.map { letter ->
                if (letter.id == letterId) {
                    letter.copy(
                        status = LetterStatus.READY_TO_OPEN,
                        scheduledUnlockFormatted = "Gửi ngay (Sẵn sàng mở)",
                    ).also { updatedLetter = it }
                } else {
                    letter
                }
            }
        }
        updatedLetter?.let { supabaseService.upsertLetter(letterMapper.domainToDto(it)) }
    }
}
