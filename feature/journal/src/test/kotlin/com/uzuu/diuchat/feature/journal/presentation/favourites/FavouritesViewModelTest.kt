package com.uzuu.diuchat.feature.journal.presentation.favourites

import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.usecase.GetFavouritesUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.ToggleFavouriteUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val getFavouritesUseCase: GetFavouritesUseCase = mockk()
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase = mockk()

    private val testMemories = listOf(
        FavouriteMemory(
            id = "fav_1",
            senderName = "Minh",
            mood = JournalMoodType.THUONG,
            content = "Cảm ơn vì đã luôn ở bên mình.",
            savedDate = "14/02/2024",
        ),
    )

    lateinit var sut: FavouritesViewModel

    @BeforeEach
    fun setUp() {
        every { getFavouritesUseCase() } returns flowOf(testMemories)
        sut = FavouritesViewModel(
            getFavouritesUseCase = getFavouritesUseCase,
            toggleFavouriteUseCase = toggleFavouriteUseCase,
        )
    }

    @Test
    fun `initial load collects favourite memories`() = runTest {
        advanceUntilIdle()
        sut.uiStateFlow.value.memories shouldBeEqualTo testMemories
        sut.uiStateFlow.value.isLoading shouldBeEqualTo false
    }

    @Test
    fun `onToggleFavourite invokes toggleFavouriteUseCase`() = runTest {
        coEvery { toggleFavouriteUseCase("fav_1") } returns Result.success(Unit)

        sut.onToggleFavourite("fav_1")
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavouriteUseCase("fav_1") }
    }
}
