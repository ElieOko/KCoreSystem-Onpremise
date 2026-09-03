package com.schoolstats.desktop.navigation

import com.schoolstats.domain.model.UserRole

enum class AppRoute(
    val label: String,
    val allowedRoles: Set<UserRole> = UserRole.entries.toSet(),
) {
    Dashboard("Dashboard"),
    Schools("Écoles", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Statistics("Statistiques scolaires"),
    Teachers("Personnel enseignant"),
    Submissions("Déclarations reçues", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Validation("Validation", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_SOUS_DIVISION)),
    Centralization("Centralisation", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Analytics("Analyses", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Reports("Rapports"),
    Users("Utilisateurs", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Settings("Paramètres"),
}

fun routesForRole(role: UserRole?): List<AppRoute> {
    if (role == null) return emptyList()
    return AppRoute.entries.filter { route -> role in route.allowedRoles }
}
