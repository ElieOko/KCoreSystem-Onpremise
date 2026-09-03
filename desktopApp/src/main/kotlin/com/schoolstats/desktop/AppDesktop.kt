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
import com.schoolstats.desktop.screens.PlaceholderScreen
import com.schoolstats.desktop.screens.PrimaryStatisticsScreen
import com.schoolstats.desktop.screens.SubmissionsScreen
import com.schoolstats.desktop.screens.auth.LoginScreen
import com.schoolstats.desktop.screens.dashboard.DashboardScreen
import com.schoolstats.desktop.screens.schools.SchoolsScreen
import com.schoolstats.presentation.theme.SchoolStatsTheme
import com.schoolstats.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppDesktop() {
    SchoolStatsTheme {
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
                    AppRoute.Statistics -> PrimaryStatisticsScreen()
                    AppRoute.Teachers -> PlaceholderScreen("Personnel enseignant", "Effectifs par niveau, branche et sexe.")
                    AppRoute.Submissions -> SubmissionsScreen()
                    AppRoute.Validation -> PlaceholderScreen("Validation", "Vérification et validation des déclarations.")
                    AppRoute.Centralization -> PlaceholderScreen("Centralisation", "Agrégation des statistiques de la Sous-Division.")
                    AppRoute.Analytics -> PlaceholderScreen("Analyses", "Graphiques et analyses filtrables.")
                    AppRoute.Reports -> PlaceholderScreen("Rapports", "Génération de rapports Excel et PDF.")
                    AppRoute.Users -> PlaceholderScreen("Utilisateurs", "Gestion des comptes utilisateurs.")
                    AppRoute.Settings -> PlaceholderScreen("Paramètres", "Configuration du système et synchronisation.")
                }
            }
        }
    }
}
