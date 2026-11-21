package com.uzuu.diuchat.feature.settings.data.datasource.api.service

import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserProfileDto
import com.uzuu.diuchat.feature.settings.data.datasource.api.model.UserSettingsDto
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SettingsSupabaseServiceTest {
    private val service = SettingsSupabaseService(supabaseClient = null)

    @Test
    fun `null supabaseClient safely returns null and no-ops`() = runTest {
        service.currentUserId() shouldBeEqualTo null
        service.fetchUserProfile() shouldBeEqualTo null
        service.fetchUserSettings() shouldBeEqualTo null
        service.upsertUserProfile(UserProfileDto(id = "u-1"))
        service.upsertUserSettings(UserSettingsDto(userId = "u-1"))
        service.signOut()
    }
}
