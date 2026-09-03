# Plan de développement — Statut d'avancement

## Étape 0 — Architecture ✅
Documentation complète, schéma Supabase, RLS, vues agrégées.

## Étape 1 — Fondation KMP ✅
Supabase-kt, SQLDelight, Koin, serialization, package `com.schoolstats`.

## Étape 2 — Schéma Supabase ✅
Migrations SQL prêtes (`supabase/migrations/`), seed dev, Edge Functions stubs.

## Étape 3 — Authentification ✅
Login Supabase + mode démo `demo@local` / `demo`.

## Étape 4 — Layout Desktop ✅
Sidebar, header, navigation par rôle, thème clair/sombre.

## Étape 5 — Offline First ✅
SQLDelight, SyncManager, outbox, base `~/.kcoresystem/schoolstats.db`.

## Étape 6 — Gestion Écoles ✅
CRUD, recherche, formulaire, sync.

## Étape 7-9 — Statistiques ✅
- Primaire (formulaire éditable avec totaux)
- Secondaire, enseignants, inscriptions, certificatives
- Validations métier (StatValidator)

## Étape 10 — Workflow Soumission/Validation ✅
Soumettre, valider, rejeter, demander correction + notifications locales.

## Étape 11 — Dashboard ✅
8+ cartes KPI, garçons/filles, données démo.

## Étape 12 — Centralisation ✅
Page agrégation complète (classes, sections, enseignants, inscriptions, certificatives).

## Étape 13 — Rapports & Exports ✅
Export Excel (Apache POI) et PDF (OpenPDF).

## Étape 14 — Utilisateurs & Paramètres ✅
Liste utilisateurs, paramètres thème/sync.

## Étape 15 — Tests ✅
Tests unitaires StatValidator (4 tests).

---

## Lancer l'application

```bash
./gradlew :desktopApp:run
```

Connexion démo : `demo@local` / `demo`

## Prochaines améliorations (Phase 2)

- Version Android (réutilisation `commonMain`)
- Connexion Supabase production + tests RLS automatisés
- Realtime notifications
- Graphiques avancés
- Pagination serveur sur grands volumes
