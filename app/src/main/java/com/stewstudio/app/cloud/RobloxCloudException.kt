package com.stewstudio.app.cloud

class RobloxCloudException(
    val statusCode: Int,
    val body: String
) : Exception(
    "Roblox Open Cloud request failed with HTTP $statusCode"
)