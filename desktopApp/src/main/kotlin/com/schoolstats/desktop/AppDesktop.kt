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
                    AppRoute.Teachers -> StatisticsScreen(initialTab = 2)
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
