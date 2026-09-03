package com.stewstudio.app.auth

sealed interface AuthState {
     data object Loading : AuthState
    data object LoggedOut : AuthState

    data class LoggedIn(
        val user: RobloxUser
    ) : AuthState
}