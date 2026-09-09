package com.schoolstats.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.data.sync.SyncState
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.model.UserRole
import com.schoolstats.presentation.navigation.AppRoute
import com.schoolstats.presentation.navigation.routesForRole
import com.schoolstats.presentation.screens.SubmissionsScreen
import com.schoolstats.presentation.screens.analytics.AnalyticsScreen
import com.schoolstats.presentation.screens.auth.LoginScreen
import com.schoolstats.presentation.screens.centralization.CentralizationScreen
import com.schoolstats.presentation.screens.dashboard.DashboardScreen
import com.schoolstats.presentation.screens.reports.ReportsScreen
import com.schoolstats.presentation.screens.schools.SchoolsScreen
import com.schoolstats.presentation.screens.settings.SettingsScreen
import com.schoolstats.presentation.screens.statistics.StatisticsScreen
import com.schoolstats.presentation.screens.users.UsersScreen
import com.schoolstats.presentation.screens.validation.ValidationScreen
import com.schoolstats.presentation.theme.SchoolStatsTheme
import com.schoolstats.presentation.viewmodel.AuthViewModel
import com.schoolstats.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private enum class MobileTab(val label: String, val icon: ImageVector, val route: AppRoute?) {
    Home("Accueil", Icons.Default.SpaceDashboard, AppRoute.Dashboard),
    Census("Saisie", Icons.Default.BarChart, AppRoute.Statistics),
    Central("Fichier", Icons.Default.Hub, AppRoute.Centralization),
    More("Plus", Icons.Default.MoreHoriz, null),
}

@Composable
fun AppAndroid() {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.uiState.collectAsState()
    SchoolStatsTheme(darkTheme = settings.darkTheme) {
        val authViewModel: AuthViewModel = koinViewModel()
        val syncManager: SyncManager = koinInject()
        val authState by authViewModel.uiState.collectAsState()
        val syncState by syncManager.state.collectAsState()
        val scope = rememberCoroutineScope()

        if (!authState.isAuthenticated || authState.profile == null) {
            LoginScreen(authViewModel)
        } else {
            val profile = authState.profile!!
            val routes = routesForRole(profile.role)
            MobileShell(
                profile = profile,
                routes = routes,
                syncState = syncState,
                onSync = { scope.launch { syncManager.syncNow() } },
                onLogout = authViewModel::logout,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileShell(
    profile: UserProfile,
    routes: List<AppRoute>,
    syncState: SyncState,
    onSync: () -> Unit,
    onLogout: () -> Unit,
) {
    val tabs = remember(routes) {
        MobileTab.entries.filter { tab ->
            tab.route == null || tab.route in routes
        }
    }
    var currentTab by remember { mutableStateOf(tabs.first()) }
    var currentRoute by remember { mutableStateOf(AppRoute.Dashboard) }

    val title = when {
        currentTab == MobileTab.More && currentRoute != AppRoute.Dashboard -> currentRoute.label
        else -> currentTab.label
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${profile.fullName} · ${roleLabel(profile.role)}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                actions = {
                    val syncLabel = when {
                        syncState.isSyncing -> "Sync…"
                        syncState.failedCount > 0 -> "Échec"
                        syncState.pendingCount > 0 -> "${syncState.pendingCount}"
                        else -> if (syncState.isOnline) "OK" else "Hors ligne"
                    }
                    Text(syncLabel, style = MaterialTheme.typography.labelSmall)
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Sync, contentDescription = "Synchroniser")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = {
                            currentTab = tab
                            currentRoute = tab.route ?: AppRoute.Dashboard
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 12.dp, vertical = 8.dp)) {
            when {
                currentTab == MobileTab.Home -> DashboardScreen(compact = true)
                currentTab == MobileTab.Census -> StatisticsScreen(compact = true)
                currentTab == MobileTab.Central -> CentralizationScreen(compact = true)
                currentTab == MobileTab.More && currentRoute == AppRoute.Dashboard -> {
                    MoreMenu(
                        routes = routes.filter { it !in setOf(AppRoute.Dashboard, AppRoute.Statistics, AppRoute.Centralization) },
                        onSelect = { currentRoute = it },
                    )
                }
                else -> RouteContent(currentRoute)
            }
        }
    }
}

@Composable
private fun RouteContent(route: AppRoute) {
    when (route) {
        AppRoute.Dashboard -> DashboardScreen(compact = true)
        AppRoute.Schools -> SchoolsScreen()
        AppRoute.Statistics -> StatisticsScreen(compact = true)
        AppRoute.Teachers -> StatisticsScreen(initialTab = 2, compact = true)
        AppRoute.Submissions -> SubmissionsScreen()
        AppRoute.Validation -> ValidationScreen(compact = true)
        AppRoute.Centralization -> CentralizationScreen(compact = true)
        AppRoute.Analytics -> AnalyticsScreen(compact = true)
        AppRoute.Reports -> ReportsScreen()
        AppRoute.Users -> UsersScreen()
        AppRoute.Settings -> SettingsScreen(databaseHint = "schoolstats.db (stockage interne)")
    }
}

@Composable
private fun MoreMenu(
    routes: List<AppRoute>,
    onSelect: (AppRoute) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Plus de modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        routes.forEach { route ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(route) },
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(iconFor(route), contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(route.label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }
            HorizontalDivider()
        }
    }
}

private fun iconFor(route: AppRoute): ImageVector = when (route) {
    AppRoute.Dashboard -> Icons.Default.SpaceDashboard
    AppRoute.Schools -> Icons.Default.School
    AppRoute.Statistics -> Icons.Default.BarChart
    AppRoute.Teachers -> Icons.Default.Groups
    AppRoute.Submissions -> Icons.Default.Inbox
    AppRoute.Validation -> Icons.Default.Verified
    AppRoute.Centralization -> Icons.Default.Hub
    AppRoute.Analytics -> Icons.Default.Analytics
    AppRoute.Reports -> Icons.Default.Description
    AppRoute.Users -> Icons.Default.People
    AppRoute.Settings -> Icons.Default.Settings
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.SUPER_ADMIN -> "Super admin"
    UserRole.ADMIN_PROVINCIAL -> "Admin provincial"
    UserRole.ADMIN_SOUS_DIVISION -> "Sous-division"
    UserRole.ECOLE -> "École"
}
