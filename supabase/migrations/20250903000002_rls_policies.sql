-- =============================================================================
-- KCoreSystem — Row Level Security Policies
-- =============================================================================

-- =============================================================================
-- HELPER FUNCTIONS
-- =============================================================================

CREATE OR REPLACE FUNCTION auth_user_role()
RETURNS user_role AS $$
    SELECT role FROM profiles WHERE id = auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION auth_user_school_id()
RETURNS UUID AS $$
    SELECT school_id FROM profiles WHERE id = auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION auth_user_subdivision_id()
RETURNS UUID AS $$
    SELECT subdivision_id FROM profiles WHERE id = auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION auth_user_province_id()
RETURNS UUID AS $$
    SELECT province_id FROM profiles WHERE id = auth.uid()
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION school_belongs_to_user_subdivision(p_school_id UUID)
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM schools s
        WHERE s.id = p_school_id
          AND s.subdivision_id = auth_user_subdivision_id()
          AND s.deleted_at IS NULL
    )
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION school_belongs_to_user_province(p_school_id UUID)
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM schools s
        JOIN subdivisions sd ON sd.id = s.subdivision_id
        WHERE s.id = p_school_id
          AND sd.province_id = auth_user_province_id()
          AND s.deleted_at IS NULL
    )
$$ LANGUAGE sql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION submission_is_editable(p_submission_id UUID)
RETURNS BOOLEAN AS $$
    SELECT status IN ('BROUILLON', 'REJETE', 'CORRECTION_DEMANDEE')
    FROM submissions WHERE id = p_submission_id
$$ LANGUAGE sql SECURITY DEFINER STABLE;

-- =============================================================================
-- ENABLE RLS ON ALL SENSITIVE TABLES
-- =============================================================================

ALTER TABLE provinces ENABLE ROW LEVEL SECURITY;
ALTER TABLE education_provinces ENABLE ROW LEVEL SECURITY;
ALTER TABLE subdivisions ENABLE ROW LEVEL SECURITY;
ALTER TABLE schools ENABLE ROW LEVEL SECURITY;
ALTER TABLE school_years ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE primary_class_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE secondary_sections ENABLE ROW LEVEL SECURITY;
ALTER TABLE secondary_options ENABLE ROW LEVEL SECURITY;
ALTER TABLE certification_exams ENABLE ROW LEVEL SECURITY;
ALTER TABLE submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE submission_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE primary_class_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE secondary_student_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE teacher_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE teacher_statistics_details ENABLE ROW LEVEL SECURITY;
ALTER TABLE beginning_year_enrollment ENABLE ROW LEVEL SECURITY;
ALTER TABLE end_year_enrollment ENABLE ROW LEVEL SECURITY;
ALTER TABLE certification_exam_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- PROVINCES
-- =============================================================================

CREATE POLICY provinces_select ON provinces FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND id = auth_user_province_id())
    OR auth_user_role() IN ('ADMIN_SOUS_DIVISION', 'ECOLE')
);

CREATE POLICY provinces_all ON provinces FOR ALL TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN')
WITH CHECK (auth_user_role() = 'SUPER_ADMIN');

-- =============================================================================
-- SUBDIVISIONS
-- =============================================================================

CREATE POLICY subdivisions_select ON subdivisions FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND province_id = auth_user_province_id())
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND id = auth_user_subdivision_id())
    OR auth_user_role() = 'ECOLE'
);

CREATE POLICY subdivisions_insert ON subdivisions FOR INSERT TO authenticated
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL'));

CREATE POLICY subdivisions_update ON subdivisions FOR UPDATE TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL'));

CREATE POLICY subdivisions_delete ON subdivisions FOR DELETE TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN');

-- =============================================================================
-- SCHOOLS
-- =============================================================================

CREATE POLICY schools_select ON schools FOR SELECT TO authenticated
USING (
    deleted_at IS NULL AND (
        auth_user_role() = 'SUPER_ADMIN'
        OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(id))
        OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND subdivision_id = auth_user_subdivision_id())
        OR (auth_user_role() = 'ECOLE' AND id = auth_user_school_id())
    )
);

CREATE POLICY schools_insert ON schools FOR INSERT TO authenticated
WITH CHECK (
    auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION')
    AND (
        auth_user_role() = 'SUPER_ADMIN'
        OR subdivision_id = auth_user_subdivision_id()
    )
);

CREATE POLICY schools_update ON schools FOR UPDATE TO authenticated
USING (
    auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION')
    AND (
        auth_user_role() = 'SUPER_ADMIN'
        OR subdivision_id = auth_user_subdivision_id()
    )
)
WITH CHECK (
    auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION')
);

CREATE POLICY schools_delete ON schools FOR DELETE TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN');

-- =============================================================================
-- PROFILES
-- =============================================================================

CREATE POLICY profiles_select ON profiles FOR SELECT TO authenticated
USING (
    id = auth.uid()
    OR auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND province_id = auth_user_province_id())
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND subdivision_id = auth_user_subdivision_id())
);

CREATE POLICY profiles_update_own ON profiles FOR UPDATE TO authenticated
USING (id = auth.uid())
WITH CHECK (id = auth.uid());

CREATE POLICY profiles_admin_update ON profiles FOR UPDATE TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL', 'ADMIN_SOUS_DIVISION'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL', 'ADMIN_SOUS_DIVISION'));

-- =============================================================================
-- SCHOOL YEARS (lecture pour tous, écriture super admin)
-- =============================================================================

CREATE POLICY school_years_select ON school_years FOR SELECT TO authenticated
USING (TRUE);

CREATE POLICY school_years_admin ON school_years FOR ALL TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN')
WITH CHECK (auth_user_role() = 'SUPER_ADMIN');

-- =============================================================================
-- CONFIG TABLES (lecture tous, écriture admin)
-- =============================================================================

CREATE POLICY config_select ON primary_class_configs FOR SELECT TO authenticated USING (TRUE);
CREATE POLICY config_admin ON primary_class_configs FOR ALL TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'));

CREATE POLICY sections_select ON secondary_sections FOR SELECT TO authenticated USING (TRUE);
CREATE POLICY sections_admin ON secondary_sections FOR ALL TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'));

CREATE POLICY options_select ON secondary_options FOR SELECT TO authenticated USING (TRUE);
CREATE POLICY options_admin ON secondary_options FOR ALL TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'));

CREATE POLICY exams_select ON certification_exams FOR SELECT TO authenticated USING (TRUE);
CREATE POLICY exams_admin ON certification_exams FOR ALL TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'))
WITH CHECK (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION'));

-- =============================================================================
-- SUBMISSIONS
-- =============================================================================

CREATE POLICY submissions_select ON submissions FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY submissions_insert ON submissions FOR INSERT TO authenticated
WITH CHECK (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
);

CREATE POLICY submissions_update_school ON submissions FOR UPDATE TO authenticated
USING (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND status IN ('BROUILLON', 'REJETE', 'CORRECTION_DEMANDEE')
)
WITH CHECK (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
);

-- Admin status changes via Edge Functions (service role), not direct client update

-- =============================================================================
-- MACRO : policies statistiques liées aux soumissions
-- Pattern réutilisé pour toutes les tables de stats
-- =============================================================================

-- PRIMARY CLASS STATISTICS
CREATE POLICY primary_stats_select ON primary_class_statistics FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY primary_stats_insert ON primary_class_statistics FOR INSERT TO authenticated
WITH CHECK (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

CREATE POLICY primary_stats_update ON primary_class_statistics FOR UPDATE TO authenticated
USING (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
);

CREATE POLICY primary_stats_delete ON primary_class_statistics FOR DELETE TO authenticated
USING (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

-- SECONDARY STUDENT STATISTICS (même pattern)
CREATE POLICY secondary_stats_select ON secondary_student_statistics FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY secondary_stats_write ON secondary_student_statistics FOR ALL TO authenticated
USING (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

-- TEACHER STATISTICS
CREATE POLICY teacher_stats_select ON teacher_statistics FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY teacher_stats_write ON teacher_statistics FOR ALL TO authenticated
USING (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE'
    AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

CREATE POLICY teacher_details_select ON teacher_statistics_details FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM teacher_statistics ts
        WHERE ts.id = teacher_statistics_id
          AND (
              auth_user_role() = 'SUPER_ADMIN'
              OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(ts.school_id))
              OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(ts.school_id))
              OR (auth_user_role() = 'ECOLE' AND ts.school_id = auth_user_school_id())
          )
    )
);

CREATE POLICY teacher_details_write ON teacher_statistics_details FOR ALL TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM teacher_statistics ts
        WHERE ts.id = teacher_statistics_id
          AND auth_user_role() = 'ECOLE'
          AND ts.school_id = auth_user_school_id()
          AND submission_is_editable(ts.submission_id)
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1 FROM teacher_statistics ts
        WHERE ts.id = teacher_statistics_id
          AND auth_user_role() = 'ECOLE'
          AND ts.school_id = auth_user_school_id()
          AND submission_is_editable(ts.submission_id)
    )
);

-- BEGINNING / END YEAR ENROLLMENT (même pattern école)
CREATE POLICY beginning_enrollment_select ON beginning_year_enrollment FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY beginning_enrollment_write ON beginning_year_enrollment FOR ALL TO authenticated
USING (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

CREATE POLICY end_enrollment_select ON end_year_enrollment FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY end_enrollment_write ON end_year_enrollment FOR ALL TO authenticated
USING (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

-- CERTIFICATION EXAM RESULTS
CREATE POLICY cert_results_select ON certification_exam_results FOR SELECT TO authenticated
USING (
    auth_user_role() = 'SUPER_ADMIN'
    OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(school_id))
    OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(school_id))
    OR (auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id())
);

CREATE POLICY cert_results_write ON certification_exam_results FOR ALL TO authenticated
USING (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
)
WITH CHECK (
    auth_user_role() = 'ECOLE' AND school_id = auth_user_school_id()
    AND submission_is_editable(submission_id)
);

-- =============================================================================
-- SUBMISSION COMMENTS
-- =============================================================================

CREATE POLICY submission_comments_select ON submission_comments FOR SELECT TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM submissions sub
        WHERE sub.id = submission_id
          AND (
              auth_user_role() = 'SUPER_ADMIN'
              OR (auth_user_role() = 'ADMIN_PROVINCIAL' AND school_belongs_to_user_province(sub.school_id))
              OR (auth_user_role() = 'ADMIN_SOUS_DIVISION' AND school_belongs_to_user_subdivision(sub.school_id))
              OR (auth_user_role() = 'ECOLE' AND sub.school_id = auth_user_school_id())
          )
    )
);

CREATE POLICY submission_comments_insert ON submission_comments FOR INSERT TO authenticated
WITH CHECK (
    author_id = auth.uid()
    AND (
        auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_SOUS_DIVISION', 'ADMIN_PROVINCIAL')
        OR auth_user_role() = 'ECOLE'
    )
);

-- =============================================================================
-- NOTIFICATIONS
-- =============================================================================

CREATE POLICY notifications_select ON notifications FOR SELECT TO authenticated
USING (user_id = auth.uid());

CREATE POLICY notifications_update ON notifications FOR UPDATE TO authenticated
USING (user_id = auth.uid())
WITH CHECK (user_id = auth.uid());

-- Insert via Edge Functions / triggers (service role)

-- =============================================================================
-- AUDIT LOGS (lecture admin uniquement)
-- =============================================================================

CREATE POLICY audit_logs_select ON audit_logs FOR SELECT TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL', 'ADMIN_SOUS_DIVISION'));

-- Insert via triggers / Edge Functions (service role)

-- =============================================================================
-- EDUCATION PROVINCES
-- =============================================================================

CREATE POLICY education_provinces_select ON education_provinces FOR SELECT TO authenticated
USING (TRUE);

CREATE POLICY education_provinces_admin ON education_provinces FOR ALL TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN')
WITH CHECK (auth_user_role() = 'SUPER_ADMIN');

-- =============================================================================
-- ROLES / PERMISSIONS (lecture admin)
-- =============================================================================

CREATE POLICY roles_select ON roles FOR SELECT TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL', 'ADMIN_SOUS_DIVISION'));

CREATE POLICY permissions_select ON permissions FOR SELECT TO authenticated
USING (auth_user_role() IN ('SUPER_ADMIN', 'ADMIN_PROVINCIAL', 'ADMIN_SOUS_DIVISION'));

CREATE POLICY role_permissions_select ON role_permissions FOR SELECT TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN');

CREATE POLICY user_roles_select ON user_roles FOR SELECT TO authenticated
USING (auth_user_role() = 'SUPER_ADMIN' OR user_id = auth.uid());
