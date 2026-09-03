package com.stewstudio.app.cloud

data class CloudRequest(
    val method: String,
    val path: String,
    val body: String? = null
)