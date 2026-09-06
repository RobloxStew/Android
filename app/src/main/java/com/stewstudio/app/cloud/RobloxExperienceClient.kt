package com.stewstudio.app.cloud

import com.stewstudio.app.auth.AccountManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.stewstudio.app.auth.RobloxAuthManager

class RobloxExperienceClient(
    private val accountManager: AccountManager,
    private val authManager: RobloxAuthManager
) {

    companion object {
        private const val BASE_URL =
            "https://api.stewstudio.app"
    }

    private val httpClient =
        OkHttpClient.Builder()
            .build()

    suspend fun getExperiences(): Result<List<RobloxExperience>> {
        val account =
            accountManager.activeAccount.value
                ?: return Result.failure(
                    IllegalStateException("Not authenticated")
                )

        if (account.id == 2L) {
            return Result.success(emptyList())
        }

        val tokenResult =
            authManager.getValidAccessToken(account.id)

        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull() ?: IllegalStateException("Not authenticated"))
        }
        val token =
            tokenResult.getOrThrow()

        return withContext(Dispatchers.IO) {
            try {
                val request =
                    Request.Builder()
                        .url("$BASE_URL/experiences/")
                        .header(
                            "Authorization",
                            "Bearer $token"
                        )
                        .header(
                            "Accept",
                            "application/json"
                        )
                        .get()
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
                                RobloxCloudException(
                                    statusCode = response.code,
                                    body = body
                                )
                            )
                        }

                        val json =
                            JSONObject(body)

                        val data =
                            json.optJSONArray("experiences")
                                ?: return@withContext Result.success(
                                    emptyList()
                                )

                        val experiences =
                            mutableListOf<RobloxExperience>()

                        for (index in 0 until data.length()) {
                            val item =
                                data.optJSONObject(index)
                                    ?: continue

                            val id =
                                item.optLong("id", 0L)

                            if (id == 0L) {
                                continue
                            }

                            val name =
                                item.optString(
                                    "name",
                                    "Untitled Experience"
                                )

                            val description =
                                if (
                                    item.has("description") &&
                                    !item.isNull("description")
                                ) {
                                    item.optString("description")
                                } else {
                                    null
                                }

                            val rootPlaceId =
                                if (
                                    item.has("rootPlaceId") &&
                                    !item.isNull("rootPlaceId")
                                ) {
                                    item.optLong("rootPlaceId")
                                } else {
                                    null
                                }

                            val updated =
                                if (
                                    item.has("updated") &&
                                    !item.isNull("updated")
                                ) {
                                    item.optString("updated")
                                } else {
                                    null
                                }

                            val thumbnailUrl =
                                if (
                                    item.has("thumbnailUrl") &&
                                    !item.isNull("thumbnailUrl")
                                ) {
                                    item.optString("thumbnailUrl")
                                } else {
                                    null
                                }

                            val ownerType =
                                item.optString("ownerType", "")

                            val ownerId =
                                item.optLong("ownerId", 0L)

                            val ownerName =
                                item.optString("ownerName", "")

                            experiences.add(
                                RobloxExperience(
                                    id = id,
                                    name = name,
                                    description = description,
                                    rootPlaceId = rootPlaceId,
                                    updated = updated,
                                    thumbnailUrl = thumbnailUrl,
                                    ownerType = ownerType,
                                    ownerId = ownerId,
                                    ownerName = ownerName
                                )
                            )
                        }

                        Result.success(experiences)
                    }
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }
}