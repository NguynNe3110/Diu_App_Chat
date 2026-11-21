package com.uzuu.diuchat.feature.auth.data.repository

import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AuthRepositoryImplTest {
    private val mockSupabaseClient: SupabaseClient = mockk(relaxed = true)
    private val sut = AuthRepositoryImpl(supabaseClient = mockSupabaseClient)

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
}
