package com.stewstudio.app.auth

import android.content.Context

class OAuthSessionStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "stew_oauth",
            Context.MODE_PRIVATE
        )

    fun save(
        state: String,
        codeVerifier: String
    ) {
        preferences.edit()
            .putString(
                "state",
                state
            )
            .putString(
                "code_verifier",
                codeVerifier
            )
            .apply()
    }

    fun getState(): String? {
        return preferences.getString(
            "state",
            null
        )
    }

    fun getCodeVerifier(): String? {
        return preferences.getString(
            "code_verifier",
            null
        )
    }

    fun clear() {
        preferences.edit()
            .remove("state")
            .remove("code_verifier")
            .apply()
    }
}