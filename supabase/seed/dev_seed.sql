-- Données de développement (à exécuter après les migrations)
-- Remplacer les UUID par ceux de votre projet Supabase si nécessaire

INSERT INTO provinces (id, name, code) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Kinshasa', 'KIN')
ON CONFLICT DO NOTHING;

INSERT INTO subdivisions (id, province_id, name, code) VALUES
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Limete', 'LIM')
ON CONFLICT DO NOTHING;

INSERT INTO school_years (id, name, start_date, end_date, is_active) VALUES
    ('33333333-3333-3333-3333-333333333333', '2025-2026', '2025-09-01', '2026-06-30', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO primary_class_configs (name, order_index) VALUES
    ('1ère année', 1),
    ('2ème année', 2),
    ('3ème année', 3),
    ('4ème année', 4),
    ('5ème année', 5),
    ('6ème année', 6)
ON CONFLICT DO NOTHING;

INSERT INTO secondary_sections (name, code, order_index) VALUES
    ('Enseignement Général', 'EG', 1),
    ('Enseignement Technique', 'ET', 2),
    ('Enseignement Professionnel', 'EP', 3)
ON CONFLICT DO NOTHING;

INSERT INTO certification_exams (name, code) VALUES
    ('TENAFEP', 'TENAFEP'),
    ('Examen d''État', 'EXETAT')
ON CONFLICT DO NOTHING;
