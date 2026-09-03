package com.stewstudio.app.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidTokenStore(
    context: Context
) : TokenStore {

    private val preferences =
        context.getSharedPreferences(
            "stew_auth",
            Context.MODE_PRIVATE
        )

    private val keyAlias = "stew_auth_key"

    private val keyStore: KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(
                        KeyProperties.BLOCK_MODE_GCM
                    )
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_NONE
                    )
                    .build()
            )

            keyGenerator.generateKey()
        }
    }

    private fun getKey(): SecretKey {
        return keyStore.getKey(
            keyAlias,
            null
        ) as SecretKey
    }

    private fun encrypt(
        value: String
    ): String {
        val cipher = Cipher.getInstance(
            "AES/GCM/NoPadding"
        )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getKey()
        )

        val encrypted = cipher.doFinal(
            value.toByteArray(
                StandardCharsets.UTF_8
            )
        )

        val combined = ByteArray(
            cipher.iv.size + encrypted.size
        )

        System.arraycopy(
            cipher.iv,
            0,
            combined,
            0,
            cipher.iv.size
        )

        System.arraycopy(
            encrypted,
            0,
            combined,
            cipher.iv.size,
            encrypted.size
        )

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    private fun decrypt(
        value: String
    ): String? {
        return try {
            val combined = Base64.decode(
                value,
                Base64.NO_WRAP
            )

            if (combined.size < 13) {
                return null
            }

            val iv = combined.copyOfRange(
                0,
                12
            )

            val encrypted = combined.copyOfRange(
                12,
                combined.size
            )

            val cipher = Cipher.getInstance(
                "AES/GCM/NoPadding"
            )

            cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                GCMParameterSpec(
                    128,
                    iv
                )
            )

            String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun tokenKey(
        accountId: String,
        token: String
    ): String {
        return "account_${accountId}_$token"
    }

    override suspend fun getAccounts(): List<String> {
        return preferences
            .getStringSet(
                "accounts",
                emptySet()
            )
            ?.toList()
            ?: emptyList()
    }

    override suspend fun getAccessToken(
        accountId: String
    ): String? {
        val encrypted = preferences.getString(
            tokenKey(
                accountId,
                "access_token"
            ),
            null
        ) ?: return null

        return decrypt(encrypted)
    }

    override suspend fun getRefreshToken(
        accountId: String
    ): String? {
        val encrypted = preferences.getString(
            tokenKey(
                accountId,
                "refresh_token"
            ),
            null
        ) ?: return null

        return decrypt(encrypted)
    }

    override suspend fun getExpiresAt(
        accountId: String
    ): Long? {
        val key = tokenKey(
            accountId,
            "expires_at"
        )

        if (!preferences.contains(key)) {
            return null
        }

        return preferences.getLong(
            key,
            0L
        )
    }

    override suspend fun saveTokens(
        accountId: String,
        accessToken: String,
        refreshToken: String?,
        expiresAt: Long
    ) {
        val accounts = getAccounts().toMutableSet()
        accounts.add(accountId)

        val editor = preferences.edit()
            .putStringSet(
                "accounts",
                accounts
            )
            .putString(
                tokenKey(
                    accountId,
                    "access_token"
                ),
                encrypt(accessToken)
            )
            .putLong(
                tokenKey(
                    accountId,
                    "expires_at"
                ),
                expiresAt
            )

        if (refreshToken != null) {
            editor.putString(
                tokenKey(
                    accountId,
                    "refresh_token"
                ),
                encrypt(refreshToken)
            )
        } else {
            editor.remove(
                tokenKey(
                    accountId,
                    "refresh_token"
                )
            )
        }

        editor.apply()
    }

    override suspend fun removeAccount(
        accountId: String
    ) {
        val accounts = getAccounts().toMutableSet()
        accounts.remove(accountId)

        val activeAccount = getActiveAccount()

        val editor = preferences.edit()
            .putStringSet(
                "accounts",
                accounts
            )
            .remove(
                tokenKey(
                    accountId,
                    "access_token"
                )
            )
            .remove(
                tokenKey(
                    accountId,
                    "refresh_token"
                )
            )
            .remove(
                tokenKey(
                    accountId,
                    "expires_at"
                )
            )

        if (activeAccount == accountId) {
            val newActiveAccount = accounts.firstOrNull()

            if (newActiveAccount != null) {
                editor.putString(
                    "active_account",
                    newActiveAccount
                )
            } else {
                editor.remove("active_account")
            }
        }

        editor.apply()
    }

    override suspend fun getActiveAccount(): String? {
        val activeAccount = preferences.getString(
            "active_account",
            null
        ) ?: return null

        if (!getAccounts().contains(activeAccount)) {
            preferences.edit()
                .remove("active_account")
                .apply()

            return null
        }

        return activeAccount
    }

    override suspend fun setActiveAccount(
        accountId: String
    ) {
        if (!getAccounts().contains(accountId)) {
            return
        }

        preferences.edit()
            .putString(
                "active_account",
                accountId
            )
            .apply()
    }
}