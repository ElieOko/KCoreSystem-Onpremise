# Supabase — Configuration et déploiement

## Prérequis

- Compte [Supabase](https://supabase.com)
- [Supabase CLI](https://supabase.com/docs/guides/cli) installé

## Déploiement des migrations

```bash
# Initialiser (si nouveau projet)
supabase init

# Lier au projet cloud
supabase link --project-ref <your-project-ref>

# Appliquer les migrations
supabase db push

# Ou en local pour développement
supabase start
supabase db reset
```

## Ordre des migrations

1. `20250903000001_initial_schema.sql` — Tables, enums, index, triggers
2. `20250903000002_rls_policies.sql` — Row Level Security
3. `20250903000003_aggregation_views.sql` — Vues de centralisation

## Configuration application

Copier `local.properties.example` vers `local.properties` :

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
```

> **Ne jamais** commiter `local.properties` ni utiliser la Service Role Key côté client.

## Activer Realtime

Dans le Dashboard Supabase → Database → Replication, activer Realtime pour :

- `submissions`
- `notifications`
- `submission_comments`

## Edge Functions (Phase ultérieure)

Les opérations sensibles (validation, rejet) passeront par des Edge Functions utilisant la Service Role Key côté serveur uniquement.

```
supabase/functions/
├── validate-submission/
├── reject-submission/
├── calculate-statistics/
└── send-notification/
```

## Tests RLS

Créer 4 utilisateurs test via le Dashboard :

| Email | Rôle | Scope |
|-------|------|-------|
| super@test.local | SUPER_ADMIN | Tout |
| prov@test.local | ADMIN_PROVINCIAL | Province X |
| sd@test.local | ADMIN_SOUS_DIVISION | Sous-Division Y |
| ecole@test.local | ECOLE | École Z |

Vérifier que chaque utilisateur ne voit que ses données autorisées.
