package com.stewstudio.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stewstudio.app.auth.RobloxUser
import com.stewstudio.app.cloud.RobloxExperience
import com.stewstudio.app.cloud.RobloxExperienceClient

@Composable
fun HomeScreen(
    user: RobloxUser,
    experienceClient: RobloxExperienceClient,
    onLogout: () -> Unit
) {
    var activeMenu by remember {
        mutableStateOf<String?>(null)
    }

    var showProfileMenu by remember {
        mutableStateOf(false)
    }

    var showSettings by remember {
        mutableStateOf(false)
    }

    var experiences by remember {
        mutableStateOf<List<RobloxExperience>>(emptyList())
    }

    var loadingExperiences by remember {
        mutableStateOf(true)
    }

    var experienceError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(user.id) {
        loadingExperiences = true
        experienceError = null

        experienceClient
            .getExperiences()
            .onSuccess {
                experiences = it
            }
            .onFailure {
                experienceError =
                    it.message
                        ?: "Failed to load experiences."
            }

        loadingExperiences = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            StudioTopBar(
                user = user,
                activeMenu = activeMenu,
                showProfileMenu = showProfileMenu,
                onMenuClick = { menu ->
                    showProfileMenu = false

                    activeMenu =
                        if (activeMenu == menu) {
                            null
                        } else {
                            menu
                        }
                },
                onProfileClick = {
                    activeMenu = null
                    showProfileMenu =
                        !showProfileMenu
                },
                onSettings = {
                    activeMenu = null
                    showProfileMenu = false
                    showSettings = true
                },
                onLogout = onLogout
            )

            if (activeMenu != null) {
                StudioDropdownMenu(
                    menu = activeMenu!!,
                    onDismiss = {
                        activeMenu = null
                    },
                    onSettings = {
                        activeMenu = null
                        showSettings = true
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 32.dp,
                        vertical = 28.dp
                    )
            ) {

                Text(
                    text = "Home",
                    style =
                        MaterialTheme.typography.headlineLarge,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "My Experiences",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight =
                            FontWeight.SemiBold,
                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text = "See All",
                        style =
                            MaterialTheme.typography.labelLarge,
                        color =
                            MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.clickable {
                                // Experience browser later.
                            }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                when {

                    loadingExperiences -> {

                        Text(
                            text =
                                "Loading your experiences...",
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    experienceError != null -> {

                        Text(
                            text =
                                experienceError!!,
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }

                    experiences.isEmpty() -> {

                        EmptyExperiences()
                    }

                    else -> {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(
                                    rememberScrollState()
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {

                            experiences.forEach { experience ->

                                ExperienceCard(
                                    experience = experience,
                                    onClick = {
                                        // Open experience later.
                                    }
                                )
                            }

                            NewExperienceCard(
                                onClick = {
                                    // New experience later.
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(36.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = {
                            // New experience flow later.
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Add,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("New Experience")
                    }

                    Button(
                        onClick = {
                            // Open experience flow later.
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.FolderOpen,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Open Existing")
                    }
                }
            }
        }

        if (showSettings) {
            SettingsPanel(
                onClose = {
                    showSettings = false
                }
            )
        }
    }
}

@Composable
private fun EmptyExperiences() {
    Card(
        modifier = Modifier
            .width(320.dp)
            .height(170.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "No Experiences",
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Create your first experience to get started.",
                style =
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudioTopBar(
    user: RobloxUser,
    activeMenu: String?,
    showProfileMenu: Boolean,
    onMenuClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            color =
                MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Stew Studio",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                TopMenuButton(
                    text = "File",
                    selected =
                        activeMenu == "File",
                    onClick = {
                        onMenuClick("File")
                    }
                )

                TopMenuButton(
                    text = "Plugins",
                    selected =
                        activeMenu == "Plugins",
                    onClick = {
                        onMenuClick("Plugins")
                    }
                )

                TopMenuButton(
                    text = "Help",
                    selected =
                        activeMenu == "Help",
                    onClick = {
                        onMenuClick("Help")
                    }
                )

                TopMenuButton(
                    text = "Settings",
                    selected = false,
                    onClick = onSettings
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box {

                    Surface(
                        modifier = Modifier.clickable {
                            onProfileClick()
                        },
                        shape =
                            RoundedCornerShape(8.dp),
                        color =
                            MaterialTheme.colorScheme
                                .surfaceVariant
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default
                                        .AccountCircle,
                                contentDescription = null,
                                modifier =
                                    Modifier.size(28.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(8.dp)
                            )

                            Column {

                                Text(
                                    text =
                                        user.displayName,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelLarge,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    text =
                                        "@${user.name}",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector =
                                    Icons.Default
                                        .ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded =
                            showProfileMenu,
                        onDismissRequest = {
                            onProfileClick()
                        }
                    ) {

                        Text(
                            text =
                                user.displayName,
                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(
                            text =
                                "@${user.name}",
                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp
                                )
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Switch Account")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default
                                            .AccountCircle,
                                    contentDescription =
                                        null
                                )
                            },
                            onClick = {
                                // Account switcher later.
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Settings")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default
                                            .Settings,
                                    contentDescription =
                                        null
                                )
                            },
                            onClick = {
                                onSettings()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Log Out")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default
                                            .Logout,
                                    contentDescription =
                                        null
                                )
                            },
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopMenuButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        color =
            if (selected) {
                MaterialTheme.colorScheme
                    .surfaceVariant
            } else {
                Color.Transparent
            },
        shape =
            RoundedCornerShape(6.dp)
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            style =
                MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StudioDropdownMenu(
    menu: String,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = when (menu) {
                    "File" -> 120.dp
                    "Plugins" -> 175.dp
                    "Help" -> 255.dp
                    else -> 120.dp
                }
            )
    ) {

        Card(
            modifier = Modifier.width(230.dp),
            shape =
                RoundedCornerShape(8.dp),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
        ) {

            Column(
                modifier = Modifier.padding(
                    vertical = 6.dp
                )
            ) {

                when (menu) {

                    "File" -> {

                        MenuItem(
                            icon = Icons.Default.Add,
                            text = "New Experience",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon =
                                Icons.Default.FolderOpen,
                            text = "Open Experience",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon =
                                Icons.Default.Description,
                            text = "Save",
                            onClick = onDismiss
                        )

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 6.dp
                                )
                        )

                        MenuItem(
                            icon = Icons.Default.Close,
                            text = "Close",
                            onClick = onDismiss
                        )
                    }

                    "Plugins" -> {

                        MenuItem(
                            icon =
                                Icons.Default.Extension,
                            text = "Plugin Manager",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon = Icons.Default.Add,
                            text = "Install Plugin",
                            onClick = onDismiss
                        )
                    }

                    "Help" -> {

                        MenuItem(
                            icon =
                                Icons.Default.HelpOutline,
                            text = "Documentation",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon =
                                Icons.Default.HelpOutline,
                            text = "About Stew Studio",
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon:
    androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExperienceCard(
    experience: RobloxExperience,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(10.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp
                    )
                )
                .background(
                    MaterialTheme.colorScheme
                        .surfaceVariant
                )
        ) {

            if (
                experience.thumbnailUrl
                    ?.isNotBlank() == true
            ) {

                AsyncImage(
                    model =
                        experience.thumbnailUrl,
                    contentDescription =
                        experience.name,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

            } else {

                Text(
                    text = "No Thumbnail",
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    style =
                        MaterialTheme.typography
                            .labelMedium
                )
            }
        }

        Column(
            modifier = Modifier.padding(
                14.dp
            )
        ) {

            Text(
                text = experience.name,
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    experience.description
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "No description",
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun NewExperienceCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(196.dp)
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(10.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surface
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,
                contentDescription = null,
                modifier =
                    Modifier.size(40.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text = "New Experience",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme
                    .background
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                color =
                    MaterialTheme.colorScheme
                        .surface,
                shadowElevation = 2.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 16.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription =
                                "Close"
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Settings",
                        style =
                            MaterialTheme.typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme
                                .surface
                        )
                        .padding(12.dp)
                ) {

                    SettingsCategory(
                        title = "Studio",
                        selected = true
                    )

                    SettingsCategory(
                        title = "Appearance"
                    )

                    SettingsCategory(
                        title = "Account"
                    )

                    SettingsCategory(
                        title = "Privacy"
                    )

                    SettingsCategory(
                        title = "Network"
                    )

                    SettingsCategory(
                        title = "About"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {

                    Text(
                        text = "Studio",
                        style =
                            MaterialTheme.typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )

                    Text(
                        text = "Studio Settings",
                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Configure how Stew Studio behaves.",
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    selected: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 2.dp
            ),
        shape =
            RoundedCornerShape(7.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme
                    .secondaryContainer
            } else {
                Color.Transparent
            }
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            style =
                MaterialTheme.typography
                    .bodyMedium,
            fontWeight =
                if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
        )
    }
}