-- =============================================================================
-- KCoreSystem — Schéma initial PostgreSQL
-- Système de collecte et centralisation des statistiques scolaires
-- =============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- ENUMS
-- =============================================================================

CREATE TYPE user_role AS ENUM (
    'SUPER_ADMIN',
    'ADMIN_PROVINCIAL',
    'ADMIN_SOUS_DIVISION',
    'ECOLE'
);

CREATE TYPE submission_status AS ENUM (
    'BROUILLON',
    'SOUMIS',
    'EN_VERIFICATION',
    'VALIDE',
    'REJETE',
    'CORRECTION_DEMANDEE'
);

CREATE TYPE school_type AS ENUM (
    'PRIMAIRE',
    'SECONDAIRE',
    'PRIMAIRE_ET_SECONDAIRE'
);

CREATE TYPE school_ownership AS ENUM (
    'PUBLIQUE',
    'PRIVEE'
);

CREATE TYPE school_convention AS ENUM (
    'CONVENTIONNEE',
    'NON_CONVENTIONNEE'
);

CREATE TYPE school_environment AS ENUM (
    'URBAIN',
    'RURAL'
);

CREATE TYPE education_level AS ENUM (
    'D4', 'D6', 'GRADUE', 'LICENCE', 'MASTER', 'DOCTORAT', 'AUTRES'
);

CREATE TYPE teacher_branch AS ENUM (
    'PRIMAIRE',
    'SECONDAIRE_GENERAL',
    'SECONDAIRE_TECHNIQUE',
    'SECONDAIRE_PROFESSIONNEL',
    'AUTRES'
);

CREATE TYPE audit_action AS ENUM (
    'CREATE', 'UPDATE', 'DELETE', 'SUBMIT', 'VALIDATE', 'REJECT'
);

CREATE TYPE notification_type AS ENUM (
    'DATA_SUBMITTED',
    'NEW_SUBMISSION',
    'DATA_VALIDATED',
    'DATA_REJECTED',
    'CORRECTION_REQUESTED',
    'SYNC_FAILED',
    'SYNC_SUCCESS',
    'SUBMISSION_DEADLINE'
);

-- =============================================================================
-- ORGANISATION
-- =============================================================================

CREATE TABLE provinces (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    code        TEXT NOT NULL UNIQUE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  UUID REFERENCES auth.users(id),
    updated_by  UUID REFERENCES auth.users(id)
);

CREATE TABLE education_provinces (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id UUID NOT NULL REFERENCES provinces(id) ON DELETE RESTRICT,
    name        TEXT NOT NULL,
    code        TEXT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (province_id, code)
);

CREATE TABLE subdivisions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    province_id           UUID NOT NULL REFERENCES provinces(id) ON DELETE RESTRICT,
    education_province_id UUID REFERENCES education_provinces(id) ON DELETE SET NULL,
    name                  TEXT NOT NULL,
    code                  TEXT NOT NULL UNIQUE,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by            UUID REFERENCES auth.users(id),
    updated_by            UUID REFERENCES auth.users(id)
);

CREATE TABLE schools (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subdivision_id      UUID NOT NULL REFERENCES subdivisions(id) ON DELETE RESTRICT,
    name                TEXT NOT NULL,
    school_code         TEXT NOT NULL UNIQUE,
    dinacope_id         TEXT,
    address             TEXT,
    city                TEXT,
    commune_territory   TEXT,
    sector_quarter      TEXT,
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    principal_name      TEXT,
    principal_phone     TEXT,
    management_regime   TEXT,
    school_type         school_type NOT NULL DEFAULT 'PRIMAIRE',
    ownership           school_ownership NOT NULL DEFAULT 'PUBLIQUE',
    convention          school_convention,
    environment         school_environment,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID REFERENCES auth.users(id),
    updated_by          UUID REFERENCES auth.users(id)
);

CREATE TABLE school_years (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT school_years_dates_check CHECK (end_date > start_date)
);

-- =============================================================================
-- UTILISATEURS
-- =============================================================================

CREATE TABLE profiles (
    id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name       TEXT NOT NULL,
    email           TEXT NOT NULL,
    phone           TEXT,
    role            user_role NOT NULL DEFAULT 'ECOLE',
    school_id       UUID REFERENCES schools(id) ON DELETE SET NULL,
    subdivision_id  UUID REFERENCES subdivisions(id) ON DELETE SET NULL,
    province_id     UUID REFERENCES provinces(id) ON DELETE SET NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT profiles_role_scope_check CHECK (
        (role = 'ECOLE' AND school_id IS NOT NULL) OR
        (role = 'ADMIN_SOUS_DIVISION' AND subdivision_id IS NOT NULL) OR
        (role = 'ADMIN_PROVINCIAL' AND province_id IS NOT NULL) OR
        (role = 'SUPER_ADMIN')
    )
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================================================
-- CONFIGURATION DYNAMIQUE
-- =============================================================================

CREATE TABLE primary_class_configs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT NOT NULL,
    order_index         INT NOT NULL DEFAULT 0,
    school_type_filter  school_type,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE secondary_sections (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    code        TEXT NOT NULL UNIQUE,
    order_index INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE secondary_options (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id  UUID NOT NULL REFERENCES secondary_sections(id) ON DELETE CASCADE,
    name        TEXT NOT NULL,
    code        TEXT NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (section_id, code)
);

CREATE TABLE certification_exams (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    code        TEXT NOT NULL UNIQUE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- SOUMISSIONS (workflow central)
-- =============================================================================

CREATE TABLE submissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    status          submission_status NOT NULL DEFAULT 'BROUILLON',
    submitted_at    TIMESTAMPTZ,
    validated_at    TIMESTAMPTZ,
    validated_by    UUID REFERENCES profiles(id),
    rejected_at     TIMESTAMPTZ,
    rejected_by     UUID REFERENCES profiles(id),
    rejection_reason TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES auth.users(id),
    updated_by      UUID REFERENCES auth.users(id),
    UNIQUE (school_id, school_year_id)
);

CREATE TABLE submission_comments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    author_id       UUID NOT NULL REFERENCES profiles(id),
    content         TEXT NOT NULL,
    is_rejection    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- STATISTIQUES ÉLÈVES — PRIMAIRE
-- =============================================================================

CREATE TABLE primary_class_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    class_name      TEXT NOT NULL,
    class_order     INT NOT NULL DEFAULT 0,
    boys_count      INT NOT NULL DEFAULT 0 CHECK (boys_count >= 0),
    girls_count     INT NOT NULL DEFAULT 0 CHECK (girls_count >= 0),
    total_count     INT GENERATED ALWAYS AS (boys_count + girls_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES auth.users(id),
    updated_by      UUID REFERENCES auth.users(id),
    UNIQUE (submission_id, class_name)
);

-- =============================================================================
-- STATISTIQUES ÉLÈVES — SECONDAIRE
-- =============================================================================

CREATE TABLE secondary_student_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    section_id      UUID NOT NULL REFERENCES secondary_sections(id) ON DELETE RESTRICT,
    option_id       UUID REFERENCES secondary_options(id) ON DELETE SET NULL,
    class_name      TEXT NOT NULL,
    boys_count      INT NOT NULL DEFAULT 0 CHECK (boys_count >= 0),
    girls_count     INT NOT NULL DEFAULT 0 CHECK (girls_count >= 0),
    total_count     INT GENERATED ALWAYS AS (boys_count + girls_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES auth.users(id),
    updated_by      UUID REFERENCES auth.users(id)
);

-- =============================================================================
-- ENSEIGNANTS
-- =============================================================================

CREATE TABLE teacher_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id)
);

CREATE TABLE teacher_statistics_details (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_statistics_id   UUID NOT NULL REFERENCES teacher_statistics(id) ON DELETE CASCADE,
    education_level         education_level NOT NULL,
    branch                  teacher_branch NOT NULL,
    men_count               INT NOT NULL DEFAULT 0 CHECK (men_count >= 0),
    women_count             INT NOT NULL DEFAULT 0 CHECK (women_count >= 0),
    total_count             INT GENERATED ALWAYS AS (men_count + women_count) STORED,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (teacher_statistics_id, education_level, branch)
);

-- =============================================================================
-- INSCRIPTIONS DÉBUT / FIN D'ANNÉE
-- =============================================================================

CREATE TABLE beginning_year_enrollment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    class_name      TEXT NOT NULL,
    boys_count      INT NOT NULL DEFAULT 0 CHECK (boys_count >= 0),
    girls_count     INT NOT NULL DEFAULT 0 CHECK (girls_count >= 0),
    total_count     INT GENERATED ALWAYS AS (boys_count + girls_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id, class_name)
);

CREATE TABLE end_year_enrollment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    class_name      TEXT NOT NULL,
    boys_count      INT NOT NULL DEFAULT 0 CHECK (boys_count >= 0),
    girls_count     INT NOT NULL DEFAULT 0 CHECK (girls_count >= 0),
    total_count     INT GENERATED ALWAYS AS (boys_count + girls_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id, class_name)
);

-- =============================================================================
-- ÉPREUVES CERTIFICATIVES
-- =============================================================================

CREATE TABLE certification_exam_results (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id       UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id           UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id      UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    exam_id             UUID NOT NULL REFERENCES certification_exams(id) ON DELETE RESTRICT,
    class_name          TEXT NOT NULL,
    registered_count    INT NOT NULL DEFAULT 0 CHECK (registered_count >= 0),
    participants_count  INT NOT NULL DEFAULT 0 CHECK (participants_count >= 0),
    successes_count     INT NOT NULL DEFAULT 0 CHECK (successes_count >= 0),
    failures_count      INT GENERATED ALWAYS AS (
        GREATEST(participants_count - successes_count, 0)
    ) STORED,
    boys_succeeded      INT NOT NULL DEFAULT 0 CHECK (boys_succeeded >= 0),
    girls_succeeded     INT NOT NULL DEFAULT 0 CHECK (girls_succeeded >= 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT cert_participants_check CHECK (participants_count <= registered_count),
    CONSTRAINT cert_successes_check CHECK (successes_count <= participants_count),
    UNIQUE (submission_id, exam_id, class_name)
);

-- =============================================================================
-- NOTIFICATIONS
-- =============================================================================

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    type            notification_type NOT NULL,
    title           TEXT NOT NULL,
    message         TEXT NOT NULL,
    reference_id    UUID,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- AUDIT
-- =============================================================================

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES profiles(id),
    action      audit_action NOT NULL,
    table_name  TEXT NOT NULL,
    record_id   UUID,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  INET,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- INDEX
-- =============================================================================

CREATE INDEX idx_schools_subdivision ON schools(subdivision_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_schools_code ON schools(school_code);
CREATE INDEX idx_subdivisions_province ON subdivisions(province_id);
CREATE INDEX idx_profiles_role ON profiles(role);
CREATE INDEX idx_profiles_school ON profiles(school_id) WHERE school_id IS NOT NULL;
CREATE INDEX idx_profiles_subdivision ON profiles(subdivision_id) WHERE subdivision_id IS NOT NULL;
CREATE INDEX idx_profiles_province ON profiles(province_id) WHERE province_id IS NOT NULL;
CREATE INDEX idx_submissions_school ON submissions(school_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_submissions_year ON submissions(school_year_id);
CREATE INDEX idx_primary_stats_submission ON primary_class_statistics(submission_id);
CREATE INDEX idx_primary_stats_school ON primary_class_statistics(school_id);
CREATE INDEX idx_secondary_stats_submission ON secondary_student_statistics(submission_id);
CREATE INDEX idx_teacher_details_stats ON teacher_statistics_details(teacher_statistics_id);
CREATE INDEX idx_beginning_enrollment_submission ON beginning_year_enrollment(submission_id);
CREATE INDEX idx_end_enrollment_submission ON end_year_enrollment(submission_id);
CREATE INDEX idx_cert_results_submission ON certification_exam_results(submission_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_audit_logs_table ON audit_logs(table_name, record_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);

-- =============================================================================
-- TRIGGERS updated_at
-- =============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_provinces_updated_at BEFORE UPDATE ON provinces
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_subdivisions_updated_at BEFORE UPDATE ON subdivisions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_schools_updated_at BEFORE UPDATE ON schools
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_school_years_updated_at BEFORE UPDATE ON school_years
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_profiles_updated_at BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_submissions_updated_at BEFORE UPDATE ON submissions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_primary_stats_updated_at BEFORE UPDATE ON primary_class_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_secondary_stats_updated_at BEFORE UPDATE ON secondary_student_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- TRIGGER : création automatique du profil à l'inscription
-- =============================================================================

CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO profiles (id, full_name, email, role)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        NEW.email,
        COALESCE((NEW.raw_user_meta_data->>'role')::user_role, 'ECOLE')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION handle_new_user();
