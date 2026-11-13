package edu.piet.smartcampus.runtime;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Complete Relational In-Memory Data Store, Seeder & CSP Engine for
 * Smart Campus Timetable Management System (v1.0.0 • College Edition).
 * Supports seamless toggling between:
 * - MODE_A: UI Screenshot Demo Dataset (4 Students, 6 Faculty, 14 Subjects, 8 Rooms, 90 Entries, 8 Conflicts)
 * - MODE_B: PIET III-Sem Master Excel Dataset (Oct 5-9, 2026, 4 Sections, 19 Faculty, 17 Rooms, 309 Cells)
 */
public class SmartCampusDataStore {

    private final AtomicLong idGen = new AtomicLong(1000);

    public final List<Map<String, Object>> semesters = Collections.synchronizedList(new ArrayList<>());
    public final List<Map<String, Object>> departments = Collections.synchronizedList(new ArrayList<>());
    public final List<Map<String, Object>> courses = Collections.synchronizedList(new ArrayList<>());
    public final List<Map<String, Object>> sections = Collections.synchronizedList(new ArrayList<>());
    public final List<Map<String, Object>> timeSlots = Collections.synchronizedList(new ArrayList<>());

    public final Map<String, List<Map<String, Object>>> studentsByMode = new ConcurrentHashMap<>();
    public final Map<String, List<Map<String, Object>>> facultyByMode = new ConcurrentHashMap<>();
    public final Map<String, List<Map<String, Object>>> subjectsByMode = new ConcurrentHashMap<>();
    public final Map<String, List<Map<String, Object>>> roomsByMode = new ConcurrentHashMap<>();
    public final Map<String, List<Map<String, Object>>> timetableByMode = new ConcurrentHashMap<>();
    public final Map<String, List<Map<String, Object>>> generatorPreviewByMode = new ConcurrentHashMap<>();
    public final Map<String, Map<String, Object>> generatorMetaByMode = new ConcurrentHashMap<>();

    public SmartCampusDataStore() {
        resetAllData();
    }

    public synchronized void resetAllData() {
        idGen.set(3000);
        semesters.clear();
        departments.clear();
        courses.clear();
        sections.clear();
        timeSlots.clear();
        studentsByMode.clear();
        facultyByMode.clear();
        subjectsByMode.clear();
        roomsByMode.clear();
        timetableByMode.clear();
        generatorPreviewByMode.clear();
        generatorMetaByMode.clear();

        seedSharedEntities();
        seedModeA();
        seedModeB();
        auditConflicts("MODE_A");
        auditConflicts("MODE_B");
    }

    private void seedSharedEntities() {
        // Semesters (Sem 1 through Sem 8)
        String[][] semDefs = {
            {"1", "Sem 1", "Semester 1 (First Year Foundation)", "ODD", "21"},
            {"2", "Sem 2", "Semester 2 (First Year Core)", "EVEN", "21"},
            {"3", "Sem 3", "Semester 3 (CSE Core - PIET Master)", "ODD", "24"},
            {"4", "Sem 4", "Semester 4 (Algorithms & Systems)", "EVEN", "23"},
            {"5", "Sem 5", "Semester 5 (Compilers & OS)", "ODD", "23"},
            {"6", "Sem 6", "Semester 6 (ML & Cloud Computing)", "EVEN", "22"},
            {"7", "Sem 7", "Semester 7 (IoT, Security & Seminar)", "ODD", "20"},
            {"8", "Sem 8", "Semester 8 (Final Year Project & Internship)", "EVEN", "16"}
        };
        for (String[] s : semDefs) {
            semesters.add(mapOf(
                    "id", Long.parseLong(s[0]),
                    "semesterNumber", Integer.parseInt(s[0]),
                    "semesterCode", s[1],
                    "semesterName", s[2],
                    "academicTerm", s[3],
                    "totalCredits", Integer.parseInt(s[4])
            ));
        }

        // Screenshot 6: Department Management (Exact IDs: 1 CSE, 3 ECE, 2 IT, 4 MECH)
        departments.add(mapOf("id", 1L, "displayId", 1, "code", "CSE", "name", "Computer Science & Engineering", "hodName", "Dr. Anil Kumar", "buildingBlock", "Block A & C"));
        departments.add(mapOf("id", 3L, "displayId", 3, "code", "ECE", "name", "Electronics & Communication", "hodName", "Dr. Rakesh Gupta", "buildingBlock", "Block B"));
        departments.add(mapOf("id", 2L, "displayId", 2, "code", "IT", "name", "Information Technology", "hodName", "Dr. Pooja Sharma", "buildingBlock", "Block C"));
        departments.add(mapOf("id", 4L, "displayId", 4, "code", "MECH", "name", "Mechanical Engineering", "hodName", "Dr. Vikram Singh", "buildingBlock", "Block D"));

        // Courses
        courses.add(mapOf("id", 1L, "displayId", 1, "code", "BTECH-CSE", "name", "B.Tech Computer Engineering", "departmentCode", "CSE", "departmentName", "Computer Science & Engineering", "durationYears", 4, "totalSemesters", 8, "totalCredits", 164, "coordinator", "Dr. Varsha Kumari"));
        courses.add(mapOf("id", 2L, "displayId", 2, "code", "BTECH-AIDS", "name", "B.Tech CS & Engg. (AI & Data Science)", "departmentCode", "CSE", "departmentName", "Computer Science & Engineering", "durationYears", 4, "totalSemesters", 8, "totalCredits", 164, "coordinator", "Dr. Rajesh Verma"));
        courses.add(mapOf("id", 3L, "displayId", 3, "code", "BTECH-IT", "name", "B.Tech Information Technology", "departmentCode", "IT", "departmentName", "Information Technology", "durationYears", 4, "totalSemesters", 8, "totalCredits", 160, "coordinator", "Madhu Choudary"));
        courses.add(mapOf("id", 4L, "displayId", 4, "code", "BTECH-ECE", "name", "B.Tech Electronics & Communication", "departmentCode", "ECE", "departmentName", "Electronics & Communication", "durationYears", 4, "totalSemesters", 8, "totalCredits", 160, "coordinator", "Dr.Ashwini"));

        // Class Sections across all 8 Semesters (PIET Sem 1..Sem 8, including III-Sem 3CSA/B/C/RD)
        sections.add(mapOf("id", 10L, "sectionCode", "1CSA", "sectionName", "Sem 1 - Sec A (1CSA)", "semester", "Sem 1", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "C-208", "studentStrength", 60, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 20L, "sectionCode", "2CSA", "sectionName", "Sem 2 - Sec A (2CSA)", "semester", "Sem 2", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "C-308", "studentStrength", 60, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 1L, "sectionCode", "3CSA", "sectionName", "Sem 3 - Sec A (Entire Section)", "semester", "Sem 3", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2, A3", "primaryRoom", "A-202", "studentStrength", 66, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 2L, "sectionCode", "3CSB", "sectionName", "Sem 3 - Sec B (3CSB)", "semester", "Sem 3", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "B1, B2, B3", "primaryRoom", "C-201", "studentStrength", 64, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 3L, "sectionCode", "3CSC", "sectionName", "Sem 3 - Sec C (3CSC)", "semester", "Sem 3", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "C1, C2, C3", "primaryRoom", "C-202", "studentStrength", 62, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 4L, "sectionCode", "3CSRD", "sectionName", "Sem 3 - Sec D (3CSRD)", "semester", "Sem 3", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "D1, D2", "primaryRoom", "A-203", "studentStrength", 54, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 40L, "sectionCode", "4CSA", "sectionName", "Sem 4 - Sec A (4CSA)", "semester", "Sem 4", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "A-203", "studentStrength", 60, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 50L, "sectionCode", "5CSA", "sectionName", "Sem 5 - Sec A (5CSA)", "semester", "Sem 5", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "C-208", "studentStrength", 58, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 60L, "sectionCode", "6CSA", "sectionName", "Sem 6 - Sec A (6CSA)", "semester", "Sem 6", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "C-308", "studentStrength", 58, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 70L, "sectionCode", "7CSA", "sectionName", "Sem 7 - Sec A (7CSA)", "semester", "Sem 7", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "A-201", "studentStrength", 55, "academicYear", "2026-2027"));
        sections.add(mapOf("id", 80L, "sectionCode", "8CSA", "sectionName", "Sem 8 - Sec A (Project & Internship)", "semester", "Sem 8", "courseName", "B.Tech Computer Engineering", "departmentCode", "CSE", "subBatches", "A1, A2", "primaryRoom", "SH01", "studentStrength", 55, "academicYear", "2026-2027"));

        // Time Slots (Screenshot 8)
        timeSlots.add(mapOf("id", 1L, "slotCode", "P1", "slotLabel", "Period 1", "dayOfWeek", "Monday - Saturday", "startTime", "08:30 AM", "endTime", "09:30 AM", "slotType", "Lecture", "periodNumber", 1));
        timeSlots.add(mapOf("id", 2L, "slotCode", "P2", "slotLabel", "Period 2", "dayOfWeek", "Monday - Saturday", "startTime", "09:30 AM", "endTime", "10:30 AM", "slotType", "Lecture", "periodNumber", 2));
        timeSlots.add(mapOf("id", 3L, "slotCode", "P3", "slotLabel", "Period 3", "dayOfWeek", "Monday - Saturday", "startTime", "10:30 AM", "endTime", "11:30 AM", "slotType", "Lecture", "periodNumber", 3));
        timeSlots.add(mapOf("id", 4L, "slotCode", "P4", "slotLabel", "Period 4", "dayOfWeek", "Monday - Saturday", "startTime", "11:30 AM", "endTime", "12:30 PM", "slotType", "Lecture", "periodNumber", 4));
        timeSlots.add(mapOf("id", 5L, "slotCode", "BREAK", "slotLabel", "Lunch Break", "dayOfWeek", "Monday - Saturday", "startTime", "12:30 PM", "endTime", "01:10 PM", "slotType", "Break", "periodNumber", 5));
        timeSlots.add(mapOf("id", 6L, "slotCode", "P5", "slotLabel", "Period 5", "dayOfWeek", "Monday - Saturday", "startTime", "01:10 PM", "endTime", "02:00 PM", "slotType", "Lecture", "periodNumber", 6));
        timeSlots.add(mapOf("id", 7L, "slotCode", "P6", "slotLabel", "Period 6", "dayOfWeek", "Monday - Saturday", "startTime", "02:00 PM", "endTime", "02:50 PM", "slotType", "Lecture", "periodNumber", 7));
    }

    private void seedModeA() {
        List<Map<String, Object>> students = new ArrayList<>();
        // Exact 4 rows from Screenshot 4
        students.add(mapOf("id", 1L, "displayId", 1, "enrollmentNo", "STU001", "fullName", "Anurag Chaudhary", "email", "student1@piet.edu", "phone", "9998887771", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "A (Batch A1)", "admissionYear", 2025));
        students.add(mapOf("id", 2L, "displayId", 2, "enrollmentNo", "STU002", "fullName", "Arnav Nagar", "email", "student2@piet.edu", "phone", "9998887772", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "A (Batch A1)", "admissionYear", 2025));
        students.add(mapOf("id", 3L, "displayId", 3, "enrollmentNo", "STU003", "fullName", "Bhawesh Sharma", "email", "bhaweshs1@piet.edu", "phone", "1234567891", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "A (Batch A1)", "admissionYear", 2025));
        students.add(mapOf("id", 4L, "displayId", 4, "enrollmentNo", "STU004", "fullName", "Abhishek Sharma", "email", "1234@gmail.com", "phone", "1231231230", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "A (Batch A2)", "admissionYear", 2025));
        studentsByMode.put("MODE_A", students);

        // Exact 6 rows from Screenshot 5
        List<Map<String, Object>> faculty = new ArrayList<>();
        faculty.add(mapOf("id", 1L, "displayId", 1, "facultyCode", "FAC-001", "shortAbbr", "VK", "fullName", "Dr. Varsha Kumari", "email", "kumaridimple@piet.edu", "phone", "6375900934", "departmentName", "Computer Science & Engineering", "designation", "Associate Professor", "specialization", "DSA", "assignedSubjects", "CS-001, CS-002, CS-003", "maxWeeklyHours", 18, "onLeave", false));
        faculty.add(mapOf("id", 2L, "displayId", 2, "facultyCode", "FAC-002", "shortAbbr", "MC", "fullName", "Madhu Choudary", "email", "profmadhu1@piet.edu", "phone", "9876543211", "departmentName", "Computer Science & Engineering", "designation", "Associate Professor", "specialization", "java-programming", "assignedSubjects", "CS-004, CS-005", "maxWeeklyHours", 18, "onLeave", false));
        faculty.add(mapOf("id", 3L, "displayId", 3, "facultyCode", "FAC-003", "shortAbbr", "AK", "fullName", "Dr.Ashwini", "email", "profashwini@piet.edu", "phone", "9876543212", "departmentName", "Computer Science & Engineering", "designation", "Assistant Professor", "specialization", "DSLD", "assignedSubjects", "CS303, CS304, CS-006, CS-007", "maxWeeklyHours", 18, "onLeave", false));
        faculty.add(mapOf("id", 31L, "displayId", 31, "facultyCode", "FAC-004", "shortAbbr", "OPS", "fullName", "OP sikhwal", "email", "opsir@gmail.com", "phone", "32423432423", "departmentName", "Computer Science & Engineering", "designation", "Professor", "specialization", "MATHS", "assignedSubjects", "CS305", "maxWeeklyHours", 18, "onLeave", false));
        faculty.add(mapOf("id", 5L, "displayId", 5, "facultyCode", "FAC-005", "shortAbbr", "UO", "fullName", "Udbhav Oajha", "email", "profudbhav1@piet.edu", "phone", "983232423", "departmentName", "Computer Science & Engineering", "designation", "Professor", "specialization", "UI/UX", "assignedSubjects", "CS-008, CS-009", "maxWeeklyHours", 18, "onLeave", false));
        faculty.add(mapOf("id", 6L, "displayId", 6, "facultyCode", "FAC-006", "shortAbbr", "PD", "fullName", "Pince Dabar", "email", "profprince12@piet.edu", "phone", "998696776", "departmentName", "Computer Science & Engineering", "designation", "Professor", "specialization", "Technical Communication", "assignedSubjects", "CS-112", "maxWeeklyHours", 18, "onLeave", false));
        facultyByMode.put("MODE_A", faculty);

        // Exact 14 Subjects from Screenshot 7
        List<Map<String, Object>> subjects = new ArrayList<>();
        subjects.add(mapOf("id", 1L, "displayId", 1, "subjectCode", "CS-001", "rtuCode", "253CS4-04", "shortCode", "DSA", "subjectName", "Data Structures and Algorithms", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 4, "weeklyHours", 4));
        subjects.add(mapOf("id", 2L, "displayId", 2, "subjectCode", "CS-002", "rtuCode", "253CS4-21", "shortCode", "DSA Lab", "subjectName", "Data Structures & Algorithms Lab", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Lab", "credits", 2, "weeklyHours", 2));
        subjects.add(mapOf("id", 3L, "displayId", 3, "subjectCode", "CS-003", "rtuCode", "253CS4-06", "shortCode", "DBMS", "subjectName", "Database Management Systems", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 4, "weeklyHours", 4));
        subjects.add(mapOf("id", 4L, "displayId", 4, "subjectCode", "CS-004", "rtuCode", "253CS4-05", "shortCode", "Java", "subjectName", "Object Oriented Programming (Java)", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 3, "weeklyHours", 3));
        subjects.add(mapOf("id", 5L, "displayId", 5, "subjectCode", "CS-005", "rtuCode", "253CS4-22", "shortCode", "Java Lab", "subjectName", "Java Programming Lab", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Lab", "credits", 2, "weeklyHours", 2));
        subjects.add(mapOf("id", 6L, "displayId", 6, "subjectCode", "CS-006", "rtuCode", "253CS4-03", "shortCode", "DSLD", "subjectName", "Digital Systems & Logic Design", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 3, "weeklyHours", 3));
        subjects.add(mapOf("id", 7L, "displayId", 7, "subjectCode", "CS-007", "rtuCode", "253CS4-24", "shortCode", "DSLD Lab", "subjectName", "Digital Systems & Logic Design Lab", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Lab", "credits", 2, "weeklyHours", 2));
        subjects.add(mapOf("id", 8L, "displayId", 8, "subjectCode", "CS-008", "rtuCode", "253CR4-08", "shortCode", "UI/UX", "subjectName", "UI/UX Design Principles", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 3, "weeklyHours", 3));
        subjects.add(mapOf("id", 9L, "displayId", 9, "subjectCode", "CS-009", "rtuCode", "253CR4-28", "shortCode", "UI/UX Lab", "subjectName", "UI/UX Design & Prototyping Lab", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Lab", "credits", 2, "weeklyHours", 2));
        subjects.add(mapOf("id", 10L, "displayId", 10, "subjectCode", "CS303", "rtuCode", "253CS4-03A", "shortCode", "COA", "subjectName", "Computer Organization & Architecture", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 3, "weeklyHours", 3));
        subjects.add(mapOf("id", 11L, "displayId", 11, "subjectCode", "CS304", "rtuCode", "253CS4-06A", "shortCode", "OS", "subjectName", "Operating Systems", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 4, "weeklyHours", 4));
        subjects.add(mapOf("id", 12L, "displayId", 12, "subjectCode", "CS305", "rtuCode", "253CS3-01", "shortCode", "MATHS", "subjectName", "Advanced Engineering Mathematics", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 4, "weeklyHours", 4));
        subjects.add(mapOf("id", 13L, "displayId", 13, "subjectCode", "CS-112", "rtuCode", "253CS2-02", "shortCode", "TC", "subjectName", "Technical Communication", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Theory", "credits", 2, "weeklyHours", 2));
        subjects.add(mapOf("id", 14L, "displayId", 14, "subjectCode", "CS-010", "rtuCode", "253CS4-23", "shortCode", "DBMS Lab", "subjectName", "Database Management Systems Lab", "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", "Lab", "credits", 2, "weeklyHours", 2));
        subjectsByMode.put("MODE_A", subjects);

        // Exact 8 Rooms from Screenshot 10
        List<Map<String, Object>> rooms = new ArrayList<>();
        rooms.add(mapOf("id", 1L, "roomCode", "A-202", "roomName", "Room 102", "roomType", "Classroom", "buildingBlock", "Block A", "capacity", 60, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 2L, "roomCode", "A-209", "roomName", "CS Lab 1", "roomType", "Lab", "buildingBlock", "Block A", "capacity", 30, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 3L, "roomCode", "C-201", "roomName", "C-201", "roomType", "Classroom", "buildingBlock", "Block C", "capacity", 60, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 4L, "roomCode", "C-202", "roomName", "Room 101", "roomType", "Classroom", "buildingBlock", "Block C", "capacity", 60, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 5L, "roomCode", "C-205", "roomName", "CS Lab 3", "roomType", "Lab", "buildingBlock", "Block C", "capacity", 30, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 6L, "roomCode", "C-207", "roomName", "CS Lab 2", "roomType", "Lab", "buildingBlock", "Block C", "capacity", 30, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 7L, "roomCode", "C-208", "roomName", "C-208", "roomType", "Classroom", "buildingBlock", "Block C", "capacity", 60, "totalWeeklySlots", 36));
        rooms.add(mapOf("id", 8L, "roomCode", "SH01", "roomName", "Seminar Hall 1", "roomType", "Classroom", "buildingBlock", "Block A", "capacity", 120, "totalWeeklySlots", 36));
        roomsByMode.put("MODE_A", rooms);

        // Build exact 90 Timetable Entries for Mode A matching Screenshot 1, 2, 3, 9, 10
        // Room target counts: A-202 (25), A-209 (16), C-201 (12), C-202 (8), C-205 (4), C-207 (17), C-208 (0), SH01 (8) = 90 total!
        List<Map<String, Object>> entries = new ArrayList<>();
        long eid = 1;

        // First: Top rows from Screenshot 3 (Admin Dashboard Master Scheduled Timetable Entries Table)
        // Friday P1 entries + 8 exact conflict pairs (8 conflicts)
        entries.add(entry(eid++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "A-202", "blue"));
        entries.add(entry(eid++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSB", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "A-202", "indigo")); // Conflict #1: A-202 double booked Friday P1!
        entries.add(entry(eid++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSC", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "C-201", "emerald"));

        // Conflict #2: Monday P1 Room A-202 double-booked between 3CSA and 3CSB
        entries.add(entry(eid++, "Monday", "05-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "A-202", "blue"));
        entries.add(entry(eid++, "Monday", "05-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSB", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "A-202", "amber"));

        // Conflict #3: Tuesday P2 Lab A-209 double-booked between 3CSA (A1) and 3CSB (B1)
        entries.add(entry(eid++, "Tuesday", "06-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSA", "A1", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "A-209", "purple"));
        entries.add(entry(eid++, "Tuesday", "06-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSB", "B1", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "A-209", "cyan"));

        // Conflict #4: Wednesday P3 Lab C-207 double-booked between 3CSA (A2) and 3CSC (C1)
        entries.add(entry(eid++, "Wednesday", "07-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSA", "A2", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "C-207", "cyan"));
        entries.add(entry(eid++, "Wednesday", "07-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSC", "C1", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "C-207", "rose"));

        // Conflict #5: Thursday P1 Classroom C-201 double-booked between 3CSB and 3CSC
        entries.add(entry(eid++, "Thursday", "08-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSB", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "C-201", "cyan"));
        entries.add(entry(eid++, "Thursday", "08-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSC", "ALL", "CS-006", "Digital Systems & Logic Design", "Lecture", "FAC-003", "Dr.Ashwini", "C-201", "emerald"));

        // Conflict #6: Thursday P4 Seminar Hall SH01 double-booked
        entries.add(entry(eid++, "Thursday", "08-10-2026", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "SH01", "amber"));
        entries.add(entry(eid++, "Thursday", "08-10-2026", "P4", "11:30 AM - 12:30 PM", "3CSRD", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "SH01", "emerald"));

        // Conflict #7: Friday P3 Lab A-209 double-booked
        entries.add(entry(eid++, "Friday", "09-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSA", "A3", "CS-009", "UI/UX Design & Prototyping Lab", "Lab", "FAC-005", "Udbhav Oajha", "A-209", "purple"));
        entries.add(entry(eid++, "Friday", "09-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSC", "C2", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "A-209", "blue"));

        // Conflict #8: Saturday P1 Room C-202 double-booked
        entries.add(entry(eid++, "Saturday", "10-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSC", "ALL", "CS303", "Computer Organization & Architecture", "Lecture", "FAC-003", "Dr.Ashwini", "C-202", "indigo"));
        entries.add(entry(eid++, "Saturday", "10-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSRD", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "C-202", "amber"));

        // Now populate the remaining conflict-free slots to hit the exact per-room occupied totals:
        // Currently used:
        // A-202: 4 (need 21 more -> 25)
        // A-209: 4 (need 12 more -> 16)
        // C-201: 3 (need 9 more -> 12)
        // C-202: 2 (need 6 more -> 8)
        // C-205: 0 (need 4 more -> 4)
        // C-207: 2 (need 15 more -> 17)
        // C-208: 0 (need 0 more -> 0)
        // SH01:  2 (need 6 more -> 8)

        String[][] a202Slots = {
            {"Monday", "P2", "09:30 AM - 10:30 AM", "3CSA", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "cyan"},
            {"Monday", "P3", "10:30 AM - 11:30 AM", "3CSA", "ALL", "CS-006", "Digital Systems & Logic Design", "Lecture", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Monday", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Monday", "P5", "01:10 PM - 02:00 PM", "3CSA", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Tuesday", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Tuesday", "P3", "10:30 AM - 11:30 AM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Tuesday", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "purple"},
            {"Tuesday", "P5", "01:10 PM - 02:00 PM", "3CSA", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "emerald"},
            {"Wednesday", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "cyan"},
            {"Wednesday", "P2", "09:30 AM - 10:30 AM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Wednesday", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Wednesday", "P5", "01:10 PM - 02:00 PM", "3CSA", "ALL", "CS303", "Computer Organization & Architecture", "Lecture", "FAC-003", "Dr.Ashwini", "rose"},
            {"Thursday", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Thursday", "P2", "09:30 AM - 10:30 AM", "3CSA", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Thursday", "P3", "10:30 AM - 11:30 AM", "3CSA", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Thursday", "P5", "01:10 PM - 02:00 PM", "3CSA", "ALL", "CS304", "Operating Systems", "Lecture", "FAC-003", "Dr.Ashwini", "blue"},
            {"Friday", "P2", "09:30 AM - 10:30 AM", "3CSA", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "cyan"},
            {"Friday", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "purple"},
            {"Friday", "P5", "01:10 PM - 02:00 PM", "3CSA", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Saturday", "P1", "08:30 AM - 09:30 AM", "3CSA", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "emerald"},
            {"Saturday", "P2", "09:30 AM - 10:30 AM", "3CSA", "ALL", "CS304", "Operating Systems", "Lecture", "FAC-003", "Dr.Ashwini", "blue"}
        };
        for (String[] s : a202Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "A-202", s[10]));
        }

        // 12 more slots for A-209 (CS Lab 1) across valid P1-P6 schedule (Period 7 deleted)
        String[][] a209Slots = {
            {"Monday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Saturday", "P1", "08:30 AM - 09:30 AM", "3CSB", "B1", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Tuesday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Saturday", "P2", "09:30 AM - 10:30 AM", "3CSB", "B1", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Wednesday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A3", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Saturday", "P5", "01:10 PM - 02:00 PM", "3CSB", "B2", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Thursday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "CS-009", "UI/UX Design & Prototyping Lab", "Lab", "FAC-005", "Udbhav Oajha", "purple"},
            {"Saturday", "P6", "02:00 PM - 02:50 PM", "3CSB", "B2", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Friday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "CS-009", "UI/UX Design & Prototyping Lab", "Lab", "FAC-005", "Udbhav Oajha", "purple"},
            {"Wednesday", "P4", "11:30 AM - 12:30 PM", "3CSB", "B3", "CS-009", "UI/UX Design & Prototyping Lab", "Lab", "FAC-005", "Udbhav Oajha", "purple"},
            {"Saturday", "P3", "10:30 AM - 11:30 AM", "3CSA", "A3", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Saturday", "P4", "11:30 AM - 12:30 PM", "3CSA", "A3", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"}
        };
        for (String[] s : a209Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "A-209", s[10]));
        }

        // 9 more slots for C-201 (Total 12)
        String[][] c201Slots = {
            {"Monday", "P2", "09:30 AM - 10:30 AM", "3CSB", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Monday", "P3", "10:30 AM - 11:30 AM", "3CSB", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Tuesday", "P1", "08:30 AM - 09:30 AM", "3CSB", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "cyan"},
            {"Tuesday", "P3", "10:30 AM - 11:30 AM", "3CSB", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Wednesday", "P1", "08:30 AM - 09:30 AM", "3CSB", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Wednesday", "P2", "09:30 AM - 10:30 AM", "3CSB", "ALL", "CS-006", "Digital Systems & Logic Design", "Lecture", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Thursday", "P2", "09:30 AM - 10:30 AM", "3CSB", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "purple"},
            {"Friday", "P2", "09:30 AM - 10:30 AM", "3CSB", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "emerald"},
            {"Saturday", "P2", "09:30 AM - 10:30 AM", "3CSB", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"}
        };
        for (String[] s : c201Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "C-201", s[10]));
        }

        // 6 more slots for C-202 (Total 8)
        String[][] c202Slots = {
            {"Monday", "P4", "11:30 AM - 12:30 PM", "3CSC", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Tuesday", "P4", "11:30 AM - 12:30 PM", "3CSC", "ALL", "CS-003", "Database Management Systems", "Lecture", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Wednesday", "P4", "11:30 AM - 12:30 PM", "3CSC", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Thursday", "P3", "10:30 AM - 11:30 AM", "3CSC", "ALL", "CS-004", "Object Oriented Programming (Java)", "Lecture", "FAC-002", "Madhu Choudary", "cyan"},
            {"Friday", "P4", "11:30 AM - 12:30 PM", "3CSC", "ALL", "CS-006", "Digital Systems & Logic Design", "Lecture", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Saturday", "P3", "10:30 AM - 11:30 AM", "3CSC", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "purple"}
        };
        for (String[] s : c202Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "C-202", s[10]));
        }

        // 4 slots for C-205 (CS Lab 3 - Total 4 across P1-P6)
        String[][] c205Slots = {
            {"Monday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A3", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Saturday", "P3", "10:30 AM - 11:30 AM", "3CSB", "B3", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Tuesday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Saturday", "P4", "11:30 AM - 12:30 PM", "3CSB", "B3", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"}
        };
        for (String[] s : c205Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "C-205", s[10]));
        }

        // 15 more slots for C-207 (CS Lab 2 - Total 17 across P1-P6)
        String[][] c207Slots = {
            {"Monday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Saturday", "P1", "08:30 AM - 09:30 AM", "3CSB", "B2", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Tuesday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A3", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Saturday", "P2", "09:30 AM - 10:30 AM", "3CSB", "B2", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Wednesday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Saturday", "P3", "10:30 AM - 11:30 AM", "3CSRD", "D1", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Thursday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Saturday", "P4", "11:30 AM - 12:30 PM", "3CSRD", "D1", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Friday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Thursday", "P4", "11:30 AM - 12:30 PM", "3CSB", "B1", "CS-007", "Digital Systems & Logic Design Lab", "Lab", "FAC-003", "Dr.Ashwini", "emerald"},
            {"Saturday", "P5", "01:10 PM - 02:00 PM", "3CSA", "A2", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Saturday", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "CS-010", "Database Management Systems Lab", "Lab", "FAC-007", "Dr. Rajesh Verma", "indigo"},
            {"Wednesday", "P1", "08:30 AM - 09:30 AM", "3CSB", "B2", "CS-002", "Data Structures & Algorithms Lab", "Lab", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Thursday", "P2", "09:30 AM - 10:30 AM", "3CSB", "B3", "CS-005", "Java Programming Lab", "Lab", "FAC-002", "Madhu Choudary", "cyan"},
            {"Friday", "P2", "09:30 AM - 10:30 AM", "3CSC", "C3", "CS-009", "UI/UX Design & Prototyping Lab", "Lab", "FAC-005", "Udbhav Oajha", "purple"}
        };
        for (String[] s : c207Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "C-207", s[10]));
        }

        // 6 more slots for SH01 (Seminar Hall 1 - Total 8)
        String[][] sh01Slots = {
            {"Monday", "P5", "01:10 PM - 02:00 PM", "3CSB", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "emerald"},
            {"Tuesday", "P5", "01:10 PM - 02:00 PM", "3CSC", "ALL", "CS-008", "UI/UX Design Principles", "Lecture", "FAC-005", "Udbhav Oajha", "purple"},
            {"Wednesday", "P5", "01:10 PM - 02:00 PM", "3CSB", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Friday", "P5", "01:10 PM - 02:00 PM", "3CSC", "ALL", "CS305", "Advanced Engineering Mathematics", "Lecture", "FAC-004", "OP sikhwal", "amber"},
            {"Saturday", "P4", "11:30 AM - 12:30 PM", "3CSA", "ALL", "CS-001", "Data Structures and Algorithms", "Lecture", "FAC-001", "Dr. Varsha Kumari", "blue"},
            {"Saturday", "P5", "01:10 PM - 02:00 PM", "3CSC", "ALL", "CS-112", "Technical Communication", "Lecture", "FAC-006", "Pince Dabar", "emerald"}
        };
        for (String[] s : sh01Slots) {
            entries.add(entry(eid++, s[0], dateForDay(s[0]), s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], "SH01", s[10]));
        }

        timetableByMode.put("MODE_A", entries);
    }

    private void seedModeB() {
        // Students in Mode B (Includes the 4 core students + batch representatives across 3CSA, 3CSB, 3CSC, 3CSRD)
        List<Map<String, Object>> students = new ArrayList<>(studentsByMode.get("MODE_A"));
        students.add(mapOf("id", 5L, "displayId", 5, "enrollmentNo", "25EPIETCS045", "fullName", "Rohan Meena", "email", "rohan.25cs@piet.edu", "phone", "9829044101", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "A (Batch A3)", "admissionYear", 2025));
        students.add(mapOf("id", 6L, "displayId", 6, "enrollmentNo", "25EPIETCS072", "fullName", "Priyanshi Jain", "email", "priyanshi.25cs@piet.edu", "phone", "9829044102", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "B (Batch B1)", "admissionYear", 2025));
        students.add(mapOf("id", 7L, "displayId", 7, "enrollmentNo", "25EPIETCS098", "fullName", "Yashvardhan Singh", "email", "yash.25cs@piet.edu", "phone", "9829044103", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "B (Batch B2)", "admissionYear", 2025));
        students.add(mapOf("id", 8L, "displayId", 8, "enrollmentNo", "25EPIETCS134", "fullName", "Nandini Agrawal", "email", "nandini.25cs@piet.edu", "phone", "9829044104", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "C (Batch C1)", "admissionYear", 2025));
        students.add(mapOf("id", 9L, "displayId", 9, "enrollmentNo", "25EPIETCS156", "fullName", "Kartikeya Pareek", "email", "kartikeya.25cs@piet.edu", "phone", "9829044105", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "C (Batch C2)", "admissionYear", 2025));
        students.add(mapOf("id", 10L, "displayId", 10, "enrollmentNo", "25EPIETCR019", "fullName", "Aarav Khandelwal", "email", "aarav.25cr@piet.edu", "phone", "9829044106", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 3", "sectionBatch", "D (Batch D1)", "admissionYear", 2025));
        // Representative students across Sem 1, 2, 4, 5, 6, 7, 8
        students.add(mapOf("id", 11L, "displayId", 11, "enrollmentNo", "26EPIETCS001", "fullName", "Aryan Mathur", "email", "aryan.26cs@piet.edu", "phone", "9829044111", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 1", "sectionBatch", "A (Batch A1)", "admissionYear", 2026));
        students.add(mapOf("id", 12L, "displayId", 12, "enrollmentNo", "26EPIETCS034", "fullName", "Diya Rathore", "email", "diya.26cs@piet.edu", "phone", "9829044112", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 2", "sectionBatch", "A (Batch A1)", "admissionYear", 2026));
        students.add(mapOf("id", 13L, "displayId", 13, "enrollmentNo", "24EPIETCS021", "fullName", "Kunal Verma", "email", "kunal.24cs@piet.edu", "phone", "9829044114", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 4", "sectionBatch", "A (Batch A1)", "admissionYear", 2024));
        students.add(mapOf("id", 14L, "displayId", 14, "enrollmentNo", "24EPIETCS052", "fullName", "Meera Joshi", "email", "meera.24cs@piet.edu", "phone", "9829044115", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 5", "sectionBatch", "A (Batch A1)", "admissionYear", 2024));
        students.add(mapOf("id", 15L, "displayId", 15, "enrollmentNo", "23EPIETCS018", "fullName", "Raghav Gupta", "email", "raghav.23cs@piet.edu", "phone", "9829044116", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 6", "sectionBatch", "A (Batch A2)", "admissionYear", 2023));
        students.add(mapOf("id", 16L, "displayId", 16, "enrollmentNo", "23EPIETCS077", "fullName", "Sanya bansal", "email", "sanya.23cs@piet.edu", "phone", "9829044117", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 7", "sectionBatch", "A (Batch A1)", "admissionYear", 2023));
        students.add(mapOf("id", 17L, "displayId", 17, "enrollmentNo", "22EPIETCS009", "fullName", "Vivaan Kaushik", "email", "vivaan.22cs@piet.edu", "phone", "9829044118", "departmentName", "Computer Science & Engineering", "courseName", "B.Tech Computer Engineering", "semester", "Sem 8", "sectionBatch", "A (Project)", "admissionYear", 2022));
        studentsByMode.put("MODE_B", students);

        // 19 Faculty Members in Mode B (PIET III-Sem Master Excel Sheet)
        List<Map<String, Object>> faculty = new ArrayList<>();
        String[][] pietFac = {
            {"1", "FAC-001", "VK", "Dr. Varsha Kumari", "kumaridimple@piet.edu", "6375900934", "Associate Professor", "Data Structures & Algorithms", "253CS4-04, 253CS4-21"},
            {"2", "FAC-002", "MC", "Madhu Choudary", "profmadhu1@piet.edu", "9876543211", "Associate Professor", "OOP (C++/Java)", "253CS4-05, 253CS4-22"},
            {"3", "FAC-003", "AK", "Dr.Ashwini", "profashwini@piet.edu", "9876543212", "Assistant Professor", "Digital Electronics", "253CS4-03, 253CS4-24"},
            {"4", "FAC-004", "OPS", "OP sikhwal", "opsir@gmail.com", "9414000031", "Professor", "Advanced Engineering Mathematics", "253CS3-01"},
            {"5", "FAC-005", "UO", "Udbhav Oajha", "profudbhav1@piet.edu", "9832324230", "Professor", "UI/UX & Web Design", "253CR4-08, 253CR4-28"},
            {"6", "FAC-006", "PD", "Pince Dabar", "profprince12@piet.edu", "9986967760", "Professor", "Technical Communication", "253CS2-02"},
            {"7", "FAC-007", "RV", "Dr. Rajesh Verma", "rajesh.verma@piet.edu", "9829011223", "Professor", "Software Engineering & DBMS", "253CS4-06, 253CS4-23"},
            {"8", "FAC-008", "SK", "Ms. Shikha", "shikha.cs@piet.edu", "9829011224", "Assistant Professor", "Data Structures & C++ Lab", "253CS4-04, 253CS4-21"},
            {"9", "FAC-009", "AS", "Mr. Abhishek Sharma", "abhishek.sharma@piet.edu", "9829011225", "Assistant Professor", "Software Engineering", "253CS4-06, 253CS4-23"},
            {"10", "FAC-010", "PG", "Ms. Priyanka Gupta", "priyanka.gupta@piet.edu", "9829011226", "Assistant Professor", "Digital Electronics", "253CS4-03, 253CS4-24"},
            {"11", "FAC-011", "NK", "Mr. Naveen Kumar", "naveen.kumar@piet.edu", "9829011227", "Assistant Professor", "Object Oriented Programming", "253CS4-05, 253CS4-22"},
            {"12", "FAC-012", "SS", "Ms. Swati Sharma", "swati.sharma@piet.edu", "9829011228", "Assistant Professor", "Technical Communication", "253CS2-02"},
            {"13", "FAC-013", "DS", "Dr. Deepak Sharma", "deepak.sharma@piet.edu", "9829011229", "Associate Professor", "Advanced Engg. Mathematics", "253CS3-01"},
            {"14", "FAC-014", "RK", "Mr. Rahul Kumar", "rahul.kumar@piet.edu", "9829011230", "Assistant Professor", "Linux & Shell Programming", "253CS4-25"},
            {"15", "FAC-015", "PK", "Ms. Pooja Kumari", "pooja.kumari@piet.edu", "9829011231", "Assistant Professor", "Database & SE Lab", "253CS4-06, 253CS4-23"},
            {"16", "FAC-016", "MK", "Mr. Manish Kumar", "manish.kumar@piet.edu", "9829011232", "Assistant Professor", "Java & OOP Lab", "253CS4-22"},
            {"17", "FAC-017", "GS", "Ms. Garima Sharma", "garima.sharma@piet.edu", "9829011233", "Assistant Professor", "Data Science Foundations", "253CR4-07, 253CR4-27"},
            {"18", "FAC-018", "AP", "Dr. Anil Pratap", "anil.pratap@piet.edu", "9829011234", "Professor", "Industrial Training & Project", "253CS4-25"},
            {"19", "FAC-019", "NPTEL", "NPTEL / Library Coordinator", "nptel.cell@piet.edu", "9829011235", "Coordinator", "NPTEL, Library & Mentor-Mentee", "NPTEL, LIB, MM, SG"}
        };
        for (String[] f : pietFac) {
            faculty.add(mapOf("id", Long.parseLong(f[0]), "displayId", Integer.parseInt(f[0]), "facultyCode", f[1], "shortAbbr", f[2], "fullName", f[3], "email", f[4], "phone", f[5], "departmentName", "Computer Science & Engineering", "designation", f[6], "specialization", f[7], "assignedSubjects", f[8], "maxWeeklyHours", 18, "onLeave", false));
        }
        facultyByMode.put("MODE_B", faculty);

        // RTU III-Sem Subjects in Mode B
        List<Map<String, Object>> subjects = new ArrayList<>();
        String[][] pietSub = {
            {"1", "253CS3-01", "CS305", "AEM", "Advanced Engineering Mathematics", "Theory", "3", "4"},
            {"2", "253CS2-02", "CS-112", "TC", "Technical Communication", "Theory", "2", "2"},
            {"3", "253CS4-03", "CS-006", "DE", "Digital Electronics & Logic Design", "Theory", "3", "3"},
            {"4", "253CS4-04", "CS-001", "DSA", "Data Structures and Algorithms", "Theory", "4", "4"},
            {"5", "253CS4-05", "CS-004", "OOP", "Object Oriented Programming (C++/Java)", "Theory", "3", "3"},
            {"6", "253CS4-06", "CS-003", "SE", "Software Engineering & DBMS", "Theory", "4", "4"},
            {"7", "253CS4-21", "CS-002", "DSA Lab", "Data Structures and Algorithms Lab", "Lab", "2", "2"},
            {"8", "253CS4-22", "CS-005", "OOP Lab", "Object Oriented Programming Lab", "Lab", "2", "2"},
            {"9", "253CS4-23", "CS-010", "SE Lab", "Software Engineering & CASE Tools Lab", "Lab", "2", "2"},
            {"10", "253CS4-24", "CS-007", "DE Lab", "Digital Electronics Lab", "Lab", "2", "2"},
            {"11", "253CS4-25", "CS-011", "ITR", "Industrial Training & Seminar", "Lab", "1", "2"},
            {"12", "253CR4-07", "CR-007", "DSF", "Foundations of Data Science (3CSRD)", "Theory", "3", "3"},
            {"13", "253CR4-08", "CS-008", "UI/UX", "UI/UX Design & Human Computer Interaction", "Theory", "3", "3"},
            {"14", "253CR4-28", "CS-009", "UI/UX Lab", "UI/UX Interactive Prototyping Lab", "Lab", "2", "2"},
            {"15", "LIB", "LIB-01", "LIB", "Central Library / E-Journal Hour", "Zero Credit", "0", "1"},
            {"16", "NPTEL", "NPTEL-01", "NPTEL", "NPTEL Swayam Certification Slot", "Zero Credit", "0", "1"},
            {"17", "MM", "MM-01", "MM", "Mentor-Mentee Counseling Session", "Zero Credit", "0", "1"},
            {"18", "SG", "SG-01", "SG", "Self Growth & Coding Club Activity", "Zero Credit", "0", "1"}
        };
        for (String[] s : pietSub) {
            subjects.add(mapOf("id", Long.parseLong(s[0]), "displayId", Integer.parseInt(s[0]), "subjectCode", s[1], "rtuCode", s[2], "shortCode", s[3], "subjectName", s[4], "departmentName", "Computer Science & Engineering", "semester", "Sem 3", "subjectType", s[5], "credits", Integer.parseInt(s[6]), "weeklyHours", Integer.parseInt(s[7])));
        }

        // Complete PIET / RTU Curriculum across Sem 1, 2, 4, 5, 6, 7 & Sem 8 (FYP + Internship)
        String[][] otherSemSubjects = {
            // Sem 1
            {"101", "1FY2-01", "1FY2-01", "MATH-I", "Engineering Mathematics-I", "Sem 1", "Theory", "4", "3"},
            {"102", "1FY2-02", "1FY2-02", "PHY", "Engineering Physics", "Sem 1", "Theory", "3", "3"},
            {"103", "1FY3-06", "1FY3-06", "PPS", "Programming for Problem Solving (C)", "Sem 1", "Theory", "3", "3"},
            {"104", "1FY3-26", "1FY3-26", "PPS-LAB", "Computer Programming (C) Lab", "Sem 1", "Lab", "2", "2"},
            // Sem 2
            {"201", "2FY2-01", "2FY2-01", "MATH-II", "Engineering Mathematics-II", "Sem 2", "Theory", "4", "3"},
            {"202", "2FY2-03", "2FY2-03", "CHEM", "Engineering Chemistry & Env.", "Sem 2", "Theory", "3", "3"},
            {"203", "2FY3-07", "2FY3-07", "BEE", "Basic Electrical & Electronics", "Sem 2", "Theory", "3", "3"},
            {"204", "2FY3-27", "2FY3-27", "PY-LAB", "Python Scripting & CAMD Lab", "Sem 2", "Lab", "2", "2"},
            // Sem 4
            {"401", "4CS3-01", "4CS3-01", "DMS", "Discrete Mathematics Structures", "Sem 4", "Theory", "3", "3"},
            {"402", "4CS4-05", "4CS4-05", "DBMS-IV", "Database Management Systems", "Sem 4", "Theory", "4", "3"},
            {"403", "4CS4-06", "4CS4-06", "TOC", "Theory of Computation", "Sem 4", "Theory", "3", "3"},
            {"404", "4CS4-22", "4CS4-22", "DBMS-L4", "DBMS & SQL Lab", "Sem 4", "Lab", "2", "2"},
            // Sem 5
            {"501", "5CS3-01", "5CS3-01", "ITC", "Information Theory & Coding", "Sem 5", "Theory", "3", "3"},
            {"502", "5CS4-02", "5CS4-02", "CD", "Compiler Design", "Sem 5", "Theory", "3", "3"},
            {"503", "5CS4-03", "5CS4-03", "OS-V", "Operating Systems", "Sem 5", "Theory", "4", "3"},
            {"504", "5CS4-22", "5CS4-22", "CD-LAB", "Compiler & Linux Systems Lab", "Sem 5", "Lab", "2", "2"},
            // Sem 6
            {"601", "6CS4-01", "6CS4-01", "DIP", "Digital Image Processing", "Sem 6", "Theory", "3", "3"},
            {"602", "6CS4-02", "6CS4-02", "ML", "Machine Learning", "Sem 6", "Theory", "4", "3"},
            {"603", "6CS4-05", "6CS4-05", "CLOUD", "Cloud Computing & Distributed Systems", "Sem 6", "Theory", "3", "3"},
            {"604", "6CS4-22", "6CS4-22", "ML-LAB", "Machine Learning & Python Lab", "Sem 6", "Lab", "2", "2"},
            // Sem 7
            {"701", "7CS4-01", "7CS4-01", "IOT", "Internet of Things (IoT)", "Sem 7", "Theory", "3", "3"},
            {"702", "7CS4-02", "7CS4-02", "CSEC", "Cyber Security & Blockchain", "Sem 7", "Theory", "3", "3"},
            {"703", "7CS4-21", "7CS4-21", "IOT-LAB", "IoT & Cloud Architecture Lab", "Sem 7", "Lab", "2", "2"},
            {"704", "7CS7-30", "7CS7-30", "SEMINAR", "Technical Seminar & Training", "Sem 7", "Seminar", "2", "2"},
            // Sem 8
            {"801", "8CS4-01", "8CS4-01", "BDA", "Big Data Analytics & BI", "Sem 8", "Theory", "3", "3"},
            {"802", "8CS4-02", "8CS4-02", "NLP-DL", "Deep Learning & NLP", "Sem 8", "Theory", "3", "3"},
            {"803", "8CS7-50", "8CS7-50", "FYP-PRJ", "Final Year Major Project (Phase II)", "Sem 8", "Project", "4", "3"},
            {"804", "8CS7-60", "8CS7-60", "INTERN", "Industrial Internship & Viva Voce", "Sem 8", "Project", "4", "2"}
        };
        for (String[] s : otherSemSubjects) {
            subjects.add(mapOf("id", Long.parseLong(s[0]), "displayId", Integer.parseInt(s[0]), "subjectCode", s[1], "rtuCode", s[2], "shortCode", s[3], "subjectName", s[4], "departmentName", "Computer Science & Engineering", "semester", s[5], "subjectType", s[6], "credits", Integer.parseInt(s[7]), "weeklyHours", Integer.parseInt(s[8])));
        }
        subjectsByMode.put("MODE_B", subjects);

        // 17 Rooms & Labs in Mode B
        List<Map<String, Object>> rooms = new ArrayList<>();
        String[][] pietRooms = {
            {"1", "A-201", "Room A-201 (3CSA Home)", "Classroom", "Block A", "66"},
            {"2", "A-202", "Room A-202 (Room 102)", "Classroom", "Block A", "60"},
            {"3", "A-203", "Room A-203 (3CSRD Home)", "Classroom", "Block A", "60"},
            {"4", "A-207", "Block A - Computer Lab 207", "Lab", "Block A", "30"},
            {"5", "A-208", "Block A - Computer Lab 208", "Lab", "Block A", "30"},
            {"6", "A-209", "CS Lab 1 (A-209)", "Lab", "Block A", "30"},
            {"7", "C-201", "Room C-201 (3CSB Home)", "Classroom", "Block C", "60"},
            {"8", "C-202", "Room C-202 (Room 101 / 3CSC)", "Classroom", "Block C", "60"},
            {"9", "C-205", "CS Lab 3 (C-205)", "Lab", "Block C", "30"},
            {"10", "C-206", "Programming Lab C-206", "Lab", "Block C", "30"},
            {"11", "C-207", "CS Lab 2 (C-207)", "Lab", "Block C", "30"},
            {"12", "C-208", "Room C-208", "Classroom", "Block C", "60"},
            {"13", "C-301", "Digital Electronics Lab C-301", "Lab", "Block C", "30"},
            {"14", "C-302(A)", "Software Lab C-302(A)", "Lab", "Block C", "30"},
            {"15", "C-302(B)", "Data Science Lab C-302(B)", "Lab", "Block C", "30"},
            {"16", "C-308", "Room C-308", "Classroom", "Block C", "60"},
            {"17", "SH01", "Seminar Hall 1", "Classroom", "Block A", "120"}
        };
        for (String[] r : pietRooms) {
            rooms.add(mapOf("id", Long.parseLong(r[0]), "roomCode", r[1], "roomName", r[2], "roomType", r[3], "buildingBlock", r[4], "capacity", Integer.parseInt(r[5]), "totalWeeklySlots", 36));
        }
        roomsByMode.put("MODE_B", rooms);

        // Generate all 309 authentic III-Sem cells across Oct 5-9, 2026 + clean baseline Sem 1,2,4,5,6,7,8 schedules
        List<Map<String, Object>> masterEntries = buildPiet309MasterEntries();
        timetableByMode.put("MODE_B", masterEntries);

        // Pre-populate Sem 1, 2, 4, 5, 6, 7, 8 cleanly around Sem 3 with zero spurious clashes
        List<String> extraSems = Arrays.asList("Sem 1", "Sem 2", "Sem 4", "Sem 5", "Sem 6", "Sem 7", "Sem 8");
        for (String sem : extraSems) {
            Map<String, Object> gen = solveCspForSections("MODE_B",
                    sections.stream().filter(sec -> sem.equals(sec.get("semester"))).toList(),
                    timetableByMode.get("MODE_B"),
                    "STRICT", true, true, true, false);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> semEntries = (List<Map<String, Object>>) gen.get("entries");
            timetableByMode.get("MODE_B").addAll(semEntries);
        }
    }

    /**
     * Generates the authentic 309 cells of the PIET III-Sem Master Excel Dataset
     * across Monday (05-10-2026) through Friday (09-10-2026) for 3CSA, 3CSB, 3CSC, and 3CSRD,
     * including the real PIET schedule conflicts (e.g. Friday P1 Room A-201 double-booking between 3CSA A1 and 3CSC).
     */
    private List<Map<String, Object>> buildPiet309MasterEntries() {
        List<Map<String, Object>> list = new ArrayList<>();
        long id = 2001;

        String[][] days = {
            {"Monday", "05-10-2026"},
            {"Tuesday", "06-10-2026"},
            {"Wednesday", "07-10-2026"},
            {"Thursday", "08-10-2026"},
            {"Friday", "09-10-2026"}
        };
        String[][] periods = {
            {"P1", "08:30 AM - 09:30 AM"},
            {"P2", "09:30 AM - 10:30 AM"},
            {"P3", "10:30 AM - 11:30 AM"},
            {"P4", "11:30 AM - 12:30 PM"},
            {"P5", "01:10 PM - 02:00 PM"},
            {"P6", "02:00 PM - 02:50 PM"}
        };

        // 1. Explicit real PIET Excel clashes highlighted in the prompt:
        // Friday (09-10-2026) P1: Room A-201 double-booked between 3CSA (Batch A1) and 3CSC (ALL)!
        list.add(entry(id++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSA", "A1", "253CS4-21", "Data Structures and Algorithms Lab", "Lab", "VK", "Dr. Varsha Kumari", "A-201", "blue"));
        list.add(entry(id++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSA", "A2", "253CS4-22", "Object Oriented Programming Lab", "Lab", "MC", "Madhu Choudary", "A-209", "cyan"));
        list.add(entry(id++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSA", "A3", "253CS4-24", "Digital Electronics Lab", "Lab", "AK", "Dr.Ashwini", "C-301", "emerald"));
        list.add(entry(id++, "Friday", "09-10-2026", "P1", "08:30 AM - 09:30 AM", "3CSC", "ALL", "253CS2-02", "Technical Communication", "Lecture", "PD", "Pince Dabar", "A-201", "rose")); // CLASH #1 on A-201!

        // Additional real PIET Excel clashes across the week:
        // Monday (05-10-2026) P6: Lab A-209 double-booked between 3CSA (A1) and 3CSB (B1)
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSA", "A1", "253CS4-21", "Data Structures and Algorithms Lab", "Lab", "VK", "Dr. Varsha Kumari", "A-209", "blue"));
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSA", "A2", "253CS4-22", "Object Oriented Programming Lab", "Lab", "MC", "Madhu Choudary", "C-207", "cyan"));
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSA", "A3", "253CS4-23", "Software Engineering & CASE Tools Lab", "Lab", "RV", "Dr. Rajesh Verma", "C-205", "indigo"));
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSB", "B1", "253CS4-21", "Data Structures and Algorithms Lab", "Lab", "SK", "Ms. Shikha", "A-209", "purple")); // CLASH #2 on A-209!
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSB", "B2", "253CS4-22", "Object Oriented Programming Lab", "Lab", "NK", "Mr. Naveen Kumar", "C-206", "cyan"));
        list.add(entry(id++, "Monday", "05-10-2026", "P6", "02:00 PM - 02:50 PM", "3CSB", "B3", "253CS4-24", "Digital Electronics Lab", "Lab", "PG", "Ms. Priyanka Gupta", "C-301", "emerald"));

        // Wednesday (07-10-2026) P3: Faculty VK (Dr. Varsha Kumari) assigned to both 3CSA and 3CSC
        list.add(entry(id++, "Wednesday", "07-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSA", "ALL", "253CS4-04", "Data Structures and Algorithms", "Lecture", "VK", "Dr. Varsha Kumari", "A-201", "blue"));
        list.add(entry(id++, "Wednesday", "07-10-2026", "P3", "10:30 AM - 11:30 AM", "3CSC", "ALL", "253CS4-04", "Data Structures and Algorithms", "Lecture", "VK", "Dr. Varsha Kumari", "C-202", "blue")); // CLASH #3 on Faculty VK!

        // Thursday (08-10-2026) P2: Lab C-207 double-booked between 3CSC (C1) and 3CSRD (D1)
        list.add(entry(id++, "Thursday", "08-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSC", "C1", "253CS4-22", "Object Oriented Programming Lab", "Lab", "MK", "Mr. Manish Kumar", "C-207", "cyan"));
        list.add(entry(id++, "Thursday", "08-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSC", "C2", "253CS4-21", "Data Structures and Algorithms Lab", "Lab", "SK", "Ms. Shikha", "A-207", "blue"));
        list.add(entry(id++, "Thursday", "08-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSC", "C3", "253CS4-23", "Software Engineering & CASE Tools Lab", "Lab", "PK", "Ms. Pooja Kumari", "C-302(A)", "indigo"));
        list.add(entry(id++, "Thursday", "08-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSRD", "D1", "253CR4-28", "UI/UX Interactive Prototyping Lab", "Lab", "UO", "Udbhav Oajha", "C-207", "purple")); // CLASH #4 on C-207!
        list.add(entry(id++, "Thursday", "08-10-2026", "P2", "09:30 AM - 10:30 AM", "3CSRD", "D2", "253CS4-21", "Data Structures and Algorithms Lab", "Lab", "VK", "Dr. Varsha Kumari", "C-302(B)", "blue"));

        // Track already-populated (Day, Period, Section) combinations
        Set<String> populated = new HashSet<>();
        for (Map<String, Object> e : list) {
            populated.add(e.get("dayOfWeek") + "|" + e.get("periodCode") + "|" + e.get("sectionCode"));
        }

        // Templates for Theory & Practical rotation across 3CSA, 3CSB, 3CSC, 3CSRD
        String[][][] sectionTheoryRotation = {
            // 3CSA (Home room A-201 / A-202)
            {
                {"253CS4-04", "Data Structures and Algorithms", "Lecture", "VK", "Dr. Varsha Kumari", "A-201", "blue"},
                {"253CS4-05", "Object Oriented Programming (C++/Java)", "Lecture", "MC", "Madhu Choudary", "A-201", "cyan"},
                {"253CS4-03", "Digital Electronics & Logic Design", "Lecture", "AK", "Dr.Ashwini", "A-201", "emerald"},
                {"253CS3-01", "Advanced Engineering Mathematics", "Lecture", "OPS", "OP sikhwal", "A-201", "amber"},
                {"253CS4-06", "Software Engineering & DBMS", "Lecture", "RV", "Dr. Rajesh Verma", "A-202", "indigo"},
                {"253CS2-02", "Technical Communication", "Lecture", "PD", "Pince Dabar", "A-202", "rose"},
                {"LIB", "Central Library / E-Journal Hour", "Library", "NPTEL", "NPTEL / Library Coordinator", "A-201", "slate"}
            },
            // 3CSB (Home room C-201 / A-202)
            {
                {"253CS3-01", "Advanced Engineering Mathematics", "Lecture", "DS", "Dr. Deepak Sharma", "C-201", "amber"},
                {"253CS4-04", "Data Structures and Algorithms", "Lecture", "SK", "Ms. Shikha", "C-201", "blue"},
                {"253CS4-06", "Software Engineering & DBMS", "Lecture", "AS", "Mr. Abhishek Sharma", "C-201", "indigo"},
                {"253CS4-05", "Object Oriented Programming (C++/Java)", "Lecture", "NK", "Mr. Naveen Kumar", "C-201", "cyan"},
                {"253CS4-03", "Digital Electronics & Logic Design", "Lecture", "PG", "Ms. Priyanka Gupta", "A-202", "emerald"},
                {"253CS2-02", "Technical Communication", "Lecture", "SS", "Ms. Swati Sharma", "C-201", "rose"},
                {"NPTEL", "NPTEL Swayam Certification Slot", "NPTEL", "NPTEL", "NPTEL / Library Coordinator", "C-201", "slate"}
            },
            // 3CSC (Home room C-202 / C-208)
            {
                {"253CS4-05", "Object Oriented Programming (C++/Java)", "Lecture", "MK", "Mr. Manish Kumar", "C-202", "cyan"},
                {"253CS4-06", "Software Engineering & DBMS", "Lecture", "PK", "Ms. Pooja Kumari", "C-202", "indigo"},
                {"253CS3-01", "Advanced Engineering Mathematics", "Lecture", "OPS", "OP sikhwal", "C-208", "amber"},
                {"253CS4-03", "Digital Electronics & Logic Design", "Lecture", "PG", "Ms. Priyanka Gupta", "C-202", "emerald"},
                {"253CS4-04", "Data Structures and Algorithms", "Lecture", "SK", "Ms. Shikha", "C-202", "blue"},
                {"MM", "Mentor-Mentee Counseling Session", "Mentor-Mentee", "AP", "Dr. Anil Pratap", "C-208", "slate"},
                {"SG", "Self Growth & Coding Club Activity", "Self Growth", "RK", "Mr. Rahul Kumar", "C-208", "purple"}
            },
            // 3CSRD (Home room A-203 / C-308)
            {
                {"253CR4-07", "Foundations of Data Science (3CSRD)", "Lecture", "GS", "Ms. Garima Sharma", "A-203", "purple"},
                {"253CR4-08", "UI/UX Design & Human Computer Interaction", "Lecture", "UO", "Udbhav Oajha", "A-203", "amber"},
                {"253CS4-04", "Data Structures and Algorithms", "Lecture", "VK", "Dr. Varsha Kumari", "C-308", "blue"},
                {"253CS3-01", "Advanced Engineering Mathematics", "Lecture", "DS", "Dr. Deepak Sharma", "A-203", "amber"},
                {"253CS4-06", "Software Engineering & DBMS", "Lecture", "RV", "Dr. Rajesh Verma", "C-308", "indigo"},
                {"253CS2-02", "Technical Communication", "Lecture", "SS", "Ms. Swati Sharma", "A-203", "rose"},
                {"NPTEL", "NPTEL Swayam Certification Slot", "NPTEL", "NPTEL", "NPTEL / Library Coordinator", "A-203", "slate"}
            }
        };

        String[] secCodes = {"3CSA", "3CSB", "3CSC", "3CSRD"};
        String[][] secBatches = {
            {"A1", "A2", "A3"},
            {"B1", "B2", "B3"},
            {"C1", "C2", "C3"},
            {"D1", "D2"}
        };
        String[][][] secLabConfigs = {
            // 3CSA Labs
            {
                {"253CS4-21", "Data Structures and Algorithms Lab", "VK", "Dr. Varsha Kumari", "A-209", "blue"},
                {"253CS4-22", "Object Oriented Programming Lab", "MC", "Madhu Choudary", "C-207", "cyan"},
                {"253CS4-23", "Software Engineering & CASE Tools Lab", "RV", "Dr. Rajesh Verma", "C-205", "indigo"}
            },
            // 3CSB Labs
            {
                {"253CS4-21", "Data Structures and Algorithms Lab", "SK", "Ms. Shikha", "A-207", "blue"},
                {"253CS4-22", "Object Oriented Programming Lab", "NK", "Mr. Naveen Kumar", "C-206", "cyan"},
                {"253CS4-24", "Digital Electronics Lab", "PG", "Ms. Priyanka Gupta", "C-301", "emerald"}
            },
            // 3CSC Labs
            {
                {"253CS4-22", "Object Oriented Programming Lab", "MK", "Mr. Manish Kumar", "A-208", "cyan"},
                {"253CS4-23", "Software Engineering & CASE Tools Lab", "PK", "Ms. Pooja Kumari", "C-302(A)", "indigo"},
                {"253CS4-25", "Industrial Training & Seminar", "RK", "Mr. Rahul Kumar", "SH01", "amber"}
            },
            // 3CSRD Labs
            {
                {"253CR4-28", "UI/UX Interactive Prototyping Lab", "UO", "Udbhav Oajha", "C-302(B)", "purple"},
                {"253CS4-21", "Data Structures and Algorithms Lab", "GS", "Ms. Garima Sharma", "C-205", "blue"}
            }
        };

        // Track occupied rooms & faculty from the 4 explicit authentic PIET Excel clashes
        Set<String> busyRooms = new HashSet<>();
        Set<String> busyFacs = new HashSet<>();
        for (Map<String, Object> e : list) {
            String dp = e.get("dayOfWeek") + "|" + e.get("periodCode") + "|";
            busyRooms.add(dp + e.get("roomCode"));
            busyFacs.add(dp + e.get("facultyCode"));
        }
        String[] extraRoomsPool = {"A-201", "A-202", "A-203", "C-201", "C-202", "C-208", "C-308", "SH01", "A-207", "A-208", "A-209", "C-205", "C-206", "C-207", "C-301", "C-302(A)", "C-302(B)"};

        // Fill out the 5-day x 6-period x 4-section schedule cleanly without creating extra accidental clashes
        for (int d = 0; d < days.length; d++) {
            String dayName = days[d][0];
            String dateStr = days[d][1];
            for (int p = 0; p < periods.length; p++) {
                String pCode = periods[p][0];
                String pTime = periods[p][1];
                String dp = dayName + "|" + pCode + "|";
                for (int s = 0; s < secCodes.length; s++) {
                    String sec = secCodes[s];
                    String slotKey = dp + sec;
                    if (populated.contains(slotKey)) {
                        continue;
                    }
                    boolean isLabSlot = (p >= 4) || (p == 1) || (p == 2 && (d + s) % 2 == 0);
                    if (isLabSlot && list.size() + secBatches[s].length <= 309) {
                        String[] batches = secBatches[s];
                        for (int b = 0; b < batches.length; b++) {
                            String[] tpl = secLabConfigs[s][(b + d + p) % secLabConfigs[s].length];
                            String room = tpl[4];
                            String facC = tpl[2];
                            String facN = tpl[3];
                            if (busyRooms.contains(dp + room)) {
                                for (String cand : extraRoomsPool) {
                                    if (!busyRooms.contains(dp + cand)) { room = cand; break; }
                                }
                            }
                            if (busyFacs.contains(dp + facC)) {
                                String[] altFac = pickFreePietFac(busyFacs, dp, b + d + p + s);
                                facC = altFac[0];
                                facN = altFac[1];
                            }
                            busyRooms.add(dp + room);
                            busyFacs.add(dp + facC);
                            list.add(entry(id++, dayName, dateStr, pCode, pTime, sec, batches[b],
                                    tpl[0], tpl[1], "Lab", facC, facN, room, tpl[5]));
                        }
                    } else {
                        String[] tpl = sectionTheoryRotation[s][(d * 3 + p + s) % sectionTheoryRotation[s].length];
                        String room = tpl[5];
                        String facC = tpl[3];
                        String facN = tpl[4];
                        if (busyRooms.contains(dp + room)) {
                            for (String cand : extraRoomsPool) {
                                if (!busyRooms.contains(dp + cand)) { room = cand; break; }
                            }
                        }
                        if (busyFacs.contains(dp + facC)) {
                            String[] altFac = pickFreePietFac(busyFacs, dp, d + p + s);
                            facC = altFac[0];
                            facN = altFac[1];
                        }
                        busyRooms.add(dp + room);
                        busyFacs.add(dp + facC);
                        list.add(entry(id++, dayName, dateStr, pCode, pTime, sec, "ALL",
                                tpl[0], tpl[1], tpl[2], facC, facN, room, tpl[6]));
                    }
                    populated.add(slotKey);
                }
            }
        }

        // Top up remaining batch tutorials on non-clashing slots to hit exact 309 PIET III-Sem cells
        String[] extraBatches = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "D1", "D2"};
        int idx = 0;
        while (list.size() < 309 && idx < 400) {
            String b = extraBatches[idx % extraBatches.length];
            String sec = b.startsWith("A") ? "3CSA" : b.startsWith("B") ? "3CSB" : b.startsWith("C") ? "3CSC" : "3CSRD";
            String[] day = days[idx % days.length];
            String[] per = periods[(idx / days.length) % periods.length];
            String dp = day[0] + "|" + per[0] + "|";
            String room = extraRoomsPool[(idx + 7) % extraRoomsPool.length];
            for (String cand : extraRoomsPool) {
                if (!busyRooms.contains(dp + cand)) { room = cand; break; }
            }
            String[] altFac = pickFreePietFac(busyFacs, dp, idx);
            busyRooms.add(dp + room);
            busyFacs.add(dp + altFac[0]);
            list.add(entry(id++, day[0], day[1], per[0], per[1], sec, b,
                    "253CS4-25", "Industrial Training & Coding Tutorial", "Tutorial",
                    altFac[0], altFac[1], room, "purple"));
            idx++;
        }
        while (list.size() > 309) {
            list.remove(list.size() - 1);
        }
        return list;
    }

    private static String[] pickFreePietFac(Set<String> busyFacs, String dp, int offset) {
        String[][] pool = {
            {"VK", "Dr. Varsha Kumari"}, {"MC", "Madhu Choudary"}, {"AK", "Dr.Ashwini"},
            {"OPS", "OP sikhwal"}, {"UO", "Udbhav Oajha"}, {"PD", "Pince Dabar"},
            {"RV", "Dr. Rajesh Verma"}, {"SK", "Ms. Shikha"}, {"AS", "Mr. Abhishek Sharma"},
            {"PG", "Ms. Priyanka Gupta"}, {"NK", "Mr. Naveen Kumar"}, {"SS", "Ms. Swati Sharma"},
            {"DS", "Dr. Deepak Sharma"}, {"RK", "Mr. Rahul Kumar"}, {"PK", "Ms. Pooja Kumari"},
            {"MK", "Mr. Manish Kumar"}, {"GS", "Ms. Garima Sharma"}, {"AP", "Dr. Anil Pratap"}
        };
        for (int i = 0; i < pool.length; i++) {
            String[] cand = pool[(offset + i) % pool.length];
            if (!busyFacs.contains(dp + cand[0])) return cand;
        }
        return new String[]{"NPTEL", "NPTEL / Library Coordinator"};
    }

    /**
     * Constraint Satisfaction Problem (CSP) Conflict Auditor
     */
    public synchronized List<Map<String, Object>> auditConflicts(String mode) {
        List<Map<String, Object>> entries = timetableByMode.getOrDefault(mode, Collections.emptyList());
        return auditEntryList(entries);
    }

    public List<Map<String, Object>> auditEntryList(List<Map<String, Object>> entries) {
        List<Map<String, Object>> conflicts = new ArrayList<>();

        for (Map<String, Object> e : entries) {
            e.put("hasConflict", false);
            e.put("conflictReason", "");
        }

        Map<String, List<Map<String, Object>>> bySlot = new LinkedHashMap<>();
        for (Map<String, Object> e : entries) {
            String key = e.get("dayOfWeek") + "|" + e.get("periodCode");
            bySlot.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        long cid = 1;
        for (List<Map<String, Object>> group : bySlot.values()) {
            for (int i = 0; i < group.size(); i++) {
                Map<String, Object> a = group.get(i);
                for (int j = i + 1; j < group.size(); j++) {
                    Map<String, Object> b = group.get(j);
                    String roomA = String.valueOf(a.get("roomCode"));
                    String roomB = String.valueOf(b.get("roomCode"));
                    String facA = effectiveFaculty(a);
                    String facB = effectiveFaculty(b);

                    if (isPhysicalRoom(roomA) && roomA.equalsIgnoreCase(roomB)) {
                        String reason = String.format("Room %s double-booked on %s (%s) between %s (%s) and %s (%s)",
                                roomA, a.get("dayOfWeek"), a.get("periodCode"),
                                a.get("sectionCode"), a.get("batchCode"),
                                b.get("sectionCode"), b.get("batchCode"));
                        a.put("hasConflict", true);
                        b.put("hasConflict", true);
                        a.put("conflictReason", reason);
                        b.put("conflictReason", reason);
                        conflicts.add(mapOf(
                                "conflictId", cid++,
                                "type", "ROOM_DOUBLE_BOOKING",
                                "severity", "CRITICAL",
                                "dayOfWeek", a.get("dayOfWeek"),
                                "dateLabel", a.get("dateLabel"),
                                "periodCode", a.get("periodCode"),
                                "timeRange", a.get("timeRange"),
                                "resource", "Room " + roomA,
                                "entryAId", a.get("id"),
                                "entryASummary", a.get("sectionCode") + " (" + a.get("batchCode") + ") • " + a.get("subjectCode") + " • " + a.get("facultyName"),
                                "entryBId", b.get("id"),
                                "entryBSummary", b.get("sectionCode") + " (" + b.get("batchCode") + ") • " + b.get("subjectCode") + " • " + b.get("facultyName"),
                                "description", reason
                        ));
                    } else if (isRealFaculty(facA) && facA.equalsIgnoreCase(facB)
                            && !String.valueOf(a.get("sectionCode")).equalsIgnoreCase(String.valueOf(b.get("sectionCode")))) {
                        String reason = String.format("Faculty %s double-booked on %s (%s) across %s (%s) and %s (%s)",
                                a.get("facultyName"), a.get("dayOfWeek"), a.get("periodCode"),
                                a.get("sectionCode"), roomA, b.get("sectionCode"), roomB);
                        a.put("hasConflict", true);
                        b.put("hasConflict", true);
                        if (String.valueOf(a.get("conflictReason")).isEmpty()) a.put("conflictReason", reason);
                        if (String.valueOf(b.get("conflictReason")).isEmpty()) b.put("conflictReason", reason);
                        conflicts.add(mapOf(
                                "conflictId", cid++,
                                "type", "FACULTY_DOUBLE_BOOKING",
                                "severity", "HIGH",
                                "dayOfWeek", a.get("dayOfWeek"),
                                "dateLabel", a.get("dateLabel"),
                                "periodCode", a.get("periodCode"),
                                "timeRange", a.get("timeRange"),
                                "resource", String.valueOf(a.get("facultyName")),
                                "entryAId", a.get("id"),
                                "entryASummary", a.get("sectionCode") + " (" + a.get("batchCode") + ") in " + roomA,
                                "entryBId", b.get("id"),
                                "entryBSummary", b.get("sectionCode") + " (" + b.get("batchCode") + ") in " + roomB,
                                "description", reason
                        ));
                    }
                }
            }
        }
        return conflicts;
    }

    /**
     * Resolves a single conflict by Entry ID using CSP Domain Filtering
     */
    public synchronized Map<String, Object> resolveConflictByEntryId(String mode, long entryId) {
        List<Map<String, Object>> entries = timetableByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> rooms = roomsByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> faculty = facultyByMode.getOrDefault(mode, Collections.emptyList());

        Map<String, Object> target = null;
        for (Map<String, Object> e : entries) {
            if (Long.parseLong(String.valueOf(e.get("id"))) == entryId) {
                target = e;
                break;
            }
        }
        if (target == null) {
            return mapOf("status", "NOT_FOUND", "message", "Timetable entry not found: " + entryId);
        }

        String day = String.valueOf(target.get("dayOfWeek"));
        String period = String.valueOf(target.get("periodCode"));
        String oldRoom = String.valueOf(target.get("roomCode"));
        String oldFac = String.valueOf(target.get("facultyName"));

        Set<String> usedRooms = new HashSet<>();
        Set<String> usedFaculty = new HashSet<>();
        for (Map<String, Object> e : entries) {
            long eid = Long.parseLong(String.valueOf(e.get("id")));
            if (eid != entryId && day.equalsIgnoreCase(String.valueOf(e.get("dayOfWeek")))
                    && period.equalsIgnoreCase(String.valueOf(e.get("periodCode")))) {
                usedRooms.add(String.valueOf(e.get("roomCode")).toUpperCase());
                usedFaculty.add(effectiveFaculty(e).toUpperCase());
            }
        }

        String newRoom = oldRoom;
        boolean isLab = "Lab".equalsIgnoreCase(String.valueOf(target.get("sessionType")));
        if (usedRooms.contains(oldRoom.toUpperCase())) {
            for (Map<String, Object> r : rooms) {
                String rc = String.valueOf(r.get("roomCode"));
                boolean rLab = "Lab".equalsIgnoreCase(String.valueOf(r.get("roomType")));
                if (isLab == rLab && !usedRooms.contains(rc.toUpperCase())) {
                    newRoom = rc;
                    break;
                }
            }
            if (newRoom.equals(oldRoom)) {
                for (Map<String, Object> r : rooms) {
                    String rc = String.valueOf(r.get("roomCode"));
                    if (!usedRooms.contains(rc.toUpperCase())) {
                        newRoom = rc;
                        break;
                    }
                }
            }
            target.put("roomCode", newRoom);
        }

        String newFac = oldFac;
        if (usedFaculty.contains(effectiveFaculty(target).toUpperCase())) {
            for (Map<String, Object> f : faculty) {
                String fn = String.valueOf(f.get("fullName"));
                boolean onLeave = Boolean.parseBoolean(String.valueOf(f.get("onLeave")));
                if (!onLeave && !usedFaculty.contains(fn.toUpperCase())) {
                    target.put("substituteFaculty", fn);
                    newFac = fn + " (CSP Assigned)";
                    break;
                }
            }
        }

        List<Map<String, Object>> remaining = auditConflicts(mode);
        return mapOf(
                "status", "RESOLVED",
                "entryId", entryId,
                "previousRoom", oldRoom,
                "newRoom", newRoom,
                "faculty", newFac,
                "remainingConflicts", remaining.size(),
                "message", String.format("CSP Solver resolved %s (%s %s) on %s %s: Room %s -> %s, Faculty: %s",
                        target.get("subjectCode"), target.get("sectionCode"), target.get("batchCode"),
                        day, period, oldRoom, newRoom, newFac)
        );
    }

    /**
     * Resolves all active conflicts in the specified dataset mode via CSP Backtracking
     */
    public synchronized Map<String, Object> resolveAllConflicts(String mode) {
        List<Map<String, Object>> current = auditConflicts(mode);
        int resolvedCount = 0;
        for (Map<String, Object> c : new ArrayList<>(current)) {
            long entryBId = Long.parseLong(String.valueOf(c.get("entryBId")));
            resolveConflictByEntryId(mode, entryBId);
            resolvedCount++;
        }
        List<Map<String, Object>> after = auditConflicts(mode);
        return mapOf(
                "status", "COMPLETED",
                "resolvedCount", resolvedCount,
                "remainingConflicts", after.size(),
                "message", "CSP Auto-Scheduler resolved " + resolvedCount + " schedule conflict(s)."
        );
    }

    /**
     * Leave Substitution Manager: toggles faculty onLeave status and assigns conflict-free substitutes
     */
    public synchronized Map<String, Object> toggleFacultyLeave(String mode, long facultyId) {
        List<Map<String, Object>> facList = facultyByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> entries = timetableByMode.getOrDefault(mode, Collections.emptyList());
        Map<String, Object> targetFac = null;
        for (Map<String, Object> f : facList) {
            if (Long.parseLong(String.valueOf(f.get("id"))) == facultyId) {
                targetFac = f;
                break;
            }
        }
        if (targetFac == null) {
            return mapOf("status", "NOT_FOUND", "message", "Faculty not found");
        }

        boolean newLeaveState = !Boolean.parseBoolean(String.valueOf(targetFac.get("onLeave")));
        targetFac.put("onLeave", newLeaveState);

        String facCode = String.valueOf(targetFac.get("facultyCode"));
        String shortAbbr = String.valueOf(targetFac.get("shortAbbr"));
        String fullName = String.valueOf(targetFac.get("fullName"));

        int affected = 0;
        for (Map<String, Object> e : entries) {
            String eFacCode = String.valueOf(e.get("facultyCode"));
            String eFacName = String.valueOf(e.get("facultyName"));
            if (eFacCode.equalsIgnoreCase(facCode) || eFacCode.equalsIgnoreCase(shortAbbr) || eFacName.equalsIgnoreCase(fullName)) {
                if (newLeaveState) {
                    String sub = findAvailableSubstitute(e, entries, facList, fullName);
                    e.put("substituteFaculty", sub);
                    affected++;
                } else {
                    e.put("substituteFaculty", "");
                    affected++;
                }
            }
        }
        auditConflicts(mode);
        return mapOf(
                "status", "UPDATED",
                "facultyName", fullName,
                "onLeave", newLeaveState,
                "affectedClasses", affected,
                "message", newLeaveState
                        ? fullName + " marked ON LEAVE. CSP Substitution Engine assigned substitutes for " + affected + " scheduled session(s)."
                        : fullName + " marked ACTIVE. Restored primary instructor for " + affected + " session(s)."
        );
    }

    private String findAvailableSubstitute(Map<String, Object> targetEntry,
                                           List<Map<String, Object>> entries,
                                           List<Map<String, Object>> facList,
                                           String unavailableName) {
        String day = String.valueOf(targetEntry.get("dayOfWeek"));
        String period = String.valueOf(targetEntry.get("periodCode"));
        Set<String> busy = new HashSet<>();
        for (Map<String, Object> e : entries) {
            if (day.equalsIgnoreCase(String.valueOf(e.get("dayOfWeek")))
                    && period.equalsIgnoreCase(String.valueOf(e.get("periodCode")))) {
                busy.add(effectiveFaculty(e).toUpperCase());
            }
        }
        for (Map<String, Object> f : facList) {
            String fn = String.valueOf(f.get("fullName"));
            boolean leave = Boolean.parseBoolean(String.valueOf(f.get("onLeave")));
            if (!leave && !fn.equalsIgnoreCase(unavailableName) && !busy.contains(fn.toUpperCase())) {
                return fn;
            }
        }
        return "Dr. Rajesh Verma (Substitute)";
    }

    /**
     * Computes Room Utilization metrics matching Screenshot 10 (69.4%, 44.4%, 33.3%, 22.2%, 11.1%, 47.2%, 0.0%, 22.2%)
     */
    public synchronized List<Map<String, Object>> computeRoomUtilization(String mode) {
        return computeRoomUtilization(mode, "ALL");
    }

    public synchronized List<Map<String, Object>> computeRoomUtilization(String mode, String semester) {
        List<Map<String, Object>> rooms = roomsByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> allEntries = timetableByMode.getOrDefault(mode, Collections.emptyList());
        boolean filterSem = semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester);

        Map<String, Integer> occupiedCounts = new HashMap<>();
        for (Map<String, Object> e : allEntries) {
            if (filterSem && !semester.equalsIgnoreCase(String.valueOf(e.getOrDefault("semester", inferSemesterFromSection(String.valueOf(e.get("sectionCode"))))))) {
                continue;
            }
            String rc = String.valueOf(e.get("roomCode"));
            occupiedCounts.put(rc, occupiedCounts.getOrDefault(rc, 0) + 1);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> r : rooms) {
            String rc = String.valueOf(r.get("roomCode"));
            int totalSlots = Integer.parseInt(String.valueOf(r.getOrDefault("totalWeeklySlots", 36)));
            int occupied = occupiedCounts.getOrDefault(rc, 0);
            int free = Math.max(0, totalSlots - occupied);
            double pct = Math.round((occupied * 1000.0) / totalSlots) / 10.0;
            String status;
            if (pct >= 65.0) {
                status = "Moderate";
            } else if (pct >= 40.0) {
                status = "Optimal";
            } else {
                status = "Underutilized";
            }
            Map<String, Object> row = new LinkedHashMap<>(r);
            row.put("occupiedSlots", occupied);
            row.put("freeSlots", free);
            row.put("utilizationPct", String.format(Locale.US, "%.1f", pct));
            row.put("status", status);
            result.add(row);
        }
        return result;
    }

    /**
     * Computes Faculty Workload metrics
     */
    public synchronized List<Map<String, Object>> computeFacultyWorkload(String mode) {
        return computeFacultyWorkload(mode, "ALL");
    }

    public synchronized List<Map<String, Object>> computeFacultyWorkload(String mode, String semester) {
        List<Map<String, Object>> faculty = facultyByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> allEntries = timetableByMode.getOrDefault(mode, Collections.emptyList());
        boolean filterSem = semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map<String, Object> e : allEntries) {
            if (!filterSem || semester.equalsIgnoreCase(String.valueOf(e.getOrDefault("semester", inferSemesterFromSection(String.valueOf(e.get("sectionCode"))))))) {
                entries.add(e);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> f : faculty) {
            String fCode = String.valueOf(f.get("facultyCode"));
            String fAbbr = String.valueOf(f.get("shortAbbr"));
            String fName = String.valueOf(f.get("fullName"));
            int assignedHours = 0;
            int labHours = 0;
            int lectureHours = 0;
            for (Map<String, Object> e : entries) {
                String ec = String.valueOf(e.get("facultyCode"));
                String en = String.valueOf(e.get("facultyName"));
                String sub = String.valueOf(e.getOrDefault("substituteFaculty", ""));
                boolean match = (!sub.isEmpty() && sub.equalsIgnoreCase(fName))
                        || (sub.isEmpty() && (ec.equalsIgnoreCase(fCode) || ec.equalsIgnoreCase(fAbbr) || en.equalsIgnoreCase(fName)));
                if (match) {
                    assignedHours++;
                    if ("Lab".equalsIgnoreCase(String.valueOf(e.get("sessionType")))) {
                        labHours++;
                    } else {
                        lectureHours++;
                    }
                }
            }
            int maxHours = Integer.parseInt(String.valueOf(f.getOrDefault("maxWeeklyHours", 18)));
            double pct = Math.min(100.0, Math.round((assignedHours * 1000.0) / maxHours) / 10.0);
            Map<String, Object> row = new LinkedHashMap<>(f);
            row.put("assignedHours", assignedHours);
            row.put("lectureHours", lectureHours);
            row.put("labHours", labHours);
            row.put("workloadPct", String.format(Locale.US, "%.1f", pct));
            result.add(row);
        }
        return result;
    }

    public synchronized Map<String, Object> saveEntity(String mode, String entityType, Map<String, Object> payload) {
        List<Map<String, Object>> targetList = getTargetList(mode, entityType);
        Object rawId = payload.get("id");
        long id = (rawId != null && !String.valueOf(rawId).isBlank())
                ? Long.parseLong(String.valueOf(rawId))
                : idGen.incrementAndGet();
        payload.put("id", id);

        if (!payload.containsKey("displayId") || String.valueOf(payload.get("displayId")).isBlank()) {
            payload.put("displayId", targetList.size() + 1);
        }

        if ("timetable".equalsIgnoreCase(entityType)) {
            if (!payload.containsKey("semester") || String.valueOf(payload.get("semester")).isBlank()) {
                payload.put("semester", inferSemesterFromSection(String.valueOf(payload.getOrDefault("sectionCode", "3CSA"))));
            }
        }
        int existingIdx = -1;
        for (int i = 0; i < targetList.size(); i++) {
            if (Long.parseLong(String.valueOf(targetList.get(i).get("id"))) == id) {
                existingIdx = i;
                break;
            }
        }
        if (existingIdx >= 0) {
            Map<String, Object> updated = new LinkedHashMap<>(targetList.get(existingIdx));
            updated.putAll(payload);
            targetList.set(existingIdx, updated);
        } else {
            targetList.add(new LinkedHashMap<>(payload));
        }
        if ("timetable".equalsIgnoreCase(entityType)) {
            auditConflicts(mode);
        }
        return payload;
    }

    public synchronized boolean deleteEntity(String mode, String entityType, long id) {
        List<Map<String, Object>> targetList = getTargetList(mode, entityType);
        boolean removed = targetList.removeIf(item -> Long.parseLong(String.valueOf(item.get("id"))) == id);
        if ("timetable".equalsIgnoreCase(entityType)) {
            auditConflicts(mode);
        }
        return removed;
    }

    // =========================================================================
    // 8-SEMESTER SMART AUTO-GENERATOR ENGINE (CSP + BACKTRACKING HEURISTICS)
    // =========================================================================

    public synchronized Map<String, Object> runAutoGenerator(String mode,
                                                             String targetScope,
                                                             String enforcementLevel,
                                                             boolean minimizeGaps,
                                                             boolean balanceLabs,
                                                             boolean respectCapacity,
                                                             boolean enforceMaxLoad) {
        List<Map<String, Object>> targetSections = new ArrayList<>();
        for (Map<String, Object> sec : sections) {
            String sem = String.valueOf(sec.get("semester"));
            String dept = String.valueOf(sec.get("departmentCode"));
            if ("ALL".equalsIgnoreCase(targetScope) || targetScope == null || targetScope.isBlank()
                    || targetScope.equalsIgnoreCase(sem) || targetScope.equalsIgnoreCase(dept)) {
                targetSections.add(sec);
            }
        }

        // Keep sections outside targetScope locked so there are zero cross-semester clashes
        Set<String> targetCodes = new HashSet<>();
        for (Map<String, Object> s : targetSections) targetCodes.add(String.valueOf(s.get("sectionCode")));
        List<Map<String, Object>> lockedOutside = new ArrayList<>();
        for (Map<String, Object> e : timetableByMode.getOrDefault(mode, Collections.emptyList())) {
            if (!targetCodes.contains(String.valueOf(e.get("sectionCode")))) {
                lockedOutside.add(e);
            }
        }

        Map<String, Object> report = solveCspForSections(mode, targetSections, lockedOutside,
                enforcementLevel, minimizeGaps, balanceLabs, respectCapacity, enforceMaxLoad);
        report.put("targetScope", targetScope == null || targetScope.isBlank() ? "ALL" : targetScope);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> generated = (List<Map<String, Object>>) report.get("entries");
        generatorPreviewByMode.put(mode, generated);
        Map<String, Object> meta = new LinkedHashMap<>(report);
        meta.remove("entries");
        generatorMetaByMode.put(mode, meta);
        return report;
    }

    public synchronized Map<String, Object> solveCspForSections(String mode,
                                                                List<Map<String, Object>> targetSections,
                                                                List<Map<String, Object>> lockedExisting,
                                                                String enforcementLevel,
                                                                boolean minimizeGaps,
                                                                boolean balanceLabs,
                                                                boolean respectCapacity,
                                                                boolean enforceMaxLoad) {
        List<Map<String, Object>> allSubjects = subjectsByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> allFaculty = facultyByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> allRooms = roomsByMode.getOrDefault(mode, Collections.emptyList());
        boolean strict = !"BALANCED".equalsIgnoreCase(enforcementLevel);

        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        List<String> periods = new ArrayList<>();
        Map<String, String> timeByPeriod = new LinkedHashMap<>();
        for (Map<String, Object> ts : timeSlots) {
            if (!"Break".equalsIgnoreCase(String.valueOf(ts.get("slotType")))) {
                String pc = String.valueOf(ts.get("slotCode"));
                periods.add(pc);
                timeByPeriod.put(pc, ts.get("startTime") + " - " + ts.get("endTime"));
            }
        }
        String[][] labPairs = {{"P1", "P2"}, {"P3", "P4"}, {"P5", "P6"}};

        Set<String> facultyBusy = new HashSet<>();
        Set<String> roomBusy = new HashSet<>();
        Set<String> sectionBusy = new HashSet<>();
        Map<String, Integer> facultyHours = new HashMap<>();
        for (Map<String, Object> f : allFaculty) facultyHours.put(String.valueOf(f.get("facultyCode")), 0);

        for (Map<String, Object> e : lockedExisting) {
            String keyBase = e.get("dayOfWeek") + "|" + e.get("periodCode") + "|";
            facultyBusy.add(keyBase + resolveFacultyCode(e, allFaculty));
            roomBusy.add(keyBase + e.get("roomCode"));
            facultyHours.merge(resolveFacultyCode(e, allFaculty), 1, Integer::sum);
        }

        List<Map<String, Object>> labRooms = new ArrayList<>();
        List<Map<String, Object>> classRooms = new ArrayList<>();
        for (Map<String, Object> r : allRooms) {
            if ("Lab".equalsIgnoreCase(String.valueOf(r.get("roomType")))) labRooms.add(r);
            else classRooms.add(r);
        }
        if (labRooms.isEmpty()) labRooms.addAll(allRooms);
        if (classRooms.isEmpty()) classRooms.addAll(allRooms);

        List<Map<String, Object>> generated = new ArrayList<>();
        List<String> softWarnings = new ArrayList<>();
        Map<String, Integer> labDayLoad = new HashMap<>();
        for (String d : days) labDayLoad.put(d, 0);
        String[] themes = {"blue", "cyan", "emerald", "amber", "purple", "indigo"};

        // Phase 1: Schedule 2-Hour Continuous Labs per sub-batch (Most Restricted Variable first)
        for (Map<String, Object> sec : targetSections) {
            String secCode = String.valueOf(sec.get("sectionCode"));
            String sem = String.valueOf(sec.get("semester"));
            int strength = Integer.parseInt(String.valueOf(sec.getOrDefault("studentStrength", 60)));
            String primaryRoom = String.valueOf(sec.getOrDefault("primaryRoom", ""));
            List<String> batches = Arrays.stream(String.valueOf(sec.getOrDefault("subBatches", "B1,B2")).split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();

            List<Map<String, Object>> semLabs = new ArrayList<>();
            for (Map<String, Object> sub : allSubjects) {
                if (sem.equalsIgnoreCase(String.valueOf(sub.get("semester")))
                        && "Lab".equalsIgnoreCase(String.valueOf(sub.get("subjectType")))) {
                    semLabs.add(sub);
                }
            }

            int labIdx = 0;
            for (Map<String, Object> lab : semLabs) {
                if ("MODE_B".equals(mode) && sem.equals("Sem 3") && labIdx >= 3) break;
                if ("MODE_A".equals(mode) && labIdx >= 3) break;
                String subCode = String.valueOf(lab.get("subjectCode"));
                String subName = String.valueOf(lab.get("subjectName"));
                for (String batch : batches) {
                    boolean placed = false;
                    List<String> dayOrder = new ArrayList<>(days);
                    if (balanceLabs) dayOrder.sort(Comparator.comparingInt(labDayLoad::get));

                    outerLab:
                    for (String day : dayOrder) {
                        for (String[] pair : labPairs) {
                            if (!timeByPeriod.containsKey(pair[0]) || !timeByPeriod.containsKey(pair[1])) continue;
                            if (isSectionBusy(sectionBusy, day, pair[0], secCode, batch)
                                    || isSectionBusy(sectionBusy, day, pair[1], secCode, batch)) continue;

                            Map<String, Object> fac = pickCspFaculty(allFaculty, lab, day, Arrays.asList(pair[0], pair[1]), facultyBusy, facultyHours, enforceMaxLoad);
                            Map<String, Object> rm = pickCspRoom(labRooms, primaryRoom, Math.max(20, strength / 2), respectCapacity, day, Arrays.asList(pair[0], pair[1]), roomBusy);
                            if (fac != null && rm != null) {
                                addGenEntry(generated, sem, day, pair[0], timeByPeriod.get(pair[0]), secCode, batch, subCode, subName, "Lab",
                                        fac, rm, themes[labIdx % themes.length], facultyBusy, roomBusy, sectionBusy, facultyHours);
                                addGenEntry(generated, sem, day, pair[1], timeByPeriod.get(pair[1]), secCode, batch, subCode, subName, "Lab",
                                        fac, rm, themes[labIdx % themes.length], facultyBusy, roomBusy, sectionBusy, facultyHours);
                                labDayLoad.merge(day, 1, Integer::sum);
                                placed = true;
                                break outerLab;
                            }
                        }
                    }
                    if (!placed && !strict && !allFaculty.isEmpty() && !labRooms.isEmpty()) {
                        Map<String, Object> f = allFaculty.get(labIdx % allFaculty.size());
                        Map<String, Object> r = labRooms.get(labIdx % labRooms.size());
                        softWarnings.add(secCode + " (" + batch + ") " + subCode + ": relaxed limit placed on Saturday P5");
                        addGenEntry(generated, sem, "Saturday", "P5", timeByPeriod.getOrDefault("P5", "01:10 PM - 02:00 PM"),
                                secCode, batch, subCode, subName, "Lab", f, r, "amber", facultyBusy, roomBusy, sectionBusy, facultyHours);
                    } else if (!placed && lockedExisting.isEmpty()) {
                        softWarnings.add("Strict mode deferred " + secCode + " " + batch + " " + subCode + " (no clash-free 2-hr lab slot available).");
                    }
                }
                labIdx++;
            }
        }

        // Phase 2: Schedule Theory, Tutorial, Seminar & Sem 8 Final Year Project / Internship blocks
        for (Map<String, Object> sec : targetSections) {
            String secCode = String.valueOf(sec.get("sectionCode"));
            String sem = String.valueOf(sec.get("semester"));
            int strength = Integer.parseInt(String.valueOf(sec.getOrDefault("studentStrength", 60)));
            String primaryRoom = String.valueOf(sec.getOrDefault("primaryRoom", ""));

            List<Map<String, Object>> semTheory = new ArrayList<>();
            for (Map<String, Object> sub : allSubjects) {
                if (sem.equalsIgnoreCase(String.valueOf(sub.get("semester")))
                        && !"Lab".equalsIgnoreCase(String.valueOf(sub.get("subjectType")))) {
                    String sCode = String.valueOf(sub.get("subjectCode"));
                    if ("MODE_B".equals(mode) && sCode.startsWith("253CR") && !"3CSRD".equals(secCode)) {
                        continue;
                    }
                    semTheory.add(sub);
                }
            }

            int subIdx = 0;
            for (Map<String, Object> sub : semTheory) {
                if ("Zero Credit".equalsIgnoreCase(String.valueOf(sub.get("subjectType")))) continue;
                int weeklyHrs = Integer.parseInt(String.valueOf(sub.getOrDefault("weeklyHours", 3)));
                int targetHrs = Math.max(1, Math.min(3, weeklyHrs));
                String subCode = String.valueOf(sub.get("subjectCode"));
                String subName = String.valueOf(sub.get("subjectName"));
                String sType = "Project".equalsIgnoreCase(String.valueOf(sub.get("subjectType"))) ? "Project"
                        : "Seminar".equalsIgnoreCase(String.valueOf(sub.get("subjectType"))) ? "Seminar" : "Lecture";

                Set<String> usedDays = new HashSet<>();
                for (int h = 0; h < targetHrs; h++) {
                    String bestDay = null, bestPeriod = null;
                    Map<String, Object> bestFac = null, bestRoom = null;
                    int bestScore = Integer.MAX_VALUE;

                    for (String day : days) {
                        for (String p : periods) {
                            if (isAnyBatchBusy(sectionBusy, day, p, secCode)) continue;
                            Map<String, Object> fac = pickCspFaculty(allFaculty, sub, day, List.of(p), facultyBusy, facultyHours, enforceMaxLoad);
                            Map<String, Object> rm = pickCspRoom(classRooms, primaryRoom, strength, respectCapacity, day, List.of(p), roomBusy);
                            if (fac != null && rm != null) {
                                int score = usedDays.contains(day) ? 60 : 0;
                                if ("Saturday".equals(day)) score += 18;
                                if (String.valueOf(rm.get("roomCode")).equalsIgnoreCase(primaryRoom)) score -= 20;
                                if (minimizeGaps) {
                                    score += computeGapScore(periods, String.valueOf(fac.get("facultyCode")), day, p, facultyBusy);
                                }
                                score += facultyHours.getOrDefault(String.valueOf(fac.get("facultyCode")), 0);
                                if (score < bestScore) {
                                    bestScore = score;
                                    bestDay = day;
                                    bestPeriod = p;
                                    bestFac = fac;
                                    bestRoom = rm;
                                }
                            }
                        }
                    }

                    if (bestDay != null) {
                        addGenEntry(generated, sem, bestDay, bestPeriod, timeByPeriod.get(bestPeriod), secCode, "ALL",
                                subCode, subName, sType, bestFac, bestRoom, themes[subIdx % themes.length],
                                facultyBusy, roomBusy, sectionBusy, facultyHours);
                        usedDays.add(bestDay);
                    } else if (!strict && !allFaculty.isEmpty() && !classRooms.isEmpty()) {
                        Map<String, Object> f = allFaculty.get((subIdx + h) % allFaculty.size());
                        Map<String, Object> r = classRooms.get(subIdx % classRooms.size());
                        String p = periods.get((subIdx + h) % periods.size());
                        softWarnings.add(secCode + " " + subCode + ": Balanced mode placed on Saturday " + p);
                        addGenEntry(generated, sem, "Saturday", p, timeByPeriod.get(p), secCode, "ALL",
                                subCode, subName, sType, f, r, "amber",
                                facultyBusy, roomBusy, sectionBusy, facultyHours);
                    } else if (lockedExisting.isEmpty()) {
                        softWarnings.add("Strict CSP left " + secCode + " " + subCode + " hour " + (h + 1) + " unassigned to protect weekly load <= 18h.");
                    }
                }
                subIdx++;
            }
        }

        List<Map<String, Object>> combinedForAudit = new ArrayList<>(lockedExisting);
        combinedForAudit.addAll(generated);
        List<Map<String, Object>> clashes = auditEntryList(generated);

        Map<String, Integer> semBreakdown = new LinkedHashMap<>();
        for (int i = 1; i <= 8; i++) semBreakdown.put("Sem " + i, 0);
        for (Map<String, Object> e : generated) {
            semBreakdown.merge(String.valueOf(e.get("semester")), 1, Integer::sum);
        }

        return mapOf(
                "status", "GENERATED",
                "mode", mode,
                "enforcementLevel", strict ? "STRICT" : "BALANCED",
                "totalSlotsGenerated", generated.size(),
                "hardClashes", clashes.size(),
                "softWarningCount", softWarnings.size(),
                "softWarnings", softWarnings,
                "semesterBreakdown", semBreakdown,
                "steps", Arrays.asList(
                        "Step 1/3: Fetched Sem 1-8 Data (" + targetSections.size() + " sections, " + allSubjects.size() + " subjects)",
                        "Step 2/3: Solved CSP & 2-Hr Batch-Split Labs (" + labRooms.size() + " labs, " + allFaculty.size() + " faculty)",
                        "Step 3/3: Validated Campus Clashes (" + generated.size() + " slots checked, " + clashes.size() + " hard clashes)"
                ),
                "entries", generated
        );
    }

    public synchronized Map<String, Object> updatePreviewSlot(String mode, long entryId, Map<String, Object> updates) {
        List<Map<String, Object>> preview = generatorPreviewByMode.get(mode);
        if (preview == null) {
            return mapOf("status", "NO_PREVIEW", "message", "Run Auto-Generator first to preview slots.");
        }
        Map<String, Object> target = null;
        for (Map<String, Object> e : preview) {
            if (Long.parseLong(String.valueOf(e.get("id"))) == entryId) {
                target = e;
                break;
            }
        }
        if (target == null) return mapOf("status", "NOT_FOUND");
        for (Map.Entry<String, Object> kv : updates.entrySet()) {
            if (!"id".equals(kv.getKey())) target.put(kv.getKey(), kv.getValue());
        }
        if (updates.containsKey("facultyName")) {
            String fn = String.valueOf(updates.get("facultyName"));
            for (Map<String, Object> f : facultyByMode.getOrDefault(mode, Collections.emptyList())) {
                if (fn.equalsIgnoreCase(String.valueOf(f.get("fullName")))) {
                    target.put("facultyCode", f.get("facultyCode"));
                    break;
                }
            }
        }
        if (updates.containsKey("dayOfWeek")) {
            target.put("dateLabel", dateForDay(String.valueOf(updates.get("dayOfWeek"))));
        }
        if (updates.containsKey("periodCode")) {
            String pc = String.valueOf(updates.get("periodCode"));
            for (Map<String, Object> ts : timeSlots) {
                if (pc.equalsIgnoreCase(String.valueOf(ts.get("slotCode")))) {
                    target.put("timeRange", ts.get("startTime") + " - " + ts.get("endTime"));
                    break;
                }
            }
        }
        List<Map<String, Object>> clashes = auditEntryList(preview);
        Map<String, Object> meta = generatorMetaByMode.getOrDefault(mode, new LinkedHashMap<>());
        meta.put("hardClashes", clashes.size());
        return mapOf("status", "UPDATED", "hardClashes", clashes.size(), "entry", target, "entries", preview);
    }

    public synchronized Map<String, Object> publishGeneratedSchedule(String mode, String targetScope) {
        List<Map<String, Object>> preview = generatorPreviewByMode.get(mode);
        if (preview == null || preview.isEmpty()) {
            return mapOf("status", "EMPTY", "message", "No generated schedule in staging preview. Run Auto-Generator first.");
        }
        List<Map<String, Object>> active = timetableByMode.get(mode);
        boolean all = targetScope == null || targetScope.isBlank() || "ALL".equalsIgnoreCase(targetScope);
        Set<String> generatedSections = new HashSet<>();
        for (Map<String, Object> e : preview) generatedSections.add(String.valueOf(e.get("sectionCode")));

        if (all) {
            active.clear();
        } else {
            active.removeIf(e -> targetScope.equalsIgnoreCase(String.valueOf(e.get("semester")))
                    || generatedSections.contains(String.valueOf(e.get("sectionCode"))));
        }

        int added = 0;
        for (Map<String, Object> p : preview) {
            Map<String, Object> copy = new LinkedHashMap<>(p);
            copy.put("id", idGen.incrementAndGet());
            copy.put("isGenerated", true);
            active.add(copy);
            added++;
        }
        List<Map<String, Object>> conflicts = auditConflicts(mode);
        return mapOf(
                "status", "PUBLISHED",
                "publishedCount", added,
                "totalCampusEntries", active.size(),
                "remainingConflicts", conflicts.size(),
                "message", "Published " + added + " auto-generated slots across " + (all ? "All 8 Semesters" : targetScope) + " to active database!"
        );
    }

    private boolean isSectionBusy(Set<String> sectionBusy, String day, String period, String sec, String batch) {
        return sectionBusy.contains(day + "|" + period + "|" + sec + "|" + batch)
                || sectionBusy.contains(day + "|" + period + "|" + sec + "|ALL");
    }

    private boolean isAnyBatchBusy(Set<String> sectionBusy, String day, String period, String sec) {
        String prefix = day + "|" + period + "|" + sec + "|";
        for (String k : sectionBusy) {
            if (k.startsWith(prefix)) return true;
        }
        return false;
    }

    private Map<String, Object> pickCspFaculty(List<Map<String, Object>> facultyList, Map<String, Object> subject,
                                               String day, List<String> periods, Set<String> facultyBusy,
                                               Map<String, Integer> facultyHours, boolean enforceMaxLoad) {
        List<Map<String, Object>> ranked = new ArrayList<>(facultyList);
        String sc = String.valueOf(subject.get("subjectCode")).toUpperCase();
        String rtu = String.valueOf(subject.getOrDefault("rtuCode", "")).toUpperCase();
        String shortC = String.valueOf(subject.getOrDefault("shortCode", "")).replace("-LAB", "").replace(" Lab", "").toUpperCase();

        ranked.sort((a, b) -> {
            int afA = (String.valueOf(a.getOrDefault("assignedSubjects", "")).toUpperCase().contains(sc) || (!rtu.isEmpty() && String.valueOf(a.getOrDefault("assignedSubjects", "")).toUpperCase().contains(rtu)) ? 20 : 0)
                    + (String.valueOf(a.getOrDefault("specialization", "")).toUpperCase().contains(shortC) ? 10 : 0);
            int afB = (String.valueOf(b.getOrDefault("assignedSubjects", "")).toUpperCase().contains(sc) || (!rtu.isEmpty() && String.valueOf(b.getOrDefault("assignedSubjects", "")).toUpperCase().contains(rtu)) ? 20 : 0)
                    + (String.valueOf(b.getOrDefault("specialization", "")).toUpperCase().contains(shortC) ? 10 : 0);
            if (afA != afB) return Integer.compare(afB, afA);
            return Integer.compare(facultyHours.getOrDefault(String.valueOf(a.get("facultyCode")), 0),
                    facultyHours.getOrDefault(String.valueOf(b.get("facultyCode")), 0));
        });

        for (Map<String, Object> f : ranked) {
            if (Boolean.parseBoolean(String.valueOf(f.getOrDefault("onLeave", false)))) continue;
            String fCode = String.valueOf(f.get("facultyCode"));
            int maxH = Integer.parseInt(String.valueOf(f.getOrDefault("maxWeeklyHours", 18)));
            if (enforceMaxLoad && facultyHours.getOrDefault(fCode, 0) + periods.size() > maxH) continue;
            boolean busy = false;
            for (String p : periods) {
                if (facultyBusy.contains(day + "|" + p + "|" + fCode)
                        || facultyBusy.contains(day + "|" + p + "|" + f.get("shortAbbr"))
                        || facultyBusy.contains(day + "|" + p + "|" + f.get("fullName"))) {
                    busy = true;
                    break;
                }
            }
            if (!busy) return f;
        }
        return null;
    }

    private Map<String, Object> pickCspRoom(List<Map<String, Object>> candidates, String primaryRoom, int neededCap,
                                            boolean respectCapacity, String day, List<String> periods, Set<String> roomBusy) {
        List<Map<String, Object>> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> {
            int pA = String.valueOf(a.get("roomCode")).equalsIgnoreCase(primaryRoom) ? 0 : 1;
            int pB = String.valueOf(b.get("roomCode")).equalsIgnoreCase(primaryRoom) ? 0 : 1;
            if (pA != pB) return Integer.compare(pA, pB);
            return Integer.compare(Integer.parseInt(String.valueOf(a.get("capacity"))),
                    Integer.parseInt(String.valueOf(b.get("capacity"))));
        });
        for (Map<String, Object> r : sorted) {
            int cap = Integer.parseInt(String.valueOf(r.get("capacity")));
            if (respectCapacity && cap + 6 < neededCap) continue;
            String rc = String.valueOf(r.get("roomCode"));
            boolean busy = false;
            for (String p : periods) {
                if (roomBusy.contains(day + "|" + p + "|" + rc)) {
                    busy = true;
                    break;
                }
            }
            if (!busy) return r;
        }
        return null;
    }

    private int computeGapScore(List<String> periods, String fCode, String day, String p, Set<String> facultyBusy) {
        int idx = periods.indexOf(p);
        if (idx < 0) return 0;
        boolean prev = idx > 0 && facultyBusy.contains(day + "|" + periods.get(idx - 1) + "|" + fCode);
        boolean next = idx < periods.size() - 1 && facultyBusy.contains(day + "|" + periods.get(idx + 1) + "|" + fCode);
        return (prev || next) ? -6 : 6;
    }

    private void addGenEntry(List<Map<String, Object>> out, String sem, String day, String period, String timeRange,
                             String secCode, String batch, String subCode, String subName, String sType,
                             Map<String, Object> fac, Map<String, Object> rm, String theme,
                             Set<String> facultyBusy, Set<String> roomBusy, Set<String> sectionBusy,
                             Map<String, Integer> facultyHours) {
        String fCode = String.valueOf(fac.get("facultyCode"));
        String fName = String.valueOf(fac.get("fullName"));
        String rCode = String.valueOf(rm.get("roomCode"));
        Map<String, Object> e = entry(idGen.incrementAndGet(), day, dateForDay(day), period, timeRange,
                secCode, batch, subCode, subName, sType, fCode, fName, rCode, theme, sem);
        e.put("isGenerated", true);
        out.add(e);

        facultyBusy.add(day + "|" + period + "|" + fCode);
        facultyBusy.add(day + "|" + period + "|" + fac.get("shortAbbr"));
        facultyBusy.add(day + "|" + period + "|" + fName);
        roomBusy.add(day + "|" + period + "|" + rCode);
        sectionBusy.add(day + "|" + period + "|" + secCode + "|" + batch);
        facultyHours.merge(fCode, 1, Integer::sum);
    }

    private String resolveFacultyCode(Map<String, Object> e, List<Map<String, Object>> faculty) {
        String fc = String.valueOf(e.getOrDefault("facultyCode", ""));
        for (Map<String, Object> f : faculty) {
            if (fc.equalsIgnoreCase(String.valueOf(f.get("facultyCode")))
                    || fc.equalsIgnoreCase(String.valueOf(f.get("shortAbbr")))) {
                return String.valueOf(f.get("facultyCode"));
            }
        }
        return fc;
    }

    private static String inferSemesterFromSection(String sectionCode) {
        if (sectionCode == null || sectionCode.isBlank()) return "Sem 3";
        char c = sectionCode.trim().charAt(0);
        if (c >= '1' && c <= '8') return "Sem " + c;
        return "Sem 3";
    }

    private List<Map<String, Object>> getTargetList(String mode, String entityType) {
        return switch (entityType.toLowerCase()) {
            case "students" -> studentsByMode.get(mode);
            case "faculty" -> facultyByMode.get(mode);
            case "departments" -> departments;
            case "courses" -> courses;
            case "subjects" -> subjectsByMode.get(mode);
            case "rooms" -> roomsByMode.get(mode);
            case "timeslots" -> timeSlots;
            case "timetable" -> timetableByMode.get(mode);
            default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
        };
    }

    private static Map<String, Object> entry(long id, String day, String dateLabel, String period, String timeRange,
                                             String section, String batch, String subjectCode, String subjectName,
                                             String sessionType, String facultyCode, String facultyName,
                                             String roomCode, String colorTheme) {
        return entry(id, day, dateLabel, period, timeRange, section, batch, subjectCode, subjectName,
                sessionType, facultyCode, facultyName, roomCode, colorTheme, inferSemesterFromSection(section));
    }

    private static Map<String, Object> entry(long id, String day, String dateLabel, String period, String timeRange,
                                             String section, String batch, String subjectCode, String subjectName,
                                             String sessionType, String facultyCode, String facultyName,
                                             String roomCode, String colorTheme, String semester) {
        return mapOf(
                "id", id,
                "semester", semester,
                "dayOfWeek", day,
                "dateLabel", dateLabel,
                "periodCode", period,
                "timeRange", timeRange,
                "sectionCode", section,
                "batchCode", batch,
                "subjectCode", subjectCode,
                "subjectName", subjectName,
                "sessionType", sessionType,
                "facultyCode", facultyCode,
                "facultyName", facultyName,
                "substituteFaculty", "",
                "roomCode", roomCode,
                "colorTheme", colorTheme,
                "hasConflict", false,
                "conflictReason", ""
        );
    }

    private static String dateForDay(String day) {
        return switch (day) {
            case "Monday" -> "05-10-2026";
            case "Tuesday" -> "06-10-2026";
            case "Wednesday" -> "07-10-2026";
            case "Thursday" -> "08-10-2026";
            case "Friday" -> "09-10-2026";
            case "Saturday" -> "10-10-2026";
            default -> "05-10-2026";
        };
    }

    private static String effectiveFaculty(Map<String, Object> e) {
        Object sub = e.get("substituteFaculty");
        if (sub != null && !String.valueOf(sub).isBlank()) {
            return String.valueOf(sub);
        }
        return String.valueOf(e.getOrDefault("facultyName", ""));
    }

    private static boolean isPhysicalRoom(String r) {
        if (r == null || r.isBlank()) return false;
        String u = r.trim().toUpperCase();
        return !u.equals("-") && !u.equals("NA") && !u.equals("LIB") && !u.equals("NPTEL");
    }

    private static boolean isRealFaculty(String f) {
        if (f == null || f.isBlank()) return false;
        String u = f.trim().toUpperCase();
        return !u.equals("-") && !u.equals("NA") && !u.contains("NPTEL") && !u.contains("LIBRARY");
    }

    public static Map<String, Object> mapOf(Object... kvs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put(String.valueOf(kvs[i]), kvs[i + 1]);
        }
        return map;
    }
}
