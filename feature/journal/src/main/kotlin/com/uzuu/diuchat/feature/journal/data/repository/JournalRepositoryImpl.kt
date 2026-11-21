package com.uzuu.diuchat.feature.journal.data.repository

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import com.uzuu.diuchat.feature.journal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl : JournalRepository {
    private val checkinsFlow = MutableStateFlow(seedMonthlyCheckins())
    private val allMessages = seedMessages()

    override fun getMonthlyMoodCheckins(
        month: Int,
        year: Int,
    ): Flow<List<DailyMoodCheckin>> = checkinsFlow.map { list ->
        val expectedPrefix = "%04d-%02d".format(year, month)
        list.filter { it.dateIso.startsWith(expectedPrefix) }
    }

    override suspend fun saveMoodCheckin(checkin: DailyMoodCheckin): Result<Unit> {
        val current = checkinsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.dayNumber == checkin.dayNumber }
        if (index >= 0) {
            current[index] = checkin
        } else {
            current.add(checkin)
            current.sortBy { it.dayNumber }
        }
        checkinsFlow.value = current
        return Result.success(Unit)
    }

    override fun getMoodComparison(partnerId: String): Flow<MoodComparison> {
        val myMoods = listOf(
            JournalMoodType.BINH_YEN,
            JournalMoodType.THUONG,
            JournalMoodType.BINH_YEN,
            JournalMoodType.VUI,
            JournalMoodType.BINH_YEN,
            JournalMoodType.NHO,
            JournalMoodType.BINH_YEN,
            JournalMoodType.THUONG,
            JournalMoodType.BINH_YEN,
            JournalMoodType.BINH_YEN,
            JournalMoodType.VUI,
            JournalMoodType.BINH_YEN,
            JournalMoodType.THUONG,
            JournalMoodType.BINH_YEN,
        )
        val partnerMoods = listOf(
            JournalMoodType.BINH_YEN,
            JournalMoodType.VUI,
            JournalMoodType.BINH_YEN,
            JournalMoodType.VUI,
            JournalMoodType.BINH_YEN,
            JournalMoodType.THUONG,
            JournalMoodType.BINH_YEN,
            JournalMoodType.TRAM,
            JournalMoodType.BINH_YEN,
            JournalMoodType.BINH_YEN,
            JournalMoodType.THUONG,
            JournalMoodType.BINH_YEN,
            JournalMoodType.VUI,
            JournalMoodType.BINH_YEN,
        )
        val matchingDays = myMoods.indices.count { myMoods[it] == partnerMoods[it] }

        return flowOf(
            MoodComparison(
                partnerId = partnerId,
                partnerName = "Minh",
                matchingDaysCount = matchingDays,
                primaryMood = JournalMoodType.BINH_YEN,
                myMoods = myMoods,
                partnerMoods = partnerMoods,
                summaryMessage = "Hai bạn cùng thấy Bình yên $matchingDays ngày trong tháng này.",
            ),
        )
    }

    override fun searchMessages(
        query: String,
        mood: JournalMoodType?,
        conversationId: String?,
    ): Flow<List<SearchResultItem>> = flowOf(allMessages).map { list ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.snippet.contains(query, ignoreCase = true) ||
                item.senderName.contains(query, ignoreCase = true) ||
                item.conversationName.contains(query, ignoreCase = true)

            val matchesMood = mood == null || item.mood == mood
            val matchesConv = conversationId == null || item.conversationId == conversationId

            matchesQuery && matchesMood && matchesConv
        }
    }

    private companion object {
        private const val CONV_MINH = "conv_minh"
        fun seedMonthlyCheckins(): List<DailyMoodCheckin> {
            val list = mutableListOf<DailyMoodCheckin>()
            val moods = listOf(
                JournalMoodType.BINH_YEN,
                JournalMoodType.THUONG,
                JournalMoodType.VUI,
                JournalMoodType.BINH_YEN,
                JournalMoodType.NHO,
                JournalMoodType.BINH_YEN,
                JournalMoodType.THUONG,
                JournalMoodType.VUI,
                JournalMoodType.BINH_YEN,
                JournalMoodType.TRAM,
                JournalMoodType.BINH_YEN,
                JournalMoodType.THUONG,
                JournalMoodType.BINH_YEN,
                JournalMoodType.LO,
                JournalMoodType.VUI,
                JournalMoodType.BINH_YEN,
                JournalMoodType.THUONG,
                JournalMoodType.BINH_YEN,
                JournalMoodType.NHO,
                JournalMoodType.BINH_YEN,
                JournalMoodType.VUI,
                JournalMoodType.BINH_YEN,
                JournalMoodType.THUONG,
                JournalMoodType.BINH_YEN,
            )

            val notes = mapOf(
                3 to "Buổi sáng trời se lạnh, uống trà ấm với Minh.",
                7 to "Được tặng bó hoa nhỏ, thấy vui cả ngày.",
                11 to "Một ngày bình lặng, hoàn thành mọi việc sớm.",
                17 to "Xem lại ảnh cũ, thấy nhớ ngày mới gặp.",
                21 to "Đi dạo bờ sông lúc hoàng hôn, gió mát lành.",
                24 to "“Đi dạo với Minh ở bờ sông, trời mát.”",
            )

            for (day in 1..24) {
                val mood = moods[(day - 1) % moods.size]
                list.add(
                    DailyMoodCheckin(
                        dateIso = "2026-11-%02d".format(day),
                        dayNumber = day,
                        mood = mood,
                        privateNote = notes[day],
                        sharedWithContactIds = listOf("minh", "me", "thao"),
                        displayDate = "%d tháng 11".format(day),
                    ),
                )
            }
            return list
        }

        fun seedMessages(): List<SearchResultItem> = listOf(
            SearchResultItem(
                id = "msg_1",
                conversationId = CONV_MINH,
                conversationName = "Minh",
                senderName = "Minh",
                snippet = "Vậy anh ghé tiệm bánh flan đầu hẻm nha",
                timestamp = "Hôm nay 21:10",
                mood = JournalMoodType.THUONG,
            ),
            SearchResultItem(
                id = "msg_2",
                conversationId = CONV_MINH,
                conversationName = "Minh",
                senderName = "Bạn",
                snippet = "Flan ở Đà Lạt ăn trong tiết trời se lạnh ngon hơn á",
                timestamp = "14/11",
                mood = JournalMoodType.VUI,
            ),
            SearchResultItem(
                id = "msg_3",
                conversationId = CONV_MINH,
                conversationName = "Minh",
                senderName = "Bạn",
                snippet = "Mai mình làm flan ở nhà thử không, em chuẩn bị trứng sữa rồi",
                timestamp = "02/11",
                mood = JournalMoodType.BINH_YEN,
            ),
            SearchResultItem(
                id = "msg_4",
                conversationId = CONV_MINH,
                conversationName = "Minh",
                senderName = "Minh",
                snippet = "Nhớ cái flan hôm bữa ghê, ngọt thanh dịu dàng",
                timestamp = "20/10",
                mood = JournalMoodType.NHO,
            ),
            SearchResultItem(
                id = "msg_5",
                conversationId = "conv_nha",
                conversationName = "Nhà mình",
                senderName = "Mẹ",
                snippet = "Tối mai cả nhà ăn cơm lúc 7 giờ nhé con",
                timestamp = "Hôm qua 18:30",
                mood = JournalMoodType.THUONG,
            ),
            SearchResultItem(
                id = "msg_6",
                conversationId = "conv_thao",
                conversationName = "Thảo",
                senderName = "Thảo",
                snippet = "Cuối tuần này đi cà phê ngắm mưa không cậu?",
                timestamp = "Thứ Ba 14:15",
                mood = JournalMoodType.VUI,
            ),
            SearchResultItem(
                id = "msg_7",
                conversationId = CONV_MINH,
                conversationName = "Minh",
                senderName = "Minh",
                snippet = "Anh tới đầu hẻm rồi nè, có mua bánh flan cho em đó",
                timestamp = "Hôm qua 21:30",
                mood = JournalMoodType.THUONG,
            ),
        )
    }
}
