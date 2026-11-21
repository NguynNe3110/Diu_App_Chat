package com.uzuu.diuchat.feature.journal.presentation.journal

import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMonthlyMoodCheckinsUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMoodComparisonUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SaveMoodCheckinUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockGetMonthlyMoodCheckinsUseCase: GetMonthlyMoodCheckinsUseCase = mockk()
    private val mockGetMoodComparisonUseCase: GetMoodComparisonUseCase = mockk()
    private val mockSaveMoodCheckinUseCase: SaveMoodCheckinUseCase = mockk()

    private lateinit var sut: JournalViewModel

    private val testCheckins = listOf(
        DailyMoodCheckin(
            dateIso = "2026-11-24",
            dayNumber = 24,
            mood = JournalMoodType.BINH_YEN,
            privateNote = "Một ngày thanh thản",
            sharedWithContactIds = listOf("minh"),
        ),
        DailyMoodCheckin(
            dateIso = "2026-11-23",
            dayNumber = 23,
            mood = JournalMoodType.THUONG,
            privateNote = null,
            sharedWithContactIds = listOf("minh", "me"),
        ),
    )

    private val testComparison = MoodComparison(
        partnerId = "minh",
        partnerName = "Minh",
        matchingDaysCount = 6,
        primaryMood = JournalMoodType.BINH_YEN,
        myMoods = listOf(JournalMoodType.BINH_YEN),
        partnerMoods = listOf(JournalMoodType.BINH_YEN),
        summaryMessage = "Cùng bình yên 6 ngày",
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockGetMonthlyMoodCheckinsUseCase(11, 2026) } returns flowOf(testCheckins)
        every { mockGetMoodComparisonUseCase("minh") } returns flowOf(testComparison)

        sut = JournalViewModel(
            getMonthlyMoodCheckinsUseCase = mockGetMonthlyMoodCheckinsUseCase,
            getMoodComparisonUseCase = mockGetMoodComparisonUseCase,
            saveMoodCheckinUseCase = mockSaveMoodCheckinUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load sets checkins, selectedDay, breakdown and comparison`() {
        val state = sut.uiStateFlow.value

        assertFalse(state.isLoading)
        assertEquals(2, state.checkins.size)
        assertEquals(24, state.selectedDay)
        assertNotNull(state.breakdown)
        assertEquals(testComparison, state.comparison)
    }

    @Test
    fun `onSelectDay updates selectedDay in state`() {
        sut.onSelectDay(23)

        assertEquals(23, sut.uiStateFlow.value.selectedDay)
    }

    @Test
    fun `onOpenCheckinSheet sets sheet visible and populates existing mood and note`() {
        sut.onOpenCheckinSheet()

        val state = sut.uiStateFlow.value
        assertTrue(state.isCheckinSheetVisible)
        assertEquals(JournalMoodType.BINH_YEN, state.checkinSelectedMood)
        assertEquals("Một ngày thanh thản", state.checkinNote)
    }

    @Test
    fun `onSaveCheckin calls saveMoodCheckinUseCase and closes sheet`() = runTest {
        coEvery { mockSaveMoodCheckinUseCase(any()) } returns Result.success(Unit)

        sut.onOpenCheckinSheet()
        sut.onSelectCheckinMood(JournalMoodType.VUI)
        sut.onChangeCheckinNote("Rất vui")
        sut.onSaveCheckin()

        coVerify { mockSaveMoodCheckinUseCase(any()) }
        assertFalse(sut.uiStateFlow.value.isCheckinSheetVisible)
    }
}
