-- =============================================================================
-- Recensement étendu : âge/sexe, ouvriers, personnel administratif
-- Fichier central sous-division
-- =============================================================================

CREATE TYPE admin_staff_function AS ENUM (
    'DIRECTEUR',
    'DIRECTEUR_ADJOINT',
    'SURNUMERAIRE',
    'PREFET',
    'SECRETAIRE',
    'DIRECTEUR_DES_ETUDES',
    'CONSEILLER_PEDAGOGIQUE',
    'DIRECTEUR_DE_DISCIPLINE',
    'CONSEILLER_ORIENTATION'
);

CREATE TABLE student_age_sex_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    class_name      TEXT NOT NULL,
    age             INT NOT NULL CHECK (age >= 5 AND age <= 25),
    boys_count      INT NOT NULL DEFAULT 0 CHECK (boys_count >= 0),
    girls_count     INT NOT NULL DEFAULT 0 CHECK (girls_count >= 0),
    total_count     INT GENERATED ALWAYS AS (boys_count + girls_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id, class_name, age)
);

CREATE TABLE worker_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    education_level education_level NOT NULL,
    men_count       INT NOT NULL DEFAULT 0 CHECK (men_count >= 0),
    women_count     INT NOT NULL DEFAULT 0 CHECK (women_count >= 0),
    total_count     INT GENERATED ALWAYS AS (men_count + women_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id, education_level)
);

CREATE TABLE admin_staff_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    school_id       UUID NOT NULL REFERENCES schools(id) ON DELETE RESTRICT,
    school_year_id  UUID NOT NULL REFERENCES school_years(id) ON DELETE RESTRICT,
    function_name   admin_staff_function NOT NULL,
    education_level education_level NOT NULL,
    men_count       INT NOT NULL DEFAULT 0 CHECK (men_count >= 0),
    women_count     INT NOT NULL DEFAULT 0 CHECK (women_count >= 0),
    total_count     INT GENERATED ALWAYS AS (men_count + women_count) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (submission_id, function_name, education_level)
);

CREATE INDEX idx_age_sex_submission ON student_age_sex_statistics(submission_id);
CREATE INDEX idx_worker_stats_submission ON worker_statistics(submission_id);
CREATE INDEX idx_admin_staff_submission ON admin_staff_statistics(submission_id);

CREATE OR REPLACE VIEW vw_subdivision_age_sex_statistics AS
SELECT
    sd.id AS subdivision_id,
    sd.name AS subdivision_name,
    sy.id AS school_year_id,
    sy.name AS school_year_name,
    sax.class_name,
    sax.age,
    SUM(sax.boys_count) AS total_boys,
    SUM(sax.girls_count) AS total_girls,
    SUM(sax.total_count) AS total_students
FROM student_age_sex_statistics sax
JOIN submissions sub ON sub.id = sax.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = sax.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = sax.school_year_id
GROUP BY sd.id, sd.name, sy.id, sy.name, sax.class_name, sax.age
ORDER BY sd.name, sax.class_name, sax.age;

CREATE OR REPLACE VIEW vw_subdivision_worker_statistics AS
SELECT
    sd.id AS subdivision_id,
    sd.name AS subdivision_name,
    sy.id AS school_year_id,
    ws.education_level,
    SUM(ws.men_count) AS total_men,
    SUM(ws.women_count) AS total_women,
    SUM(ws.total_count) AS total_workers
FROM worker_statistics ws
JOIN submissions sub ON sub.id = ws.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = ws.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = ws.school_year_id
GROUP BY sd.id, sd.name, sy.id, ws.education_level;

CREATE OR REPLACE VIEW vw_subdivision_admin_staff_statistics AS
SELECT
    sd.id AS subdivision_id,
    sd.name AS subdivision_name,
    sy.id AS school_year_id,
    ast.function_name,
    ast.education_level,
    SUM(ast.men_count) AS total_men,
    SUM(ast.women_count) AS total_women,
    SUM(ast.total_count) AS total_staff
FROM admin_staff_statistics ast
JOIN submissions sub ON sub.id = ast.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = ast.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = ast.school_year_id
GROUP BY sd.id, sd.name, sy.id, ast.function_name, ast.education_level;
