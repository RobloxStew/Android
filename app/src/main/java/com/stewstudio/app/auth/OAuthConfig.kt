package com.stewstudio.app.auth

object OAuthConfig {

    const val CLIENT_ID = "2231627309926300547"

    const val REDIRECT_URI =
        "stew://oauth/callback"

    const val AUTHORIZATION_URL =
        "https://apis.roblox.com/oauth/v1/authorize"

    const val TOKEN_URL =
        "https://api.stewstudio.app/oauth/token"

    const val USERINFO_URL =
        "https://apis.roblox.com/oauth/v1/userinfo"

    const val SCOPE =
        "openid profile legacy-universe:manage"
}