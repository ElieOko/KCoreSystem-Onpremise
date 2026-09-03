# KCoreSystem — Statistiques Scolaires

Application professionnelle de **collecte, gestion, validation et centralisation des statistiques scolaires**, développée en **Kotlin Multiplatform** avec approche **Desktop First**.

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| UI | Compose Multiplatform + Material 3 |
| Desktop | JVM (Windows, macOS, Linux) |
| Backend | Supabase (PostgreSQL, Auth, Realtime, Storage) |
| Base locale | SQLDelight (offline first) |
| Architecture | Clean Architecture + MVVM |
| DI | Koin |

## Structure du projet

```
KCoreSystem/
├── desktopApp/          # Application Desktop (Phase 1)
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

- JDK 17+
- Projet Supabase configuré (voir [supabase/README.md](supabase/README.md))

### Configuration

```bash
cp local.properties.example local.properties
# Remplir supabase.url et supabase.anon.key
```

### Lancer l'application Desktop

#### Option 1 — Terminal (recommandé)

```bash
./gradlew :desktopApp:run
# ou raccourci :
./gradlew runDesktop
```

#### Option 2 — Cursor / IntelliJ IDEA

Le projet inclut des configurations de lancement partagées dans `.run/` :

1. Ouvrez le sélecteur de configurations (en haut à droite)
2. Choisissez **desktopApp**
3. Cliquez sur **Run** (▶)

Si la config n'apparaît pas : **Run → Edit Configurations → + → Gradle** → tâche `:desktopApp:run`

#### Option 3 — VS Code / Cursor (tasks)

1. `Ctrl+Shift+P` → **Tasks: Run Task**
2. Sélectionnez **KCoreSystem: Run Desktop**

Ou utilisez **Run and Debug** avec la config **KCoreSystem Desktop** (`.vscode/launch.json`).

#### Créer un installateur Windows

```bash
./gradlew :desktopApp:packageMsi
```

### Mode démo (sans Supabase)

Connectez-vous avec `demo@local` / `demo` — des données de démonstration sont chargées automatiquement.

### Tests

```bash
./gradlew :shared:jvmTest
```

## Modules livrés (Phase 1 Desktop)

| Module | Description |
|--------|-------------|
| Authentification | Supabase + mode démo |
| Dashboard | 10 cartes KPI + analyses |
| Écoles | CRUD, recherche, sync offline |
| Statistiques | Primaire, secondaire, enseignants, inscriptions, certificatives |
| Déclarations | Liste filtrable par statut |
| Validation | Valider, rejeter, demander correction |
| Centralisation | Agrégation complète sous-division |
| Rapports | Export Excel et PDF |
| Utilisateurs | Gestion des comptes |
| Paramètres | Thème clair/sombre |

## État du projet

**Phase 1 Desktop — fonctionnelle** (étapes 0 à 15 complétées)

**Phase 2** : Version Android (réutilisation du code `commonMain`)

## Rôles utilisateurs

- **Super Admin** — Gestion nationale
- **Admin Provincial** — Vue province
- **Admin Sous-Division** — Validation et centralisation (rôle principal Desktop)
- **École** — Saisie et soumission des statistiques

## Licence

Projet privé — Tous droits réservés.
