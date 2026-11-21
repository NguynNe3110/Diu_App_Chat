package com.uzuu.diuchat.feature.contacts.presentation.screen

import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ContactsActionTest {
    @Test
    fun `SelectCircle updates selected circle`() {
        val initial = ContactsUiState()
        val next = ContactsAction.SelectCircle(ContactCircle.FAMILY).reduce(initial)

        next.selectedCircle shouldBeEqualTo ContactCircle.FAMILY
    }

    @Test
    fun `UpdateSearchQuery updates query`() {
        val initial = ContactsUiState()
        val next = ContactsAction.UpdateSearchQuery("Minh").reduce(initial)

        next.searchQuery shouldBeEqualTo "Minh"
    }

    @Test
    fun `SetQrSheetVisible updates sheet flag`() {
        val initial = ContactsUiState()
        val next = ContactsAction.SetQrSheetVisible(true).reduce(initial)

        next.isQrSheetVisible shouldBeEqualTo true
    }

    @Test
    fun `SetCopiedLink updates copied link flag`() {
        val initial = ContactsUiState()
        val next = ContactsAction.SetCopiedLink(true).reduce(initial)

        next.copiedLink shouldBeEqualTo true
    }
}
