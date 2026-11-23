package com.uzuu.diuchat.feature.contacts.domain.model

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object InviteDeepLinkParser {
    const val INVITE_WEB_HOST = "diu-chat.vercel.app"
    const val INVITE_WEB_BASE_URL = "https://$INVITE_WEB_HOST/invite"
    const val INVITE_APP_SCHEME = "diu"
    const val INVITE_APP_HOST = "invite"

    private const val PATH_INVITE_SEGMENT = "invite"
    private val supportedQueryKeys = setOf("code", "invite", "invite_code")

    fun buildInviteUrl(code: String): String {
        val cleanCode = code.trim()
        return if (cleanCode.isEmpty()) {
            "https://$INVITE_WEB_HOST/"
        } else {
            "$INVITE_WEB_BASE_URL/$cleanCode"
        }
    }

    fun extractInviteCode(rawUrl: String?): String? {
        val trimmed = rawUrl?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase().orEmpty()
        val host = uri.host?.lowercase().orEmpty()

        return when {
            (scheme == "https" || scheme == "http") && host == INVITE_WEB_HOST -> {
                extractFromWebUri(uri)
            }

            scheme == INVITE_APP_SCHEME && host == INVITE_APP_HOST -> {
                extractFromAppUri(uri)
            }

            else -> {
                null
            }
        }
    }

    fun normalizeConnectQuery(rawInput: String): String = extractInviteCode(rawInput) ?: rawInput.trim()

    private fun extractFromWebUri(uri: URI): String? {
        val queryCode = extractQueryCode(uri.rawQuery)
        if (queryCode != null) return queryCode

        val segments = uri.rawPath.orEmpty()
            .split('/')
            .filter { it.isNotBlank() }
        val inviteIndex = segments.indexOfFirst { it.equals(PATH_INVITE_SEGMENT, ignoreCase = true) }
        val rawCode = segments.getOrNull(inviteIndex + 1)?.takeIf { inviteIndex >= 0 }
        return decodeSegment(rawCode)
    }

    private fun extractFromAppUri(uri: URI): String? {
        val queryCode = extractQueryCode(uri.rawQuery)
        if (queryCode != null) return queryCode

        val firstSegment = uri.rawPath.orEmpty()
            .split('/')
            .firstOrNull { it.isNotBlank() }
        return decodeSegment(firstSegment)
    }

    private fun extractQueryCode(rawQuery: String?): String? {
        if (rawQuery.isNullOrBlank()) return null
        return rawQuery.split('&')
            .asSequence()
            .mapNotNull { pair ->
                val parts = pair.split('=', limit = 2)
                val key = parts.firstOrNull()?.lowercase().orEmpty()
                val value = parts.getOrNull(1)
                if (key in supportedQueryKeys) decodeSegment(value) else null
            }
            .firstOrNull()
    }

    private fun decodeSegment(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            URLDecoder.decode(raw, StandardCharsets.UTF_8.name()).trim()
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
