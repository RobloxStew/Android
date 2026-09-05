package com.stewstudio.app.auth

data class RobloxUser(
    val id: Long,
    val name: String,
    val displayName: String,
    val pictureUrl: String? = null
)