package com.uzuu.diuchat.feature.auth.presentation.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException

object GoogleSignInHelper {
    suspend fun getGoogleIdToken(
        context: Context,
        serverClientId: String,
    ): Result<String> = runCatching {
        require(serverClientId.isNotBlank()) {
            "Google Web Client ID chưa được cấu hình."
        }
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            googleIdTokenCredential.idToken
        } else {
            error("Không tìm thấy thông tin đăng nhập Google hợp lệ.")
        }
    }.recoverCatching { throwable ->
        if (throwable is GetCredentialCancellationException) {
            throw CancellationException("Đã hủy đăng nhập Google", throwable)
        }
        throw throwable
    }
}
