-- Données de développement (à exécuter après les migrations)
-- Référentiel officiel EPST / RDC

INSERT INTO provinces (id, name, code) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Kinshasa', 'KIN')
ON CONFLICT DO NOTHING;

INSERT INTO subdivisions (id, province_id, name, code) VALUES
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Limete', 'LIM')
ON CONFLICT DO NOTHING;

INSERT INTO school_years (id, name, start_date, end_date, is_active) VALUES
    ('33333333-3333-3333-3333-333333333333', '2025-2026', '2025-09-01', '2026-06-30', TRUE)
ON CONFLICT DO NOTHING;

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
        school_type_filter = EXCLUDED.school_type_filter;

INSERT INTO secondary_sections (name, code, order_index) VALUES
    ('Tronc commun', 'TC', 1),
    ('Humanités Générales', 'HG', 2),
    ('Humanités Pédagogiques', 'HP', 3),
    ('Humanités Techniques', 'HT', 4),
    ('Humanités Professionnelles', 'HPR', 5)
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name,
        order_index = EXCLUDED.order_index;

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
        order_index = EXCLUDED.order_index;

INSERT INTO certification_exams (name, code) VALUES
    ('TENAFEP', 'TENAFEP'),
    ('TENASOSP', 'TENASOSP'),
    ('EXETAT', 'EXETAT')
ON CONFLICT (code) DO UPDATE
    SET name = EXCLUDED.name;
