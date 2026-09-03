-- =============================================================================
-- KCoreSystem — Vues d'agrégation pour centralisation
-- =============================================================================

-- =============================================================================
-- VUE : Statistiques élèves primaires par sous-division
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_primary_statistics AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    pcs.class_name,
    pcs.class_order,
    SUM(pcs.boys_count)             AS total_boys,
    SUM(pcs.girls_count)            AS total_girls,
    SUM(pcs.total_count)            AS total_students,
    COUNT(DISTINCT s.school_id)     AS school_count
FROM primary_class_statistics pcs
JOIN submissions sub ON sub.id = pcs.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = pcs.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = pcs.school_year_id
GROUP BY sd.id, sd.name, sd.province_id, sy.id, sy.name, pcs.class_name, pcs.class_order
ORDER BY sd.name, pcs.class_order;

-- =============================================================================
-- VUE : Statistiques élèves secondaires par sous-division
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_secondary_statistics AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    ss.name                         AS section_name,
    so.name                         AS option_name,
    sss.class_name,
    SUM(sss.boys_count)             AS total_boys,
    SUM(sss.girls_count)            AS total_girls,
    SUM(sss.total_count)            AS total_students,
    COUNT(DISTINCT sss.school_id)   AS school_count
FROM secondary_student_statistics sss
JOIN submissions sub ON sub.id = sss.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = sss.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = sss.school_year_id
JOIN secondary_sections ss ON ss.id = sss.section_id
LEFT JOIN secondary_options so ON so.id = sss.option_id
GROUP BY sd.id, sd.name, sd.province_id, sy.id, sy.name, ss.name, so.name, sss.class_name
ORDER BY sd.name, ss.name, so.name;

-- =============================================================================
-- VUE : Statistiques enseignants par sous-division
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_teacher_statistics AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    tsd.education_level,
    tsd.branch,
    SUM(tsd.men_count)              AS total_men,
    SUM(tsd.women_count)            AS total_women,
    SUM(tsd.total_count)            AS total_teachers,
    COUNT(DISTINCT ts.school_id)      AS school_count
FROM teacher_statistics_details tsd
JOIN teacher_statistics ts ON ts.id = tsd.teacher_statistics_id
JOIN submissions sub ON sub.id = ts.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = ts.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = ts.school_year_id
GROUP BY sd.id, sd.name, sd.province_id, sy.id, sy.name, tsd.education_level, tsd.branch
ORDER BY sd.name, tsd.branch, tsd.education_level;

-- =============================================================================
-- VUE : Inscriptions début/fin d'année par école
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_enrollment_statistics AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    s.id                            AS school_id,
    s.name                          AS school_name,
    s.school_code,
    COALESCE(bye.total_beginning, 0) AS beginning_total,
    COALESCE(eye.total_end, 0)       AS end_total,
    COALESCE(bye.total_beginning, 0) - COALESCE(eye.total_end, 0) AS difference,
    CASE
        WHEN COALESCE(bye.total_beginning, 0) = 0 THEN NULL
        ELSE ROUND(
            (COALESCE(eye.total_end, 0)::NUMERIC / bye.total_beginning::NUMERIC) * 100, 2
        )
    END AS retention_rate,
    CASE
        WHEN COALESCE(bye.total_beginning, 0) = 0 THEN NULL
        ELSE ROUND(
            100 - (COALESCE(eye.total_end, 0)::NUMERIC / bye.total_beginning::NUMERIC) * 100, 2
        )
    END AS dropout_rate
FROM schools s
JOIN subdivisions sd ON sd.id = s.subdivision_id
CROSS JOIN school_years sy
LEFT JOIN (
    SELECT submission_id, school_id, school_year_id, SUM(total_count) AS total_beginning
    FROM beginning_year_enrollment
    GROUP BY submission_id, school_id, school_year_id
) bye ON bye.school_id = s.id AND bye.school_year_id = sy.id
LEFT JOIN (
    SELECT submission_id, school_id, school_year_id, SUM(total_count) AS total_end
    FROM end_year_enrollment
    GROUP BY submission_id, school_id, school_year_id
) eye ON eye.school_id = s.id AND eye.school_year_id = sy.id
    AND eye.submission_id = bye.submission_id
WHERE s.deleted_at IS NULL
  AND EXISTS (
      SELECT 1 FROM submissions sub
      WHERE sub.school_id = s.id
        AND sub.school_year_id = sy.id
        AND sub.status = 'VALIDE'
  );

-- =============================================================================
-- VUE : Résultats épreuves certificatives par sous-division
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_certification_results AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    s.id                            AS school_id,
    s.name                          AS school_name,
    ce.name                         AS exam_name,
    cer.class_name,
    cer.registered_count,
    cer.participants_count,
    cer.successes_count,
    cer.failures_count,
    cer.boys_succeeded,
    cer.girls_succeeded,
    CASE
        WHEN cer.participants_count = 0 THEN NULL
        ELSE ROUND(
            (cer.successes_count::NUMERIC / cer.participants_count::NUMERIC) * 100, 2
        )
    END AS success_rate,
    CASE
        WHEN cer.registered_count = 0 THEN NULL
        ELSE ROUND(
            (cer.participants_count::NUMERIC / cer.registered_count::NUMERIC) * 100, 2
        )
    END AS participation_rate
FROM certification_exam_results cer
JOIN submissions sub ON sub.id = cer.submission_id AND sub.status = 'VALIDE'
JOIN schools s ON s.id = cer.school_id AND s.deleted_at IS NULL
JOIN subdivisions sd ON sd.id = s.subdivision_id
JOIN school_years sy ON sy.id = cer.school_year_id
JOIN certification_exams ce ON ce.id = cer.exam_id
ORDER BY sd.name, s.name, ce.name;

-- =============================================================================
-- VUE : Dashboard KPIs par sous-division
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_dashboard AS
SELECT
    sd.id                           AS subdivision_id,
    sd.name                         AS subdivision_name,
    sd.province_id,
    sy.id                           AS school_year_id,
    sy.name                         AS school_year_name,
    COUNT(DISTINCT s.id)            AS total_schools,
    COUNT(DISTINCT CASE WHEN sub.status IS NOT NULL AND sub.status != 'BROUILLON' THEN s.id END) AS schools_submitted,
    COUNT(DISTINCT CASE WHEN sub.status = 'VALIDE' THEN s.id END) AS schools_validated,
    COUNT(DISTINCT CASE WHEN sub.status IN ('SOUMIS', 'EN_VERIFICATION') THEN s.id END) AS schools_pending,
    COUNT(DISTINCT CASE WHEN sub.status = 'REJETE' THEN s.id END) AS schools_rejected,
    COUNT(DISTINCT s.id) - COUNT(DISTINCT CASE WHEN sub.status IS NOT NULL AND sub.status != 'BROUILLON' THEN s.id END) AS schools_not_submitted,
    COALESCE(SUM(pcs.total_count), 0) AS total_students,
    COALESCE(SUM(tsd.total_count), 0) AS total_teachers
FROM subdivisions sd
CROSS JOIN school_years sy
LEFT JOIN schools s ON s.subdivision_id = sd.id AND s.deleted_at IS NULL AND s.is_active = TRUE
LEFT JOIN submissions sub ON sub.school_id = s.id AND sub.school_year_id = sy.id
LEFT JOIN primary_class_statistics pcs ON pcs.submission_id = sub.id AND sub.status = 'VALIDE'
LEFT JOIN teacher_statistics ts ON ts.submission_id = sub.id AND sub.status = 'VALIDE'
LEFT JOIN teacher_statistics_details tsd ON tsd.teacher_statistics_id = ts.id
GROUP BY sd.id, sd.name, sd.province_id, sy.id, sy.name;

-- =============================================================================
-- VUE : Résumé global élèves par sous-division (tous niveaux)
-- =============================================================================

CREATE OR REPLACE VIEW vw_subdivision_student_statistics AS
SELECT
    subdivision_id,
    subdivision_name,
    province_id,
    school_year_id,
    school_year_name,
    SUM(total_boys)     AS total_boys,
    SUM(total_girls)    AS total_girls,
    SUM(total_students) AS total_students
FROM (
    SELECT subdivision_id, subdivision_name, province_id, school_year_id, school_year_name,
           total_boys, total_girls, total_students
    FROM vw_subdivision_primary_statistics
    UNION ALL
    SELECT subdivision_id, subdivision_name, province_id, school_year_id, school_year_name,
           total_boys, total_girls, total_students
    FROM vw_subdivision_secondary_statistics
) combined
GROUP BY subdivision_id, subdivision_name, province_id, school_year_id, school_year_name;

-- =============================================================================
-- GRANT SELECT sur les vues (RLS hérité des tables sous-jacentes via security_invoker)
-- =============================================================================

-- Les vues utilisent les tables avec RLS ; l'accès est filtré par les policies existantes
-- Pour un accès direct aux vues, activer security_invoker (PostgreSQL 15+)

ALTER VIEW vw_subdivision_primary_statistics SET (security_invoker = on);
ALTER VIEW vw_subdivision_secondary_statistics SET (security_invoker = on);
ALTER VIEW vw_subdivision_teacher_statistics SET (security_invoker = on);
ALTER VIEW vw_subdivision_enrollment_statistics SET (security_invoker = on);
ALTER VIEW vw_subdivision_certification_results SET (security_invoker = on);
ALTER VIEW vw_subdivision_dashboard SET (security_invoker = on);
ALTER VIEW vw_subdivision_student_statistics SET (security_invoker = on);

GRANT SELECT ON vw_subdivision_primary_statistics TO authenticated;
GRANT SELECT ON vw_subdivision_secondary_statistics TO authenticated;
GRANT SELECT ON vw_subdivision_teacher_statistics TO authenticated;
GRANT SELECT ON vw_subdivision_enrollment_statistics TO authenticated;
GRANT SELECT ON vw_subdivision_certification_results TO authenticated;
GRANT SELECT ON vw_subdivision_dashboard TO authenticated;
GRANT SELECT ON vw_subdivision_student_statistics TO authenticated;
