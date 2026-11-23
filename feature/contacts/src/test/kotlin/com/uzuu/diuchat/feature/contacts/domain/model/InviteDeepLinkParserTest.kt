package com.uzuu.diuchat.feature.contacts.domain.model

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class InviteDeepLinkParserTest {

    @Test
    fun `buildInviteUrl formats production vercel invite link`() {
        InviteDeepLinkParser.buildInviteUrl(" 8X3K2P ") shouldBeEqualTo "https://diu-chat.vercel.app/invite/8X3K2P"
        InviteDeepLinkParser.buildInviteUrl("   ") shouldBeEqualTo "https://diu-chat.vercel.app/"
    }

    @Test
    fun `extractInviteCode parses path and query variants for web and custom scheme`() {
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/invite/8X3K2P") shouldBeEqualTo "8X3K2P"
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/invite/8X3K2P/") shouldBeEqualTo "8X3K2P"
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/?code=8X3K2P") shouldBeEqualTo "8X3K2P"
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/invite?invite=7K2M9P") shouldBeEqualTo "7K2M9P"
        InviteDeepLinkParser.extractInviteCode("diu://invite/8X3K2P") shouldBeEqualTo "8X3K2P"
        InviteDeepLinkParser.extractInviteCode("diu://invite?code=AB12CD") shouldBeEqualTo "AB12CD"
    }

    @Test
    fun `extractInviteCode rejects unrelated hosts and empty paths`() {
        InviteDeepLinkParser.extractInviteCode(null).shouldBeNull()
        InviteDeepLinkParser.extractInviteCode("").shouldBeNull()
        InviteDeepLinkParser.extractInviteCode("https://example.com/invite/8X3K2P").shouldBeNull()
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/").shouldBeNull()
        InviteDeepLinkParser.extractInviteCode("https://diu-chat.vercel.app/invite/").shouldBeNull()
        InviteDeepLinkParser.extractInviteCode("diu://chat/123").shouldBeNull()
    }

    @Test
    fun `normalizeConnectQuery extracts code from invite url and keeps plain inputs`() {
        InviteDeepLinkParser.normalizeConnectQuery("https://diu-chat.vercel.app/invite/8X3K2P") shouldBeEqualTo "8X3K2P"
        InviteDeepLinkParser.normalizeConnectQuery("  @minh_an  ") shouldBeEqualTo "@minh_an"
        InviteDeepLinkParser.normalizeConnectQuery(" 8X3K2P ") shouldBeEqualTo "8X3K2P"
    }
}
