package com.stewstudio.app.auth

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64
import kotlinx.coroutines.cancel
import okhttp3.RequestBody.Companion.toRequestBody

class RobloxAuthManager(
    private val accountManager: AccountManager,
    private val networkMonitor: NetworkMonitor,
    private val sessionStore: OAuthSessionStore
) {

    private val scope =
        CoroutineScope(
            Dispatchers.Main.immediate
        )

    private var networkJob: Job? = null

    private var accountBeforeOffline: Long? = null

    private val httpClient =
        OkHttpClient.Builder()
            .build()

    private val _state =
        MutableStateFlow<AuthState>(
            AuthState.Loading
        )

    val state: StateFlow<AuthState> =
        _state.asStateFlow()

    suspend fun initialize() {

        try {

            if (!networkMonitor.isOnline()) {

                _state.value =
                    AuthState.LoggedIn(
                        OfflineAccount.user
                    )

                startNetworkMonitoring()

                return
            }

            accountManager.load()

            val account =
                accountManager.activeAccount.value

            if (account == null) {

                _state.value =
                    AuthState.LoggedOut

            } else {

                _state.value =
                    AuthState.LoggedIn(account)
            }

            startNetworkMonitoring()

        } catch (_: Exception) {

            _state.value =
                AuthState.LoggedIn(
                    OfflineAccount.user
                )

            startNetworkMonitoring()
        }
    }

    fun beginLogin(
        openBrowser: (String) -> Unit
    ) {

        if (!networkMonitor.isOnline()) {
            return
        }

        val codeVerifier =
            generateCodeVerifier()

        val codeChallenge =
            generateCodeChallenge(
                codeVerifier
            )

        val state =
            generateRandomValue()

        sessionStore.save(
            state = state,
            codeVerifier = codeVerifier
        )

        val uri =
            Uri.parse(
                OAuthConfig.AUTHORIZATION_URL
            ).buildUpon()
                .appendQueryParameter(
                    "client_id",
                    OAuthConfig.CLIENT_ID
                )
                .appendQueryParameter(
                    "redirect_uri",
                    OAuthConfig.REDIRECT_URI
                )
                .appendQueryParameter(
                    "scope",
                    OAuthConfig.SCOPE
                )
                .appendQueryParameter(
                    "response_type",
                    "code"
                )
                .appendQueryParameter(
                    "state",
                    state
                )
                .appendQueryParameter(
                    "code_challenge",
                    codeChallenge
                )
                .appendQueryParameter(
                    "code_challenge_method",
                    "S256"
                )
                .build()

        openBrowser(
            uri.toString()
        )
    }

    fun handleOAuthCallback(
        uri: Uri
    ) {

        scope.launch {

            try {

                val returnedState =
                    uri.getQueryParameter(
                        "state"
                    )

                val expectedState =
                    sessionStore.getState()

                if (
                    returnedState.isNullOrBlank() ||
                    expectedState.isNullOrBlank() ||
                    returnedState != expectedState
                ) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val error =
                    uri.getQueryParameter(
                        "error"
                    )

                if (!error.isNullOrBlank()) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val code =
                    uri.getQueryParameter(
                        "code"
                    )

                if (code.isNullOrBlank()) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val codeVerifier =
                    sessionStore.getCodeVerifier()

                if (
                    codeVerifier.isNullOrBlank()
                ) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val tokenResult =
                    exchangeCode(
                        code = code,
                        codeVerifier = codeVerifier
                    )

                if (tokenResult.isFailure) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val tokens =
                    tokenResult.getOrThrow()

                val userResult =
                    getUserInfo(
                        tokens.accessToken
                    )

                if (userResult.isFailure) {

                    sessionStore.clear()

                    _state.value =
                        AuthState.LoggedOut

                    return@launch
                }

                val user =
                    userResult.getOrThrow()

                accountManager.addAccount(
                    user
                )

                accountManager.saveTokens(
                    accountId = user.id,
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    expiresAt = tokens.expiresAt
                )

                sessionStore.clear()

                _state.value =
                    AuthState.LoggedIn(user)

            } catch (_: Exception) {

                sessionStore.clear()

                _state.value =
                    AuthState.LoggedOut
            }
        }
    }

    private suspend fun exchangeCode(
        code: String,
        codeVerifier: String
    ): Result<OAuthTokens> {

        return withContext(
            Dispatchers.IO
        ) {

            try {

                val jsonBody =
                    JSONObject().apply {
                        put("code", code)
                        put("codeVerifier", codeVerifier)
                    }

                val body =
                    jsonBody.toString()
                        .toRequestBody("application/json".toMediaType())

                val request =
                    Request.Builder()
                        .url(
                            OAuthConfig.TOKEN_URL
                        )
                        .post(body)
                        .header(
                            "Accept",
                            "application/json"
                        )
                        .header(
                            "Content-Type",
                            "application/json"
                        )
                        .build()

                httpClient
                    .newCall(request)
                    .execute()
                    .use { response ->

                        val responseBody =
                            response.body?.string()
                                ?: ""

                        if (!response.isSuccessful) {
                            return@withContext Result.failure(
                                IllegalStateException(
                                    "Token request failed: ${response.code} $responseBody"
                                )
                            )
                        }

                        val json =
                            JSONObject(
                                responseBody
                            )

                        val accessToken =
                            json.getString(
                                "access_token"
                            )

                        val refreshToken =
                            if (
                                json.has(
                                    "refresh_token"
                                ) &&
                                !json.isNull("refresh_token")
                            ) {
                                json.getString(
                                    "refresh_token"
                                )
                            } else {
                                null
                            }

                        val expiresIn =
                            json.optLong(
                                "expires_in",
                                900L
                            )

                        Result.success(
                            OAuthTokens(
                                accessToken =
                                    accessToken,
                                refreshToken =
                                    refreshToken,
                                expiresAt =
                                    System.currentTimeMillis() +
                                            expiresIn * 1000L
                            )
                        )
                    }

            } catch (exception: Exception) {

                Result.failure(
                    exception
                )
            }
        }
    }

    private suspend fun getUserInfo(
        accessToken: String
    ): Result<RobloxUser> {

        return withContext(
            Dispatchers.IO
        ) {

            try {

                val request =
                    Request.Builder()
                        .url(
                            OAuthConfig.USERINFO_URL
                        )
                        .get()
                        .header(
                            "Authorization",
                            "Bearer $accessToken"
                        )
                        .header(
                            "Accept",
                            "application/json"
                        )
                        .build()

                httpClient
                    .newCall(request)
                    .execute()
                    .use { response ->

                        val body =
                            response.body?.string()
                                ?: ""

                        if (!response.isSuccessful) {
                            return@withContext Result.failure(
                                IllegalStateException(
                                    "User info request failed: ${response.code} $body"
                                )
                            )
                        }

                        val json =
                            JSONObject(body)

                        val id =
                            json.getString(
                                "sub"
                            ).toLong()

                        val name =
                            json.optString(
                                "preferred_username"
                            )

                        val displayName =
                            json.optString(
                                "name",
                                name
                            )

                        val pictureUrl =
                            json.optString(
                                "picture",
                                null
                            )

                        Result.success(
                            RobloxUser(
                                id = id,
                                name = name,
                                displayName =
                                    displayName,
                                pictureUrl = pictureUrl
                            )
                        )
                    }

            } catch (exception: Exception) {

                Result.failure(
                    exception
                )
            }
        }
    }

    private fun generateRandomValue(): String {

        val bytes =
            ByteArray(32)

        SecureRandom().nextBytes(
            bytes
        )

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or
                    Base64.NO_WRAP or
                    Base64.NO_PADDING
        )
    }

    private fun generateCodeVerifier(): String {
        return generateRandomValue()
    }

    private fun generateCodeChallenge(
        verifier: String
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        val hash =
            digest.digest(
                verifier.toByteArray(
                    Charsets.US_ASCII
                )
            )

        return Base64.encodeToString(
            hash,
            Base64.URL_SAFE or
                    Base64.NO_WRAP or
                    Base64.NO_PADDING
        )
    }

    suspend fun login(
        user: RobloxUser
    ) {

        accountManager.addAccount(
            user
        )

        accountBeforeOffline = null

        _state.value =
            AuthState.LoggedIn(user)
    }

    suspend fun switchAccount(
        accountId: Long
    ) {

        if (
            accountId ==
            OfflineAccount.user.id
        ) {
            return
        }

        accountManager.setActiveAccount(
            accountId
        )

        val account =
            accountManager.activeAccount.value

        if (account != null) {

            accountBeforeOffline = null

            _state.value =
                AuthState.LoggedIn(account)
        }
    }

    fun logout() {

        accountBeforeOffline = null

        _state.value =
            AuthState.LoggedOut
    }

    private fun startNetworkMonitoring() {

        networkJob?.cancel()

        networkJob =
            scope.launch {

                networkMonitor.observe()
                    .collect { online ->

                        handleNetworkChange(
                            online
                        )
                    }
            }
    }

    private suspend fun handleNetworkChange(
        online: Boolean
    ) {

        val current =
            _state.value

        if (!online) {

            if (
                current is AuthState.LoggedIn &&
                current.user.id !=
                OfflineAccount.user.id
            ) {

                accountBeforeOffline =
                    current.user.id
            }

            if (
                current is AuthState.LoggedIn &&
                current.user.id ==
                OfflineAccount.user.id
            ) {
                return
            }

            _state.value =
                AuthState.LoggedIn(
                    OfflineAccount.user
                )

            return
        }

        if (
            current is AuthState.LoggedIn &&
            current.user.id ==
            OfflineAccount.user.id
        ) {

            accountManager.load()

            val previousId =
                accountBeforeOffline

            val account =
                if (previousId != null) {
                    accountManager.accounts.value
                        .firstOrNull {
                            it.id == previousId
                        }
                } else {
                    accountManager.activeAccount.value
                }

            if (account != null) {

                accountManager.setActiveAccount(
                    account.id
                )

                _state.value =
                    AuthState.LoggedIn(account)

            } else {

                _state.value =
                    AuthState.LoggedOut
            }

            accountBeforeOffline = null
        }
    }

    fun dispose() {
        networkJob?.cancel()
        scope.cancel()
    }
}

private data class OAuthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long
)