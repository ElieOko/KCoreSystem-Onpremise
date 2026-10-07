-- Référentiel officiel du système éducatif de la RDC (EPST)
-- Loi-cadre n° 14/004 du 11 février 2014

ALTER TYPE school_type ADD VALUE IF NOT EXISTS 'MATERNELLE';
ALTER TYPE school_type ADD VALUE IF NOT EXISTS 'MATERNELLE_ET_PRIMAIRE';
ALTER TYPE school_type ADD VALUE IF NOT EXISTS 'COMPLET';

ALTER TYPE teacher_branch ADD VALUE IF NOT EXISTS 'MATERNELLE';

CREATE UNIQUE INDEX IF NOT EXISTS primary_class_configs_name_key
    ON primary_class_configs (name);

CREATE TABLE IF NOT EXISTS secondary_class_configs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            TEXT NOT NULL UNIQUE,
    short_name      TEXT NOT NULL,
    cycle           TEXT NOT NULL,
    order_index     INT NOT NULL DEFAULT 0,
    typical_age     INT,
    certification_exam_code TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

UPDATE primary_class_configs SET name = '1ère année primaire', order_index = 4, school_type_filter = 'PRIMAIRE' WHERE name = '1ère année';
UPDATE primary_class_configs SET name = '2ème année primaire', order_index = 5, school_type_filter = 'PRIMAIRE' WHERE name = '2ème année';
UPDATE primary_class_configs SET name = '3ème année primaire', order_index = 6, school_type_filter = 'PRIMAIRE' WHERE name = '3ème année';
UPDATE primary_class_configs SET name = '4ème année primaire', order_index = 7, school_type_filter = 'PRIMAIRE' WHERE name = '4ème année';
UPDATE primary_class_configs SET name = '5ème année primaire', order_index = 8, school_type_filter = 'PRIMAIRE' WHERE name = '5ème année';
UPDATE primary_class_configs SET name = '6ème année primaire', order_index = 9, school_type_filter = 'PRIMAIRE' WHERE name = '6ème année';

INSERT INTO primary_class_configs (name, order_index, school_type_filter) VALUES
    ('1ère maternelle', 1, 'MATERNELLE'),
    ('2ème maternelle', 2, 'MATERNELLE'),
    ('3ème maternelle', 3, 'MATERNELLE'),
    ('1ère année primaire', 4, 'PRIMAIRE'),
    ('2ème année primaire', 5, 'PRIMAIRE'),
    ('3ème année primaire', 6, 'PRIMAIRE'),
    ('4ème année primaire', 7, 'PRIMAIRE'),
    ('5ème année primaire', 8, 'PRIMAIRE'),
    ('6ème année primaire', 9, 'PRIMAIRE')
ON CONFLICT (name) DO UPDATE
    SET order_index = EXCLUDED.order_index,
        school_type_filter = EXCLUDED.school_type_filter,
        is_active = TRUE,
        updated_at = NOW();

INSERT INTO secondary_class_configs (name, short_name, cycle, order_index, typical_age, certification_exam_code) VALUES
    ('7ème année (1ère secondaire)', '7ème', 'TRONC_COMMUN', 10, 12, NULL),
    ('8ème année (2ème secondaire)', '8ème', 'TRONC_COMMUN', 11, 13, 'TENASOSP'),
    ('1ère des Humanités', '1ère H', 'HUMANITES', 12, 14, NULL),
    ('2ème des Humanités', '2ème H', 'HUMANITES', 13, 15, NULL),
    ('3ème des Humanités', '3ème H', 'HUMANITES', 14, 16, NULL),
    ('4ème des Humanités', '4ème H', 'HUMANITES', 15, 17, 'EXETAT')
ON CONFLICT (name) DO UPDATE
    SET short_name = EXCLUDED.short_name,
        cycle = EXCLUDED.cycle,
        order_index = EXCLUDED.order_index,
        typical_age = EXCLUDED.typical_age,
        certification_exam_code = EXCLUDED.certification_exam_code,
        is_active = TRUE,
        updated_at = NOW();

UPDATE secondary_sections SET name = 'Humanités Générales', code = 'HG' WHERE code = 'EG';
UPDATE secondary_sections SET name = 'Humanités Techniques', code = 'HT' WHERE code = 'ET';
UPDATE secondary_sections SET name = 'Humanités Professionnelles', code = 'HPR' WHERE code = 'EP';

INSERT INTO secondary_sections (name, code, order_index) VALUES
    ('Tronc commun', 'TC', 1),
    ('Humanités Générales', 'HG', 2),
    ('Humanités Pédagogiques', 'HP', 3),
    ('Humanités Techniques', 'HT', 4),
    ('Humanités Professionnelles', 'HPR', 5)
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name,
        order_index = EXCLUDED.order_index,
        is_active = TRUE,
        updated_at = NOW();

INSERT INTO secondary_options (section_id, name, code, order_index)
SELECT s.id, o.name, o.code, o.ord
FROM secondary_sections s
JOIN (VALUES
    ('HG', 'Latin-Philosophie', 'LP', 1),
    ('HG', 'Mathématiques-Physique', 'MP', 2),
    ('HG', 'Chimie-Biologie', 'CB', 3),
    ('HG', 'Latin-Mathématiques', 'LM', 4),
    ('HP', 'Pédagogie Générale', 'PG', 1),
    ('HP', 'Pédagogie Maternelle', 'PM', 2),
    ('HT', 'Commercial et Gestion', 'CG', 1),
    ('HT', 'Sociale', 'SOC', 2),
    ('HT', 'Électricité', 'EL', 3),
    ('HT', 'Mécanique Générale', 'MG', 4),
    ('HT', 'Construction', 'CONS', 5),
    ('HT', 'Coupe et Couture', 'CC', 6),
    ('HT', 'Agriculture', 'AG', 7),
    ('HT', 'Hôtellerie et Restauration', 'HR', 8),
    ('HPR', 'Menuiserie', 'MEN', 1),
    ('HPR', 'Maçonnerie', 'MAC', 2),
    ('HPR', 'Coupe et Couture professionnelle', 'CCP', 3)
) AS o(section_code, name, code, ord) ON o.section_code = s.code
ON CONFLICT (section_id, code) DO UPDATE
    SET name = EXCLUDED.name,
        order_index = EXCLUDED.order_index,
        is_active = TRUE,
        updated_at = NOW();

INSERT INTO certification_exams (name, code) VALUES
    ('TENAFEP', 'TENAFEP'),
    ('TENASOSP', 'TENASOSP'),
    ('EXETAT', 'EXETAT')
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name,
        is_active = TRUE,
        updated_at = NOW();

UPDATE certification_exams SET name = 'EXETAT' WHERE name = 'Examen d''État';
