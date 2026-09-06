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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stewstudio.app.auth.RobloxUser
import com.stewstudio.app.cloud.RobloxExperience
import com.stewstudio.app.cloud.RobloxExperienceClient

private enum class StudioPage {
    Home,
    Experiences,
    Settings
}

private enum class ExperienceTab {
    Experiences,
    GroupExperiences,
    SharedWithMe,
    Local
}

private enum class SettingsCategory {
    Studio,
    Appearance,
    Editor,
    Account,
    Privacy,
    Network,
    About
}

@Composable
fun HomeScreen(
    user: RobloxUser,
    accounts: List<RobloxUser>,
    experienceClient: RobloxExperienceClient,
    onLogout: () -> Unit,
    onAddAccount: () -> Unit,
    onSwitchAccount: (Long) -> Unit
) {
    var currentPage by remember {
        mutableStateOf(StudioPage.Home)
    }

    var activeMenu by remember {
        mutableStateOf<String?>(null)
    }

    var showProfileMenu by remember {
        mutableStateOf(false)
    }

    var showAccountSwitcher by remember {
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
                    it.message ?: "Failed to load experiences."
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
                currentPage = currentPage,
                activeMenu = activeMenu,
                showProfileMenu = showProfileMenu,
                onNavigate = {
                    currentPage = it
                    activeMenu = null
                    showProfileMenu = false
                },
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
                    showProfileMenu = !showProfileMenu
                },
                onSwitchAccount = {
                    showProfileMenu = false
                    showAccountSwitcher = true
                },
                onLogout = onLogout
            )

            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                StudioNavigationRail(
                    currentPage = currentPage,
                    onNavigate = {
                        currentPage = it
                        activeMenu = null
                        showProfileMenu = false
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentPage) {
                        StudioPage.Home -> {
                            StudioHomePage(
                                user = user,
                                experiences = experiences,
                                loadingExperiences = loadingExperiences,
                                experienceError = experienceError,
                                onNewExperience = {},
                                onOpenExisting = {},
                                onExperienceClick = {},
                                onSeeAll = {
                                    currentPage = StudioPage.Experiences
                                }
                            )
                        }

                        StudioPage.Experiences -> {
                            ExperiencesPage(
                                experiences = experiences,
                                loadingExperiences = loadingExperiences,
                                experienceError = experienceError,
                                onNewExperience = {},
                                onExperienceClick = {}
                            )
                        }

                        StudioPage.Settings -> {
                            SettingsPage()
                        }
                    }
                }
            }
        }

        if (activeMenu != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp)
            ) {
                StudioDropdownMenu(
                    menu = activeMenu!!,
                    onDismiss = {
                        activeMenu = null
                    },
                    onNavigate = {
                        currentPage = it
                        activeMenu = null
                    }
                )
            }
        }
    }

    if (showAccountSwitcher) {
        AccountSwitcherDialog(
            accounts = accounts,
            activeAccountId = user.id,
            onDismiss = {
                showAccountSwitcher = false
            },
            onAccountSelected = { accountId ->
                showAccountSwitcher = false
                onSwitchAccount(accountId)
            },
            onAddAccount = {
                showAccountSwitcher = false
                onAddAccount()
            }
        )
    }
}

@Composable
private fun StudioTopBar(
    user: RobloxUser,
    currentPage: StudioPage,
    activeMenu: String?,
    showProfileMenu: Boolean,
    onNavigate: (StudioPage) -> Unit,
    onMenuClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stew Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                TopMenuButton(
                    text = "File",
                    selected = activeMenu == "File",
                    onClick = {
                        onMenuClick("File")
                    }
                )

                TopMenuButton(
                    text = "Plugins",
                    selected = activeMenu == "Plugins",
                    onClick = {
                        onMenuClick("Plugins")
                    }
                )

                TopMenuButton(
                    text = "Help",
                    selected = activeMenu == "Help",
                    onClick = {
                        onMenuClick("Help")
                    }
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Box {
                    Surface(
                        modifier = Modifier.clickable {
                            onProfileClick()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            ProfilePicture(
                                user = user,
                                size = 28.dp
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Column {
                                Text(
                                    text = user.displayName,
                                    style =
                                        MaterialTheme.typography
                                            .labelLarge,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    text = "@${user.name}",
                                    style =
                                        MaterialTheme.typography
                                            .labelSmall,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector =
                                    Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = onProfileClick
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            ProfilePicture(
                                user = user,
                                size = 42.dp
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Column {
                                Text(
                                    text = user.displayName,
                                    style =
                                        MaterialTheme.typography
                                            .titleSmall,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Text(
                                    text = "@${user.name}",
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(
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
                                        Icons.Default.AccountCircle,
                                    contentDescription = null
                                )
                            },
                            onClick = onSwitchAccount
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Settings")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default.Settings,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onProfileClick()
                                onNavigate(StudioPage.Settings)
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Log Out")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
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
private fun ProfilePicture(
    user: RobloxUser,
    size: androidx.compose.ui.unit.Dp
) {
    if (!user.pictureUrl.isNullOrBlank()) {
        AsyncImage(
            model = user.pictureUrl,
            contentDescription = user.displayName,
            modifier = Modifier
                .size(size)
                .clip(
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentScale = ContentScale.Crop
        )
    } else {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = user.displayName,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
private fun AccountSwitcherDialog(
    accounts: List<RobloxUser>,
    activeAccountId: Long,
    onDismiss: () -> Unit,
    onAccountSelected: (Long) -> Unit,
    onAddAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch Account",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                accounts.forEach { account ->
                    AccountSwitcherItem(
                        account = account,
                        selected =
                            account.id == activeAccountId,
                        onClick = {
                            onAccountSelected(account.id)
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = onAddAccount
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(
                        text = "Add Account",
                        style =
                            MaterialTheme.typography
                                .bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun AccountSwitcherItem(
    account: RobloxUser,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 10.dp
            ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            ProfilePicture(
                user = account,
                size = 42.dp
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.displayName,
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    fontWeight =
                        if (selected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )

                Text(
                    text = "@${account.name}",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Current account",
                    tint =
                        MaterialTheme.colorScheme
                            .primary
                )
            }
        }
    }
}

@Composable
private fun StudioNavigationRail(
    currentPage: StudioPage,
    onNavigate: (StudioPage) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationRailItem(
            selected = currentPage == StudioPage.Home,
            onClick = {
                onNavigate(StudioPage.Home)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text("Home")
            }
        )

        NavigationRailItem(
            selected = currentPage == StudioPage.Experiences,
            onClick = {
                onNavigate(StudioPage.Experiences)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Experiences"
                )
            },
            label = {
                Text("Experiences")
            }
        )

        NavigationRailItem(
            selected = currentPage == StudioPage.Settings,
            onClick = {
                onNavigate(StudioPage.Settings)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = {
                Text("Settings")
            }
        )
    }
}

@Composable
private fun StudioHomePage(
    user: RobloxUser,
    experiences: List<RobloxExperience>,
    loadingExperiences: Boolean,
    experienceError: String?,
    onNewExperience: () -> Unit,
    onOpenExisting: () -> Unit,
    onExperienceClick: (RobloxExperience) -> Unit,
    onSeeAll: () -> Unit
) {
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
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Welcome back, ${user.displayName}.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNewExperience
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("New Experience")
            }

            Button(
                onClick = onOpenExisting
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Open Existing")
            }
        }

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "See All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onSeeAll()
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        ExperienceContent(
            experiences = experiences,
            loadingExperiences = loadingExperiences,
            experienceError = experienceError,
            onExperienceClick = onExperienceClick,
            showNewCard = true,
            onNewExperience = onNewExperience
        )
    }
}

@Composable
private fun ExperiencesPage(
    experiences: List<RobloxExperience>,
    loadingExperiences: Boolean,
    experienceError: String?,
    onNewExperience: () -> Unit,
    onExperienceClick: (RobloxExperience) -> Unit,
) {
    var selectedTab by remember {
        mutableStateOf(ExperienceTab.Experiences)
    }

    var selectedGroupId by remember {
        mutableStateOf<Long?>(null)
    }

    val userExperiences = experiences.filter {
        it.ownerType == "User"
    }

    val groups =
        experiences
            .filter {
                it.ownerType == "Group"
            }
            .distinctBy {
                it.ownerId
            }
            .sortedBy {
                it.ownerName.lowercase()
            }

    val groupExperiences = experiences.filter {
        it.ownerType == "Group" && (selectedGroupId == null || it.ownerId == selectedGroupId )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 32.dp,
                vertical = 28.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Experiences",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Open and manage your Studio projects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            Button(
                onClick = onNewExperience
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("New Experience")
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        ExperienceTabs(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
            }
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        when (selectedTab) {
            ExperienceTab.Experiences -> {
                ExperienceContent(
                    experiences = userExperiences,
                    loadingExperiences = loadingExperiences,
                    experienceError = experienceError,
                    onExperienceClick = onExperienceClick,
                    showNewCard = true,
                    onNewExperience = onNewExperience,
                    emptyTitle = "No Experiences",
                    emptyDescription = "Create your first experience to get started."
                )
            }

            ExperienceTab.GroupExperiences -> {
                Column {
                    GroupSelector(
                        groups = groups,
                        selectedGroupId = selectedGroupId,
                        onGroupSelected = {
                            selectedGroupId = it
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    ExperienceContent(
                        experiences = groupExperiences,
                        loadingExperiences = loadingExperiences,
                        experienceError = experienceError,
                        onExperienceClick = onExperienceClick,
                        showNewCard = false,
                        onNewExperience = onNewExperience,
                        emptyTitle = "No Group Experiences",
                        emptyDescription =
                            if (selectedGroupId == null) {
                                "Experiences owned by groups you can edit will appear here."
                            } else {
                                "This group doesn't have any experiences you can edit."
                            }
                    )
                }
            }

            ExperienceTab.SharedWithMe -> {
                EmptyExperienceTab(
                    title = "Shared with Me",
                    description =
                        "Experiences other users have shared with you will appear here."
                )
            }

            ExperienceTab.Local -> {
                EmptyExperienceTab(
                    title = "Local",
                    description =
                        "Experiences saved locally on this device will appear here."
                )
            }
        }
    }
}

@Composable
private fun ExperienceTabs(
    selectedTab: ExperienceTab,
    onTabSelected: (ExperienceTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExperienceTabButton(
            text = "Experiences",
            selected = selectedTab == ExperienceTab.Experiences,
            onClick = {
                onTabSelected(ExperienceTab.Experiences)
            }
        )

        ExperienceTabButton(
            text = "Group Experiences",
            selected =
                selectedTab == ExperienceTab.GroupExperiences,
            onClick = {
                onTabSelected(ExperienceTab.GroupExperiences)
            }
        )

        ExperienceTabButton(
            text = "Shared with Me",
            selected =
                selectedTab == ExperienceTab.SharedWithMe,
            onClick = {
                onTabSelected(ExperienceTab.SharedWithMe)
            }
        )

        ExperienceTabButton(
            text = "Local",
            selected = selectedTab == ExperienceTab.Local,
            onClick = {
                onTabSelected(ExperienceTab.Local)
            }
        )
    }
}

@Composable
private fun GroupSelector(
    groups: List<RobloxExperience>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedGroup =
        groups.firstOrNull {
            it.ownerId == selectedGroupId
        }

    Box {
        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {
            Text(
                text =
                    selectedGroup?.ownerName
                        ?: "All Groups"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("All Groups")
                },
                onClick = {
                    onGroupSelected(null)
                    expanded = false
                }
            )

            groups
                .distinctBy {
                    it.ownerId
                }
                .sortedBy {
                    it.ownerName.lowercase()
                }
                .forEach { group ->
                    DropdownMenuItem(
                        text = {
                            Text(group.ownerName)
                        },
                        onClick = {
                            onGroupSelected(
                                group.ownerId
                            )

                            expanded = false
                        }
                    )
                }
        }
    }
}

@Composable
private fun ExperienceTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        shape = RoundedCornerShape(8.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight =
                if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
        )
    }
}

@Composable
private fun ExperienceContent(
    experiences: List<RobloxExperience>,
    loadingExperiences: Boolean,
    experienceError: String?,
    onExperienceClick: (RobloxExperience) -> Unit,
    showNewCard: Boolean,
    onNewExperience: () -> Unit,
    emptyTitle: String = "No Experiences",
    emptyDescription: String = "Create your first experience to get started."
) {
    when {
        loadingExperiences -> {
            Text(
                text = "Loading your experiences...",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }

        experienceError != null -> {
            Text(
                text = experienceError,
                color = MaterialTheme.colorScheme.error
            )
        }

        experiences.isEmpty() -> {
            EmptyExperienceTab(
                title = emptyTitle,
                description = emptyDescription
            )
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
                            onExperienceClick(experience)
                        }
                    )
                }

                if (showNewCard) {
                    NewExperienceCard(
                        onClick = onNewExperience
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyExperiences(
    onNewExperience: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(360.dp)
            .height(190.dp),
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
                imageVector = Icons.Default.Description,
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
                fontWeight = FontWeight.SemiBold
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

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Button(
                onClick = onNewExperience
            ) {
                Text("Create Experience")
            }
        }
    }
}

@Composable
private fun EmptyExperienceTab(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExperienceCard(
    experience: RobloxExperience,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .height(172.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp
                        )
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                if (!experience.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = experience.thumbnailUrl,
                        contentDescription = experience.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "No Thumbnail",
                        modifier = Modifier.align(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = experience.name,
                    style =
                        MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        experience.description
                            ?.takeIf { it.isNotBlank() }
                            ?: "No description",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NewExperienceCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .height(172.dp)
            .clickable(onClick = onClick),
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
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "New Experience",
                style =
                    MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsPage() {
    var selectedCategory by remember {
        mutableStateOf(SettingsCategory.Studio)
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .width(230.dp)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surface
                )
                .padding(12.dp)
        ) {
            Text(
                text = "Settings",
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 14.dp
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SettingsCategory(
                title = "Studio",
                selected =
                    selectedCategory ==
                            SettingsCategory.Studio,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Studio
                }
            )

            SettingsCategory(
                title = "Appearance",
                selected =
                    selectedCategory ==
                            SettingsCategory.Appearance,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Appearance
                }
            )

            SettingsCategory(
                title = "Editor",
                selected =
                    selectedCategory ==
                            SettingsCategory.Editor,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Editor
                }
            )

            SettingsCategory(
                title = "Account",
                selected =
                    selectedCategory ==
                            SettingsCategory.Account,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Account
                }
            )

            SettingsCategory(
                title = "Privacy",
                selected =
                    selectedCategory ==
                            SettingsCategory.Privacy,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Privacy
                }
            )

            SettingsCategory(
                title = "Network",
                selected =
                    selectedCategory ==
                            SettingsCategory.Network,
                onClick = {
                    selectedCategory =
                        SettingsCategory.Network
                }
            )

            SettingsCategory(
                title = "About",
                selected =
                    selectedCategory ==
                            SettingsCategory.About,
                onClick = {
                    selectedCategory =
                        SettingsCategory.About
                }
            )
        }

        SettingsContent(
            category = selectedCategory
        )
    }
}

@Composable
private fun SettingsContent(
    category: SettingsCategory
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp)
    ) {
        Text(
            text = when (category) {
                SettingsCategory.Studio -> "Studio"
                SettingsCategory.Appearance -> "Appearance"
                SettingsCategory.Editor -> "Editor"
                SettingsCategory.Account -> "Account"
                SettingsCategory.Privacy -> "Privacy"
                SettingsCategory.Network -> "Network"
                SettingsCategory.About -> "About"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        when (category) {
            SettingsCategory.Studio -> {
                SettingsSection(
                    title = "Studio Settings",
                    description =
                        "Configure how Stew Studio behaves."
                )
            }

            SettingsCategory.Appearance -> {
                SettingsSection(
                    title = "Appearance",
                    description =
                        "Customize the appearance of Stew Studio."
                )
            }

            SettingsCategory.Editor -> {
                SettingsSection(
                    title = "Editor",
                    description =
                        "Configure viewport, grid, snapping, and editing behavior."
                )
            }

            SettingsCategory.Account -> {
                SettingsSection(
                    title = "Account",
                    description =
                        "Manage the Roblox account connected to Stew."
                )
            }

            SettingsCategory.Privacy -> {
                SettingsSection(
                    title = "Privacy",
                    description =
                        "Manage privacy and data settings."
                )
            }

            SettingsCategory.Network -> {
                SettingsSection(
                    title = "Network",
                    description =
                        "Configure network and cloud behavior."
                )
            }

            SettingsCategory.About -> {
                SettingsSection(
                    title = "About Stew Studio",
                    description =
                        "Version and application information."
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(7.dp),
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight =
                if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
        )
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
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                Color.Transparent
            },
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StudioDropdownMenu(
    menu: String,
    onDismiss: () -> Unit,
    onNavigate: (StudioPage) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = when (menu) {
                    "File" -> 120.dp
                    "Plugins" -> 175.dp
                    "Help" -> 245.dp
                    else -> 120.dp
                }
            )
    ) {
        Card(
            modifier = Modifier.width(230.dp),
            shape = RoundedCornerShape(8.dp),
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
                            icon = Icons.Default.FolderOpen,
                            text = "Open Experience",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon = Icons.Default.Description,
                            text = "Save",
                            onClick = onDismiss
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(
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
                            icon = Icons.Default.Extension,
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
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            text = "Documentation",
                            onClick = onDismiss
                        )

                        MenuItem(
                            icon = Icons.Default.Settings,
                            text = "Settings",
                            onClick = {
                                onNavigate(StudioPage.Settings)
                            }
                        )

                        MenuItem(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
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
    icon: ImageVector,
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
        verticalAlignment = Alignment.CenterVertically
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}