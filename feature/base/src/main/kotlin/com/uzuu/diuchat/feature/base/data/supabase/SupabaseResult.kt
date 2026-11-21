package com.uzuu.diuchat.feature.base.data.supabase

/**
 * A generic result wrapper for Supabase operations.
 *
 * Mirrors [com.uzuu.diuchat.feature.base.data.retrofit.ApiResult] but tailored for Supabase.
 * Use this in feature module services to wrap Supabase query results before mapping
 * to domain [com.uzuu.diuchat.feature.base.domain.result.Result].
 */
sealed interface SupabaseResult<T> {
    /**
     * Represents a Supabase operation that completed successfully with data.
     */
    data class Success<T>(
        val data: T,
    ) : SupabaseResult<T>

    /**
     * Represents a Supabase operation that returned an error.
     */
    data class Error<T>(
        val message: String?,
    ) : SupabaseResult<T>

    /**
     * Represents a Supabase operation that faced an unexpected exception
     * such as network failure or serialization error.
     */
    data class Exception<T>(
        val throwable: Throwable,
    ) : SupabaseResult<T>
}
