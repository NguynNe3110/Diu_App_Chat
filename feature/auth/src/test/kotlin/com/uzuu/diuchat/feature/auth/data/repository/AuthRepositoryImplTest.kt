package com.uzuu.diuchat.feature.auth.data.repository

import com.uzuu.diuchat.feature.auth.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.auth.data.datasource.api.response.UserProfileResponse
import com.uzuu.diuchat.feature.auth.data.datasource.api.service.AuthSupabaseService
import com.uzuu.diuchat.feature.auth.data.mapper.AuthMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AuthRepositoryImplTest {
    private val mockSupabaseService: AuthSupabaseService = mockk(relaxed = true)
    private val authMapper = AuthMapper()
    private val sut = AuthRepositoryImpl(
        supabaseService = mockSupabaseService,
        authMapper = authMapper,
    )

    @Test
    fun `sendOtp rejects invalid phone number`() = runTest {
        sut.sendOtp("123").isFailure shouldBeEqualTo true
    }

    @Test
    fun `invalid OTP returns a validation failure`() = runTest {
        sut.verifyOtp("912345678", "12x").isFailure shouldBeEqualTo true
    }

    @Test
    fun `short OTP returns a validation failure`() = runTest {
        sut.verifyOtp("912345678", "123").isFailure shouldBeEqualTo true
    }

    @Test
    fun `signInWithGoogle rejects blank id token`() = runTest {
        sut.signInWithGoogle("   ").isFailure shouldBeEqualTo true
    }

    @Test
    fun `claimUsername rejects invalid username format`() = runTest {
        sut.claimUsername("ab").isFailure shouldBeEqualTo true
        sut.claimUsername("user@name!").isFailure shouldBeEqualTo true
        sut.claimUsername("a".repeat(31)).isFailure shouldBeEqualTo true
    }

    @Test
    fun `checkHasUsername maps profile dto via AuthMapper`() = runTest {
        coEvery { mockSupabaseService.fetchCurrentUserProfile() } returns
            Result.success(UserProfileResponse(UserProfileDto(id = "u-1", username = "minh")))

        val result = sut.checkHasUsername()

        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo true
    }
}
