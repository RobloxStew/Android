package com.stewstudio.app.cloud

import com.stewstudio.app.auth.AccountManager
import com.stewstudio.app.auth.RobloxAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RobloxCloudClient(
    private val accountManager: AccountManager,
    private val authManager: RobloxAuthManager
) {

    companion object {
        private const val BASE_URL =
            "https://apis.roblox.com"
    }

    private val httpClient =
        OkHttpClient.Builder()
            .build()

    suspend fun request(
        request: CloudRequest
    ): Result<String> {

        val account =
            accountManager.activeAccount.value
                ?: return Result.failure(
                    IllegalStateException(
                        "Not authenticated"
                    )
                )

        if (account.id == 2L) {
            return Result.failure(
                IllegalStateException(
                    "Open Cloud is unavailable offline"
                )
            )
        }

        val tokenResult =
            authManager.getValidAccessToken(account.id)

        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: IllegalStateException("Authentication failed"))
        }
        val token =
            tokenResult.getOrThrow()

        return withContext(Dispatchers.IO) {

            try {

                val url =
                    BASE_URL +
                            if (
                                request.path.startsWith("/")
                            ) {
                                request.path
                            } else {
                                "/${request.path}"
                            }

                val builder =
                    Request.Builder()
                        .url(url)
                        .header(
                            "Authorization",
                            "Bearer $token"
                        )
                        .header(
                            "Accept",
                            "application/json"
                        )

                when (
                    request.method.uppercase()
                ) {

                    "GET" -> {
                        builder.get()
                    }

                    "POST" -> {
                        builder.post(
                            createBody(
                                request.body
                            )
                        )
                    }

                    "PUT" -> {
                        builder.put(
                            createBody(
                                request.body
                            )
                        )
                    }

                    "PATCH" -> {
                        builder.patch(
                            createBody(
                                request.body
                            )
                        )
                    }

                    "DELETE" -> {
                        builder.delete()
                    }

                    else -> {
                        return@withContext Result.failure(
                            IllegalArgumentException(
                                "Unsupported HTTP method: ${request.method}"
                            )
                        )
                    }
                }

                httpClient
                    .newCall(builder.build())
                    .execute()
                    .use { response ->

                        val body =
                            response.body?.string()
                                ?: ""

                        if (response.isSuccessful) {
                            Result.success(body)
                        } else {
                            Result.failure(
                                RobloxCloudException(
                                    statusCode =
                                        response.code,
                                    body = body
                                )
                            )
                        }
                    }

            } catch (exception: Exception) {

                Result.failure(
                    exception
                )
            }
        }
    }

    private fun createBody(
        body: String?
    ) =
        (
                body ?: ""
                ).toRequestBody(
                "application/json".toMediaType()
            )
}