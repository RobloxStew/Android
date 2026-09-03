package com.stewstudio.app.auth

interface AccountStore {

    suspend fun getAccounts(): List<RobloxUser>

    suspend fun getAccount(
        accountId: Long
    ): RobloxUser?

    suspend fun saveAccount(
        user: RobloxUser
    )

    suspend fun removeAccount(
        accountId: Long
    )

    suspend fun clear()
}