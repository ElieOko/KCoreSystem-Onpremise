# Arborescence du projet — Structure détaillée

## 1. Structure racine

```
KCoreSystem/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── local.properties.example          # Template config Supabase
│
├── docs/                             # Documentation architecture
│   ├── 01-analyse-besoins.md
│   ├── 02-architecture-technique.md
│   ├── 03-structure-projet.md
│   ├── 04-modele-donnees.md
│   ├── 05-strategie-offline-sync.md
│   └── 06-plan-developpement.md
│
├── supabase/
│   ├── config.toml                   # Config Supabase CLI
│   ├── migrations/
│   │   ├── 20250903000001_initial_schema.sql
│   │   ├── 20250903000002_rls_policies.sql
│   │   └── 20250903000003_aggregation_views.sql
│   ├── functions/
│   │   ├── validate-submission/
│   │   ├── reject-submission/
│   │   ├── calculate-statistics/
│   │   └── send-notification/
│   └── seed/
│       └── dev_seed.sql              # Données de test
│
├── desktopApp/
│   └── src/main/kotlin/com/schoolstats/
│       ├── Main.kt
│       ├── presentation/
│       │   ├── AppDesktop.kt
│       │   ├── navigation/
│       │   │   ├── DesktopNavigator.kt
│       │   │   └── AppRoute.kt
│       │   ├── layout/
│       │   │   ├── MainLayout.kt
│       │   │   ├── Sidebar.kt
│       │   │   └── Header.kt
│       │   └── screens/
│       │       ├── auth/
│       │       │   ├── LoginScreen.kt
│       │       │   └── ForgotPasswordScreen.kt
│       │       ├── dashboard/
│       │       │   └── DashboardScreen.kt
│       │       ├── schools/
│       │       │   ├── SchoolListScreen.kt
│       │       │   ├── SchoolDetailScreen.kt
│       │       │   └── SchoolFormDialog.kt
│       │       ├── statistics/
│       │       │   ├── primary/
│       │       │   ├── secondary/
│       │       │   ├── teachers/
│       │       │   ├── enrollment/
│       │       │   └── certification/
│       │       ├── submissions/
│       │       │   ├── SubmissionListScreen.kt
│       │       │   └── ValidationScreen.kt
│       │       ├── centralization/
│       │       │   └── CentralizationScreen.kt
│       │       ├── reports/
│       │       │   └── ReportsScreen.kt
│       │       ├── users/
│       │       │   └── UserManagementScreen.kt
│       │       └── settings/
│       │           └── SettingsScreen.kt
│       ├── components/
│       │   ├── tables/
│       │   │   ├── DataTable.kt
│       │   │   ├── PaginationBar.kt
│       │   │   └── ColumnConfig.kt
│       │   ├── charts/
│       │   │   └── ChartComponents.kt
│       │   ├── forms/
│       │   │   └── StatFormTable.kt
│       │   └── common/
│       │       ├── LoadingState.kt
│       │       ├── EmptyState.kt
│       │       └── ErrorState.kt
│       └── platform/
│           ├── export/
│           │   ├── ExcelExporter.kt
│           │   └── PdfExporter.kt
│           └── file/
│               └── FilePicker.kt
│
└── shared/
    └── src/
        ├── commonMain/
        │   ├── kotlin/com/schoolstats/
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   │   ├── User.kt
        │   │   │   │   ├── UserRole.kt
        │   │   │   │   ├── School.kt
        │   │   │   │   ├── SchoolYear.kt
        │   │   │   │   ├── Submission.kt
        │   │   │   │   ├── SubmissionStatus.kt
        │   │   │   │   ├── SyncStatus.kt
        │   │   │   │   ├── PrimaryClassStat.kt
        │   │   │   │   ├── SecondaryStudentStat.kt
        │   │   │   │   ├── TeacherStat.kt
        │   │   │   │   ├── EnrollmentStat.kt
        │   │   │   │   └── CertificationResult.kt
        │   │   │   ├── repository/
        │   │   │   │   ├── AuthRepository.kt
        │   │   │   │   ├── SchoolRepository.kt
        │   │   │   │   ├── StatisticsRepository.kt
        │   │   │   │   ├── SubmissionRepository.kt
        │   │   │   │   ├── CentralizationRepository.kt
        │   │   │   │   └── NotificationRepository.kt
        │   │   │   ├── usecase/
        │   │   │   │   ├── auth/
        │   │   │   │   ├── school/
        │   │   │   │   ├── statistics/
        │   │   │   │   ├── submission/
        │   │   │   │   └── centralization/
        │   │   │   └── validation/
        │   │   │       ├── StatValidator.kt
        │   │   │       └── EnrollmentValidator.kt
        │   │   ├── data/
        │   │   │   ├── remote/
        │   │   │   │   ├── SupabaseClientProvider.kt
        │   │   │   │   ├── dto/
        │   │   │   │   └── datasource/
        │   │   │   ├── local/
        │   │   │   │   ├── database/
        │   │   │   │   │   └── SchoolStatsDatabase.sq
        │   │   │   │   └── datasource/
        │   │   │   ├── repository/
        │   │   │   ├── sync/
        │   │   │   │   ├── SyncManager.kt
        │   │   │   │   ├── SyncWorker.kt
        │   │   │   │   ├── OutboxQueue.kt
        │   │   │   │   ├── ConflictResolver.kt
        │   │   │   │   └── NetworkMonitor.kt
        │   │   │   └── mapper/
        │   │   ├── presentation/
        │   │   │   ├── viewmodel/
        │   │   │   ├── state/
        │   │   │   └── theme/
        │   │   │       ├── Color.kt
        │   │   │       ├── Typography.kt
        │   │   │       └── Theme.kt
        │   │   ├── di/
        │   │   │   ├── AppModule.kt
        │   │   │   ├── DataModule.kt
        │   │   │   ├── DomainModule.kt
        │   │   │   └── ViewModelModule.kt
        │   │   └── util/
        │   │       ├── DateUtils.kt
        │   │       └── NumberUtils.kt
        │   └── sqldelight/
        │       └── com/schoolstats/database/
        │           ├── SchoolStatsDatabase.sq
        │           ├── Organization.sq
        │           ├── Statistics.sq
        │           ├── Submissions.sq
        │           └── SyncOutbox.sq
        │
        ├── commonTest/kotlin/com/schoolstats/
        │   ├── domain/
        │   │   ├── validation/
        │   │   └── usecase/
        │   └── data/
        │       └── sync/
        │
        ├── jvmMain/kotlin/com/schoolstats/
        │   ├── data/local/
        │   │   └── DatabaseDriverFactory.jvm.kt
        │   └── di/
        │       └── PlatformModule.jvm.kt
        │
        └── androidMain/kotlin/com/schoolstats/   # Phase 2
            ├── data/local/
            │   └── DatabaseDriverFactory.android.kt
            └── di/
                └── PlatformModule.android.kt
```

---

## 2. Conventions de nommage

| Élément | Convention | Exemple |
|---------|------------|---------|
| Package | `com.schoolstats.<couche>.<feature>` | `com.schoolstats.domain.model` |
| ViewModel | `<Feature>ViewModel` | `DashboardViewModel` |
| Use Case | `<Action><Entity>UseCase` | `SubmitDeclarationUseCase` |
| Repository | `<Entity>Repository` | `SchoolRepository` |
| UiState | `<Feature>UiState` | `sealed class DashboardUiState` |
| DTO Supabase | `<Entity>Dto` | `SchoolDto` |
| Table SQL | `snake_case` pluriel | `student_statistics` |
| Migration | `YYYYMMDDHHMMSS_description.sql` | `20250903000001_initial_schema.sql` |

---

## 3. Séparation Desktop vs Common

| Élément | Emplacement | Raison |
|---------|-------------|--------|
| ViewModels | `shared/commonMain` | Réutilisable Android |
| Use Cases | `shared/commonMain` | Logique métier partagée |
| Repositories | `shared/commonMain` | Accès données partagé |
| Theme Material 3 | `shared/commonMain` | UI cohérente multi-plateforme |
| Sidebar / Layout Desktop | `desktopApp` | Spécifique grands écrans |
| DataTable avancé | `desktopApp` | Pagination desktop |
| Export Excel/PDF | `desktopApp/jvmMain` | Libs JVM uniquement |
| Navigation Desktop | `desktopApp` | Fenêtres, dialogs desktop |
| SQLDelight driver | `jvmMain` / `androidMain` | Platform-specific |

---

## 4. Fichiers de configuration

### `local.properties.example`

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key
```

### Dépendances à ajouter (`libs.versions.toml`)

```toml
[versions]
supabase-kt = "3.0.0"
sqldelight = "2.0.2"
koin = "4.0.0"
kotlinx-serialization = "1.7.3"
kamel = "1.0.0"  # images si nécessaire

[libraries]
supabase-bom = { module = "io.github.jan-tennert.supabase:bom", version.ref = "supabase-kt" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt" }
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt" }
supabase-realtime = { module = "io.github.jan-tennert.supabase:realtime-kt" }
supabase-storage = { module = "io.github.jan-tennert.supabase:storage-kt" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
```

---

## 5. Menu Sidebar par rôle

| Route | Super Admin | Admin Provincial | Admin SD | École |
|-------|:-----------:|:----------------:|:--------:|:-----:|
| Dashboard | ✅ | ✅ | ✅ | ✅ (limité) |
| Écoles | ✅ | ✅ (lecture) | ✅ | ❌ |
| Statistiques scolaires | ✅ | ✅ | ✅ | ✅ (sa école) |
| Personnel enseignant | ✅ | ✅ | ✅ | ✅ |
| Déclarations reçues | ✅ | ✅ | ✅ | ❌ |
| Validation | ✅ | ❌ | ✅ | ❌ |
| Centralisation | ✅ | ✅ | ✅ | ❌ |
| Analyses | ✅ | ✅ | ✅ | ❌ |
| Rapports | ✅ | ✅ | ✅ | ✅ (sa école) |
| Utilisateurs | ✅ | ✅ (province) | ✅ (SD) | ❌ |
| Paramètres | ✅ | ✅ | ✅ | ✅ |
