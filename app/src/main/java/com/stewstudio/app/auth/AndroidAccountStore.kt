package com.stewstudio.app.auth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AndroidAccountStore(
    context: Context
) : AccountStore {

    private val preferences =
        context.getSharedPreferences(
            "stew_accounts",
            Context.MODE_PRIVATE
        )

    private val accountsKey = "accounts"

    override suspend fun getAccounts(): List<RobloxUser> {
        val json = preferences.getString(
            accountsKey,
            null
        ) ?: return emptyList()

        return try {
            val array = JSONArray(json)
            val accounts = mutableListOf<RobloxUser>()

            for (i in 0 until array.length()) {
                val account = array.getJSONObject(i)

                accounts.add(
                    RobloxUser(
                        id = account.getLong("id"),
                        name = account.getString("name"),
                        displayName = account.getString("displayName"),
                        pictureUrl =
                                if (account.has ("pictureUrl") && !account.isNull("pictureUrl")) {
                                    account.getString("pictureUrl")
                                } else {
                                    null
                                }
                    )
                )
            }

            accounts
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getAccount(
        accountId: Long
    ): RobloxUser? {
        return getAccounts().firstOrNull {
            it.id == accountId
        }
    }

    override suspend fun saveAccount(
        user: RobloxUser
    ) {
        val accounts = getAccounts().toMutableList()

        val index = accounts.indexOfFirst {
            it.id == user.id
        }

        if (index >= 0) {
            accounts[index] = user
        } else {
            accounts.add(user)
        }

        saveAccounts(accounts)
    }

    override suspend fun removeAccount(
        accountId: Long
    ) {
        val accounts = getAccounts()
            .filter {
                it.id != accountId
            }

        saveAccounts(accounts)
    }

    override suspend fun clear() {
        preferences.edit()
            .remove(accountsKey)
            .apply()
    }

    private fun saveAccounts(
        accounts: List<RobloxUser>
    ) {
        val array = JSONArray()

        accounts.forEach { user ->
            array.put(
                JSONObject().apply {
                    put("id", user.id)
                    put("name", user.name)
                    put("displayName", user.displayName)
                    put("pictureUrl", user.pictureUrl ?: JSONObject.NULL)
                }
            )
        }

        preferences.edit()
            .putString(
                accountsKey,
                array.toString()
            )
            .apply()
    }
}