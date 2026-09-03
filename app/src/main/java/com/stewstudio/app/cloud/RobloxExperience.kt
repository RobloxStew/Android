package com.stewstudio.app.cloud

data class RobloxExperience(
    val id: Long,
    val name: String,
    val description: String?,
    val rootPlaceId: Long?,
    val updated: String?,
    val thumbnailUrl: String? = null
)