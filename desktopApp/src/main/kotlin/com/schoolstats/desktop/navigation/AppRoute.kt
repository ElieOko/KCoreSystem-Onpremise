package com.schoolstats.desktop.navigation

import com.schoolstats.domain.model.UserRole

enum class AppRoute(
    val label: String,
    val allowedRoles: Set<UserRole> = UserRole.entries.toSet(),
) {
    Dashboard("Tableau de bord"),
    Schools("Écoles", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Statistics("Statistiques scolaires"),
    Teachers("Personnel"),
    Submissions("Déclarations reçues", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Validation("Validation", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_SOUS_DIVISION)),
    Centralization("Fichier central", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Analytics("Analyses", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Reports("Rapports"),
    Users("Utilisateurs", setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN_PROVINCIAL, UserRole.ADMIN_SOUS_DIVISION)),
    Settings("Paramètres"),
}

fun routesForRole(role: UserRole?): List<AppRoute> {
    if (role == null) return emptyList()
    return AppRoute.entries.filter { route -> role in route.allowedRoles }
}
