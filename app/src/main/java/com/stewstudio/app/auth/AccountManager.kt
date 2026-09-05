package com.stewstudio.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountManager(
    private val accountStore: AccountStore,
    private val tokenStore: TokenStore
) {
    private val _accounts =
        MutableStateFlow<List<RobloxUser>>(emptyList())

    val accounts: StateFlow<List<RobloxUser>> =
        _accounts.asStateFlow()

    private val _activeAccount =
        MutableStateFlow<RobloxUser?>(null)

    val activeAccount: StateFlow<RobloxUser?> =
        _activeAccount.asStateFlow()

    suspend fun load() {
        val accounts = accountStore.getAccounts()

        _accounts.value = accounts

        if (accounts.isEmpty()) {
            _activeAccount.value = null
            return
        }

        val activeId = tokenStore.getActiveAccount()

        val active = accounts.firstOrNull {
            it.id.toString() == activeId
        }

        val selected = active ?: accounts.first()

        _activeAccount.value = selected

        tokenStore.setActiveAccount(
            selected.id.toString()
        )
    }

    suspend fun addAccount(
        user: RobloxUser
    ) {
        accountStore.saveAccount(user)

        val accounts = accountStore.getAccounts()

        _accounts.value = accounts

        setActiveAccount(user.id)
    }

    suspend fun saveTokens(
        accountId: Long,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long
    ) {
        tokenStore.saveTokens(
            accountId = accountId.toString(),
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
    }

    suspend fun setActiveAccount(
        accountId: Long
    ) {
        val account = _accounts.value.firstOrNull {
            it.id == accountId
        } ?: return

        tokenStore.setActiveAccount(
            accountId.toString()
        )

        _activeAccount.value = account
    }

    suspend fun removeAccount(
        accountId: Long
    ) {
        accountStore.removeAccount(accountId)

        tokenStore.removeAccount(
            accountId.toString()
        )

        val accounts = accountStore.getAccounts()

        _accounts.value = accounts

        if (_activeAccount.value?.id == accountId) {
            val newActive = accounts.firstOrNull()

            _activeAccount.value = newActive

            if (newActive != null) {
                tokenStore.setActiveAccount(
                    newActive.id.toString()
                )
            }
        }
    }

    suspend fun clearAccounts() {
        val accountIds = tokenStore.getAccounts()

        accountStore.clear()

        accountIds.forEach { accountId ->
            tokenStore.removeAccount(accountId)
        }

        _accounts.value = emptyList()
        _activeAccount.value = null
    }

    suspend fun getAccessToken(): String? {
        val account = _activeAccount.value
            ?: return null

        return tokenStore.getAccessToken(
            account.id.toString()
        )
    }

    suspend fun getRefreshToken(): String? {
        val account = _activeAccount.value
            ?: return null

        return tokenStore.getRefreshToken(
            account.id.toString()
        )
    }

    suspend fun getExpiresAt(): Long? {
        val account = _activeAccount.value
            ?: return null

        return tokenStore.getExpiresAt(
            account.id.toString()
        )
    }

    suspend fun getAccessToken(
        accountId: Long
    ): String? {
        return tokenStore.getAccessToken(
            accountId.toString()
        )
    }

    suspend fun getRefreshToken(
        accountId: Long
    ): String? {
        return tokenStore.getRefreshToken(
            accountId.toString()
        )
    }

    suspend fun getExpiresAt(
        accountId: Long
    ): Long? {
        return tokenStore.getExpiresAt(
            accountId.toString()
        )
    }
}