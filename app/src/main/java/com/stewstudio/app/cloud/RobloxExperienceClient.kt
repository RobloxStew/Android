package com.stewstudio.app.cloud

import com.stewstudio.app.auth.AccountManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class RobloxExperienceClient(
    private val accountManager: AccountManager
) {

    companion object {
        private const val BASE_URL =
            "https://apis.roblox.com"

        private const val THUMBNAIL_URL =
            "https://thumbnails.roblox.com/v1/games/icons"
    }

    private val httpClient =
        OkHttpClient.Builder()
            .build()

    suspend fun getExperiences(): Result<List<RobloxExperience>> {
        val account =
            accountManager.activeAccount.value
                ?: return Result.failure(
                    IllegalStateException(
                        "Not authenticated"
                    )
                )

        if (account.id == 2L) {
            return Result.success(emptyList())
        }

        val token =
            accountManager.getAccessToken(
                account.id
            ) ?: return Result.failure(
                IllegalStateException(
                    "Not authenticated"
                )
            )

        return withContext(Dispatchers.IO) {
            try {

                val request =
                    Request.Builder()
                        .url(
                            "$BASE_URL/legacy-develop/v1/user/universes"
                        )
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
                            json.optJSONArray("data")

                        if (data == null) {
                            return@withContext Result.success(
                                emptyList()
                            )
                        }

                        val experiences =
                            mutableListOf<RobloxExperience>()

                        for (index in 0 until data.length()) {

                            val item =
                                data.optJSONObject(index)
                                    ?: continue

                            val id =
                                item.optLong(
                                    "id",
                                    0L
                                )

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
                                    item.optString(
                                        "description"
                                    )
                                } else {
                                    null
                                }

                            val rootPlaceId =
                                if (
                                    item.has("rootPlaceId") &&
                                    !item.isNull("rootPlaceId")
                                ) {
                                    item.optLong(
                                        "rootPlaceId"
                                    )
                                } else {
                                    null
                                }

                            val updated =
                                if (
                                    item.has("updated") &&
                                    !item.isNull("updated")
                                ) {
                                    item.optString(
                                        "updated"
                                    )
                                } else {
                                    null
                                }

                            experiences.add(
                                RobloxExperience(
                                    id = id,
                                    name = name,
                                    description =
                                        description,
                                    rootPlaceId =
                                        rootPlaceId,
                                    updated = updated
                                )
                            )
                        }

                        val withThumbnails =
                            experiences.map { experience ->
                                experience.copy(
                                    thumbnailUrl =
                                        getThumbnailUrl(
                                            experience.id
                                        )
                                )
                            }

                        Result.success(
                            withThumbnails
                        )
                    }

            } catch (exception: Exception) {

                Result.failure(exception)
            }
        }
    }

    private fun getThumbnailUrl(
        universeId: Long
    ): String? {

        return try {

            val url =
                "$THUMBNAIL_URL" +
                        "?universeIds=$universeId" +
                        "&returnPolicy=PlaceHolder" +
                        "&size=512x512" +
                        "&format=Png" +
                        "&isCircular=false"

            val request =
                Request.Builder()
                    .url(url)
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

                    if (!response.isSuccessful) {
                        return null
                    }

                    val body =
                        response.body?.string()
                            ?: return null

                    val json =
                        JSONObject(body)

                    val data =
                        json.optJSONArray("data")
                            ?: return null

                    if (data.length() == 0) {
                        return null
                    }

                    data
                        .optJSONObject(0)
                        ?.optString(
                            "imageUrl",
                            null
                        )
                }

        } catch (_: Exception) {
            null
        }
    }
}