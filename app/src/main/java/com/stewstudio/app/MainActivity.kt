package com.stewstudio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stewstudio.app.auth.AccountManager
import com.stewstudio.app.auth.AndroidAccountStore
import com.stewstudio.app.auth.AndroidTokenStore
import com.stewstudio.app.auth.AuthState
import com.stewstudio.app.auth.NetworkMonitor
import com.stewstudio.app.auth.OAuthSessionStore
import com.stewstudio.app.auth.RobloxAuthManager
import com.stewstudio.app.cloud.RobloxExperienceClient
import com.stewstudio.app.ui.home.HomeScreen
import com.stewstudio.app.ui.login.LoginScreen
import com.stewstudio.app.ui.theme.StewTheme

class MainActivity : ComponentActivity() {

    private lateinit var authManager: RobloxAuthManager

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val tokenStore =
            AndroidTokenStore(
                applicationContext
            )

        val accountStore =
            AndroidAccountStore(
                applicationContext
            )

        val accountManager =
            AccountManager(
                accountStore = accountStore,
                tokenStore = tokenStore
            )

        val experienceClient =
            RobloxExperienceClient(
                accountManager
            )

        val networkMonitor =
            NetworkMonitor(
                applicationContext
            )

        val sessionStore =
            OAuthSessionStore(
                applicationContext
            )

        authManager =
            RobloxAuthManager(
                accountManager = accountManager,
                networkMonitor = networkMonitor,
                sessionStore = sessionStore
            )

        handleOAuthIntent(intent)

        setContent {
            StewTheme {

                val state by
                authManager.state.collectAsState()

                LaunchedEffect(authManager) {
                    authManager.initialize()
                }

                when (val current = state) {

                    AuthState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0xFF191A1C)
                                ),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.Center
                        ) {
                            CircularProgressIndicator()

                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )

                            Text(
                                text = "Starting Stew...",
                                color = Color.White
                            )
                        }
                    }

                    AuthState.LoggedOut -> {
                        LoginScreen(
                            onLogin = {
                                authManager.beginLogin { url ->

                                    val intent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(url)
                                        )

                                    startActivity(intent)
                                }
                            }
                        )
                    }

                    is AuthState.LoggedIn -> {
                        HomeScreen(
                            user = current.user,
                            experienceClient = experienceClient,
                            onLogout = {
                                authManager.logout()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(
        intent: Intent
    ) {
        val uri = intent.data ?: return

        if (
            uri.scheme == "stew" &&
            uri.host == "oauth" &&
            uri.path == "/callback"
        ) {
            authManager.handleOAuthCallback(uri)
        }
    }

    override fun onDestroy() {
        authManager.dispose()

        super.onDestroy()
    }
}