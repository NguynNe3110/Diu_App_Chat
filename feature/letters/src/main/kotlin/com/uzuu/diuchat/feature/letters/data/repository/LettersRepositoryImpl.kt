package com.uzuu.diuchat.feature.letters.data.repository

import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.repository.LettersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LettersRepositoryImpl : LettersRepository {

    private val lettersFlow = MutableStateFlow(
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
        ),
    )

    override fun getLetters(): Flow<List<SealedLetter>> = lettersFlow.asStateFlow()

    override suspend fun sendSealedLetter(letter: SealedLetter) {
        lettersFlow.update { current -> listOf(letter) + current }
    }

    override suspend fun openSealedLetter(letterId: String) {
        lettersFlow.update { current ->
            current.map { letter ->
                if (letter.id == letterId) {
                    letter.copy(
                        status = LetterStatus.OPENED,
                        openedAtEpochMs = System.currentTimeMillis(),
                        openedAtFormatted = "Vừa xong",
                    )
                } else {
                    letter
                }
            }
        }
    }

    override suspend fun cancelSealedLetter(letterId: String) {
        lettersFlow.update { current ->
            current.map { letter ->
                if (letter.id == letterId) {
                    letter.copy(status = LetterStatus.CANCELLED)
                } else {
                    letter
                }
            }.filterNot { it.id == letterId && it.status == LetterStatus.CANCELLED }
        }
    }

    override suspend fun sendLetterImmediately(letterId: String) {
        lettersFlow.update { current ->
            current.map { letter ->
                if (letter.id == letterId) {
                    letter.copy(
                        status = LetterStatus.READY_TO_OPEN,
                        scheduledUnlockFormatted = "Gửi ngay (Sẵn sàng mở)",
                    )
                } else {
                    letter
                }
            }
        }
    }
}
