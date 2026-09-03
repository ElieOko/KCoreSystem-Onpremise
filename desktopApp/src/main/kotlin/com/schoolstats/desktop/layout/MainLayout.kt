package com.schoolstats.desktop.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.data.sync.SyncState
import com.schoolstats.desktop.navigation.AppRoute
import com.schoolstats.domain.model.UserProfile

@Composable
fun MainLayout(
    profile: UserProfile,
    routes: List<AppRoute>,
    currentRoute: AppRoute,
    syncState: SyncState,
    expandedSidebar: Boolean,
    onToggleSidebar: () -> Unit,
    onNavigate: (AppRoute) -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (expandedSidebar) {
            Sidebar(
                routes = routes,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = Modifier.width(240.dp).fillMaxHeight(),
            )
        }
        Column(Modifier.fillMaxSize()) {
            Header(
                title = currentRoute.label,
                profile = profile,
                syncState = syncState,
                expandedSidebar = expandedSidebar,
                onToggleSidebar = onToggleSidebar,
                onSync = onSync,
                onLogout = onLogout,
            )
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun Sidebar(
    routes: List<AppRoute>,
    currentRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Text(
                "KCoreSystem",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            Text(
                "Statistiques scolaires",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            HorizontalDivider(
                Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            )
            routes.forEach { route ->
                val selected = route == currentRoute
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(route) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary,
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        route.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    profile: UserProfile,
    syncState: SyncState,
    expandedSidebar: Boolean,
    onToggleSidebar: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleSidebar) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val syncLabel = when {
                    syncState.isSyncing -> "Synchronisation..."
                    syncState.failedCount > 0 -> "Échec sync"
                    syncState.pendingCount > 0 -> "${syncState.pendingCount} en attente"
                    else -> if (syncState.isOnline) "Synchronisé" else "Hors ligne"
                }
                Text(syncLabel, style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = onSync) {
                    Icon(Icons.Default.Sync, contentDescription = "Synchroniser")
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(profile.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(profile.role.name, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion")
                }
            }
        }
    }
}
