package com.schoolstats.desktop.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.data.sync.SyncState
import com.schoolstats.desktop.navigation.AppRoute
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.model.UserRole

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
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (expandedSidebar) {
            Sidebar(
                routes = routes,
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = Modifier.width(260.dp).fillMaxHeight(),
            )
        }
        Column(Modifier.fillMaxSize()) {
            Header(
                title = currentRoute.label,
                profile = profile,
                syncState = syncState,
                onToggleSidebar = onToggleSidebar,
                onSync = onSync,
                onLogout = onLogout,
            )
            Box(Modifier.fillMaxSize().padding(20.dp)) {
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
        Column(Modifier.padding(vertical = 20.dp, horizontal = 12.dp)) {
            Text(
                "KCoreSystem",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Text(
                "Statistiques scolaires",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
            Spacer(Modifier.height(12.dp))
            routes.forEach { route ->
                val selected = route == currentRoute
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(route) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.primary,
                        )
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = iconFor(route),
                        contentDescription = route.label,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        route.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
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

@Composable
private fun Header(
    title: String,
    profile: UserProfile,
    syncState: SyncState,
    onToggleSidebar: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
) {
    Surface(shadowElevation = 1.dp, color = MaterialTheme.colorScheme.surface) {
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
                    Text(roleLabel(profile.role), style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Déconnexion")
                }
            }
        }
    }
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.SUPER_ADMIN -> "Super admin"
    UserRole.ADMIN_PROVINCIAL -> "Admin provincial"
    UserRole.ADMIN_SOUS_DIVISION -> "Sous-division"
    UserRole.ECOLE -> "École"
}
