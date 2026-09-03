package com.schoolstats.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.desktop.layout.MainLayout
import com.schoolstats.desktop.navigation.AppRoute
import com.schoolstats.desktop.navigation.routesForRole
import com.schoolstats.desktop.screens.SubmissionsScreen
import com.schoolstats.desktop.screens.analytics.AnalyticsScreen
import com.schoolstats.desktop.screens.auth.LoginScreen
import com.schoolstats.desktop.screens.centralization.CentralizationScreen
import com.schoolstats.desktop.screens.dashboard.DashboardScreen
import com.schoolstats.desktop.screens.reports.ReportsScreen
import com.schoolstats.desktop.screens.schools.SchoolsScreen
import com.schoolstats.desktop.screens.settings.SettingsScreen
import com.schoolstats.desktop.screens.statistics.StatisticsScreen
import com.schoolstats.desktop.screens.users.UsersScreen
import com.schoolstats.desktop.screens.validation.ValidationScreen
import com.schoolstats.presentation.theme.SchoolStatsTheme
import com.schoolstats.presentation.viewmodel.AuthViewModel
import com.schoolstats.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppDesktop() {
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
            var currentRoute by remember { mutableStateOf(AppRoute.Dashboard) }
            var expandedSidebar by remember { mutableStateOf(true) }

            MainLayout(
                profile = profile,
                routes = routes,
                currentRoute = currentRoute,
                syncState = syncState,
                expandedSidebar = expandedSidebar,
                onToggleSidebar = { expandedSidebar = !expandedSidebar },
                onNavigate = { currentRoute = it },
                onSync = { scope.launch { syncManager.syncNow() } },
                onLogout = authViewModel::logout,
            ) {
                when (currentRoute) {
                    AppRoute.Dashboard -> DashboardScreen()
                    AppRoute.Schools -> SchoolsScreen()
                    AppRoute.Statistics -> StatisticsScreen()
                    AppRoute.Teachers -> StatisticsScreen()
                    AppRoute.Submissions -> SubmissionsScreen()
                    AppRoute.Validation -> ValidationScreen()
                    AppRoute.Centralization -> CentralizationScreen()
                    AppRoute.Analytics -> AnalyticsScreen()
                    AppRoute.Reports -> ReportsScreen()
                    AppRoute.Users -> UsersScreen()
                    AppRoute.Settings -> SettingsScreen()
                }
            }
        }
    }
}
