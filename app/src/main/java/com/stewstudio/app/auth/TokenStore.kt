package com.stewstudio.app.auth

interface TokenStore {

    suspend fun getAccounts(): List<String>

    suspend fun getAccessToken(
        accountId: String
    ): String?

    suspend fun getRefreshToken(
        accountId: String
    ): String?

    suspend fun getExpiresAt(
        accountId: String
    ): Long?

    suspend fun saveTokens(
        accountId: String,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long
    )

    suspend fun removeAccount(
        accountId: String
    )

    suspend fun getActiveAccount(): String?

    suspend fun setActiveAccount(
        accountId: String
    )
}