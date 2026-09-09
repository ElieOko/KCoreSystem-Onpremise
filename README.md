# KCoreSystem — Statistiques Scolaires

Application professionnelle de **collecte, gestion, validation et centralisation des statistiques scolaires**, développée en **Kotlin Multiplatform** (Desktop + Android).

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| UI | Compose Multiplatform + Material 3 |
| Desktop | JVM (Windows, macOS, Linux) |
| Android | Application native (minSdk 26) |
| Backend | Supabase (PostgreSQL, Auth, Realtime, Storage) |
| Base locale | SQLDelight (offline first) |
| Architecture | Clean Architecture + MVVM |
| DI | Koin |

## Structure du projet

```
KCoreSystem/
├── androidApp/          # Application Android
├── desktopApp/          # Application Desktop
├── shared/              # Code KMP partagé (domain, data, presentation)
├── supabase/            # Migrations PostgreSQL, RLS, vues agrégées
└── docs/                # Documentation architecture
```

## Documentation

| Document | Description |
|----------|-------------|
| [01-analyse-besoins.md](docs/01-analyse-besoins.md) | Analyse des besoins et acteurs |
| [02-architecture-technique.md](docs/02-architecture-technique.md) | Architecture Clean + MVVM |
| [03-structure-projet.md](docs/03-structure-projet.md) | Arborescence détaillée |
| [04-modele-donnees.md](docs/04-modele-donnees.md) | Entités et relations |
| [05-strategie-offline-sync.md](docs/05-strategie-offline-sync.md) | Offline first + synchronisation |
| [06-plan-developpement.md](docs/06-plan-developpement.md) | Plan de développement progressif |

## Démarrage rapide

### Prérequis

- JDK 17+ (JDK 21 recommandé)
- Android SDK (compileSdk 36) pour l’application mobile
- Projet Supabase configuré (voir [supabase/README.md](supabase/README.md))

### Configuration

```bash
cp local.properties.example local.properties
# Remplir supabase.url et supabase.anon.key
```

### Lancer l'application Desktop

```bash
./gradlew :desktopApp:run
```

### Lancer / packager l'application Android

Installer le SDK Android, puis renseigner `sdk.dir` dans `local.properties` (voir `local.properties.example`).

```bash
# APK debug
./gradlew :androidApp:assembleDebug
```

APK généré : `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Depuis Android Studio : ouvrir le projet, sélectionner la configuration `androidApp`, lancer sur un émulateur ou un appareil (API 26+).

### Mode démo (sans Supabase)

Connectez-vous avec `demo@local` / `demo` — des données de démonstration sont chargées automatiquement.

### Tests

```bash
./gradlew :shared:jvmTest
```

## Modules livrés

| Module | Description |
|--------|-------------|
| Authentification | Supabase + mode démo (Desktop et Android) |
| Dashboard | Cartes KPI + analyses |
| Écoles | CRUD, recherche, sync offline |
| Statistiques | Effectifs, âge/sexe, enseignants, administratif, ouvriers, inscriptions, certificatives |
| Déclarations | Liste filtrable par statut |
| Validation | Valider, rejeter, demander correction |
| Centralisation | Fichier central agrégé sous-division |
| Rapports | Export Excel/PDF (Desktop) ou CSV/texte (Android) |
| Utilisateurs | Gestion des comptes |
| Paramètres | Thème clair/sombre |

## État du projet

**Desktop** — fonctionnel (saisie, validation, fichier central, packaging `.exe` Windows)

**Android** — application Compose (navigation bas de page : Accueil, Saisie, Fichier central, Plus)

### Packaging Windows (`.exe`)

Sur une machine Windows avec JDK 21 :

```bat
gradlew.bat :desktopApp:createDistributable
```

Exécutable : `desktopApp\build\compose\binaries\main\app\KCoreSystem\KCoreSystem.exe`

Installer WiX :

```bat
gradlew.bat :desktopApp:packageExe
```

## Rôles utilisateurs

- **Super Admin** — Gestion nationale
- **Admin Provincial** — Vue province
- **Admin Sous-Division** — Validation et centralisation (rôle principal Desktop)
- **École** — Saisie et soumission des statistiques

## Licence

Projet privé — Tous droits réservés.
