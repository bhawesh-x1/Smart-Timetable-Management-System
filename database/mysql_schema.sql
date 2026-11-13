-- ============================================================================
-- SMART CAMPUS TIMETABLE MANAGEMENT SYSTEM (v1.0.0 • College Edition)
-- Poornima Institute of Engineering & Technology (PIET), Jaipur
-- Department of Computer Engineering | Academic Session 2026-2027 (Sem 1 - Sem 8)
-- Normalized MySQL 8.x Relational DDL Schema (100% Aligned with JPA Entities)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS smart_campus_timetable
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_campus_timetable;

-- 1. DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_id      INT          NOT NULL,
    code            VARCHAR(20)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    hod_name        VARCHAR(120) NULL,
    building_block  VARCHAR(60)  DEFAULT 'Block A & C',
    dataset_mode    VARCHAR(20)  NOT NULL DEFAULT 'BOTH',
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_department_code UNIQUE (code)
) ENGINE=InnoDB;

-- 2. SEMESTERS (Sem 1 through Sem 8)
CREATE TABLE IF NOT EXISTS semesters (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_number  INT          NOT NULL,
    semester_code    VARCHAR(20)  NOT NULL,
    semester_name    VARCHAR(80)  NOT NULL,
    academic_term    VARCHAR(20)  NOT NULL, -- ODD (Jul-Dec) or EVEN (Jan-Jun)
    academic_year    VARCHAR(20)  NOT NULL DEFAULT '2026-2027',
    total_credits    INT          NOT NULL DEFAULT 22,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_semester_number CHECK (semester_number BETWEEN 1 AND 8),
    CONSTRAINT uq_semester_number UNIQUE (semester_number),
    CONSTRAINT uq_semester_code UNIQUE (semester_code)
) ENGINE=InnoDB;

-- 3. COURSES
CREATE TABLE IF NOT EXISTS courses (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_id       INT          NOT NULL,
    code             VARCHAR(30)  NOT NULL,
    name             VARCHAR(150) NOT NULL,
    department_code  VARCHAR(20)  NOT NULL,
    department_name  VARCHAR(150) NOT NULL,
    duration_years   INT          NOT NULL DEFAULT 4,
    total_semesters  INT          NOT NULL DEFAULT 8,
    total_credits    INT          NOT NULL DEFAULT 160,
    coordinator      VARCHAR(120) NULL,
    CONSTRAINT uq_course_code UNIQUE (code),
    CONSTRAINT fk_course_dept_code FOREIGN KEY (department_code)
        REFERENCES departments(code) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 4. CLASS SECTIONS (Sem 1 to Sem 8 Sections & Sub-batches)
CREATE TABLE IF NOT EXISTS class_sections (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_code     VARCHAR(30)  NOT NULL,
    section_name     VARCHAR(60)  NOT NULL,
    semester         VARCHAR(20)  NOT NULL DEFAULT 'Sem 3',
    course_name      VARCHAR(120) NOT NULL,
    department_code  VARCHAR(20)  NOT NULL,
    sub_batches      VARCHAR(60)  NOT NULL DEFAULT 'A1, A2, A3',
    primary_room     VARCHAR(40)  NOT NULL DEFAULT 'A-201',
    student_strength INT          NOT NULL DEFAULT 60,
    academic_year    VARCHAR(20)  NOT NULL DEFAULT '2026-2027',
    CONSTRAINT uq_section_code UNIQUE (section_code),
    CONSTRAINT fk_section_dept_code FOREIGN KEY (department_code)
        REFERENCES departments(code) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_section_semester_code FOREIGN KEY (semester)
        REFERENCES semesters(semester_code) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_sections_semester (semester)
) ENGINE=InnoDB;

-- 5. STUDENTS
CREATE TABLE IF NOT EXISTS students (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_id       INT          NOT NULL,
    enrollment_no    VARCHAR(30)  NOT NULL,
    full_name        VARCHAR(120) NOT NULL,
    email            VARCHAR(150) NOT NULL,
    phone            VARCHAR(30)  NOT NULL,
    department_name  VARCHAR(150) NOT NULL,
    course_name      VARCHAR(150) NOT NULL,
    semester         VARCHAR(20)  NOT NULL DEFAULT 'Sem 3',
    section_batch    VARCHAR(40)  NOT NULL DEFAULT 'A (Batch A1)',
    admission_year   INT          NOT NULL DEFAULT 2025,
    dataset_mode     VARCHAR(20)  NOT NULL DEFAULT 'BOTH',
    CONSTRAINT uq_student_enrollment_mode UNIQUE (enrollment_no, dataset_mode),
    INDEX idx_students_semester_mode (semester, dataset_mode)
) ENGINE=InnoDB;

-- 6. SUBJECTS (RTU Curriculum Sem 1 through Sem 8)
CREATE TABLE IF NOT EXISTS subjects (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_id       INT          NOT NULL,
    subject_code     VARCHAR(40)  NOT NULL,
    rtu_code         VARCHAR(40)  NULL,
    short_code       VARCHAR(30)  NULL,
    subject_name     VARCHAR(180) NOT NULL,
    department_name  VARCHAR(150) NOT NULL,
    semester         VARCHAR(20)  NOT NULL DEFAULT 'Sem 3',
    subject_type     VARCHAR(30)  NOT NULL DEFAULT 'Theory', -- Theory, Lab, Seminar, Project
    credits          INT          NOT NULL DEFAULT 3,
    weekly_hours     INT          NOT NULL DEFAULT 3,
    dataset_mode     VARCHAR(20)  NOT NULL DEFAULT 'MODE_B',
    CONSTRAINT uq_subject_code_mode UNIQUE (subject_code, dataset_mode),
    INDEX idx_subjects_semester_mode (semester, dataset_mode)
) ENGINE=InnoDB;

-- 7. FACULTY
CREATE TABLE IF NOT EXISTS faculty (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_id         INT          NOT NULL,
    faculty_code       VARCHAR(30)  NOT NULL,
    short_abbr         VARCHAR(20)  NULL,
    full_name          VARCHAR(120) NOT NULL,
    email              VARCHAR(150) NOT NULL,
    phone              VARCHAR(30)  NOT NULL,
    department_name    VARCHAR(150) NOT NULL,
    designation        VARCHAR(80)  NOT NULL DEFAULT 'Assistant Professor',
    specialization     VARCHAR(120) NOT NULL,
    assigned_subjects  VARCHAR(255) NOT NULL,
    max_weekly_hours   INT          NOT NULL DEFAULT 18,
    on_leave           BOOLEAN      NOT NULL DEFAULT FALSE,
    dataset_mode       VARCHAR(20)  NOT NULL DEFAULT 'BOTH',
    CONSTRAINT uq_faculty_code_mode UNIQUE (faculty_code, dataset_mode),
    INDEX idx_faculty_mode (dataset_mode)
) ENGINE=InnoDB;

-- 8. ROOMS & LABS
CREATE TABLE IF NOT EXISTS rooms (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_code               VARCHAR(40)  NOT NULL,
    room_name               VARCHAR(120) NOT NULL,
    room_type               VARCHAR(40)  NOT NULL DEFAULT 'Classroom', -- Classroom, Lab
    building_block          VARCHAR(40)  NOT NULL DEFAULT 'Block A',
    capacity                INT          NOT NULL DEFAULT 60,
    total_weekly_slots      INT          NOT NULL DEFAULT 36,
    occupied_slots_override INT          NULL,
    dataset_mode            VARCHAR(20)  NOT NULL DEFAULT 'BOTH',
    CONSTRAINT uq_room_code_mode UNIQUE (room_code, dataset_mode),
    INDEX idx_rooms_mode (dataset_mode)
) ENGINE=InnoDB;

-- 9. TIME SLOTS (Active Bell Schedule P1-P6 + Lunch Break; Period 7 Removed)
CREATE TABLE IF NOT EXISTS time_slots (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_code       VARCHAR(20)  NOT NULL,
    slot_label      VARCHAR(60)  NOT NULL,
    day_of_week     VARCHAR(40)  NOT NULL DEFAULT 'Monday - Saturday',
    start_time      VARCHAR(20)  NOT NULL,
    end_time        VARCHAR(20)  NOT NULL,
    slot_type       VARCHAR(30)  NOT NULL DEFAULT 'Lecture', -- Lecture, Lab, Break
    period_number   INT          NOT NULL,
    dataset_mode    VARCHAR(20)  NOT NULL DEFAULT 'BOTH',
    CONSTRAINT uq_slot_code_mode UNIQUE (slot_code, dataset_mode)
) ENGINE=InnoDB;

-- 10. TIMETABLE ENTRIES (8-Semester Scheduling Store + Strict Lock for Generated Slots)
CREATE TABLE IF NOT EXISTS timetable_entries (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week          VARCHAR(20)  NOT NULL,
    date_label           VARCHAR(30)  NULL,
    period_code          VARCHAR(20)  NOT NULL,
    time_range           VARCHAR(50)  NOT NULL,
    section_code         VARCHAR(30)  NOT NULL,
    batch_code           VARCHAR(30)  NOT NULL DEFAULT 'ALL',
    subject_code         VARCHAR(50)  NOT NULL,
    subject_name         VARCHAR(180) NOT NULL,
    session_type         VARCHAR(30)  NOT NULL DEFAULT 'Lecture',
    faculty_code         VARCHAR(40)  NOT NULL,
    faculty_name         VARCHAR(120) NOT NULL,
    substitute_faculty   VARCHAR(120) NULL,
    room_code            VARCHAR(60)  NOT NULL,
    color_theme          VARCHAR(30)  NULL DEFAULT 'blue',
    has_conflict         BOOLEAN      NOT NULL DEFAULT FALSE,
    conflict_reason      VARCHAR(255) NULL,
    dataset_mode         VARCHAR(20)  NOT NULL DEFAULT 'MODE_B',
    semester             VARCHAR(20)  NOT NULL DEFAULT 'Sem 3',
    is_generated         BOOLEAN      NOT NULL DEFAULT FALSE,
    -- Strictly enforce zero double-booking for auto-generated & CSP-validated conflict-free schedules
    faculty_slot_lock    VARCHAR(160) GENERATED ALWAYS AS (
        CASE WHEN is_generated = TRUE AND has_conflict = FALSE
                  AND faculty_code NOT IN ('-', 'NA', 'ALL', 'LIB', 'NPTEL', 'SG')
             THEN CONCAT(dataset_mode, '|', day_of_week, '|', period_code, '|', faculty_code)
             ELSE NULL END
    ) VIRTUAL,
    room_slot_lock       VARCHAR(160) GENERATED ALWAYS AS (
        CASE WHEN is_generated = TRUE AND has_conflict = FALSE
                  AND room_code NOT IN ('-', 'NA', 'Library', 'NPTEL', 'Self')
             THEN CONCAT(dataset_mode, '|', day_of_week, '|', period_code, '|', room_code)
             ELSE NULL END
    ) VIRTUAL,
    CONSTRAINT uq_faculty_no_double_booking UNIQUE (faculty_slot_lock),
    CONSTRAINT uq_room_no_double_booking    UNIQUE (room_slot_lock),
    CONSTRAINT fk_tt_section  FOREIGN KEY (section_code) REFERENCES class_sections(section_code) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_tt_semester FOREIGN KEY (semester)     REFERENCES semesters(semester_code)    ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_tt_mode_sem_day (dataset_mode, semester, day_of_week),
    INDEX idx_tt_section_slot (dataset_mode, section_code, day_of_week, period_code)
) ENGINE=InnoDB;
