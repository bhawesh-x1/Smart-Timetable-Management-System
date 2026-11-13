package edu.piet.smartcampus.service;

import edu.piet.smartcampus.model.*;
import edu.piet.smartcampus.repository.*;
import edu.piet.smartcampus.runtime.SmartCampusDataStore;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Automatic Database Seeder for both:
 * - MODE_A: UI Screenshot Demo Dataset (Exact KPIs: 4 Students, 6 Faculty, 14 Subjects, 8 Rooms, 90 Timetable Entries, 8 Conflicts)
 * - MODE_B: PIET III-Sem Master Excel Dataset (Oct 5-9, 2026, 4 Sections 3CSA/3CSB/3CSC/3CSRD, 19 Faculty, 17 Rooms, 309 Timetable Cells)
 */
@Service
public class DatasetSeederService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final CspConflictResolutionService cspService;

    public DatasetSeederService(DepartmentRepository departmentRepository,
                                CourseRepository courseRepository,
                                SemesterRepository semesterRepository,
                                ClassSectionRepository classSectionRepository,
                                StudentRepository studentRepository,
                                SubjectRepository subjectRepository,
                                FacultyRepository facultyRepository,
                                RoomRepository roomRepository,
                                TimeSlotRepository timeSlotRepository,
                                TimetableEntryRepository timetableEntryRepository,
                                CspConflictResolutionService cspService) {
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.classSectionRepository = classSectionRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.cspService = cspService;
    }

    @PostConstruct
    @Transactional
    public void seedAllData() {
        if (departmentRepository.count() > 0) {
            return;
        }
        seedSemesters();
        seedDepartmentsAndCourses();
        seedClassSections();
        seedTimeSlots();
        seedModeAData();
        seedModeBData();
        seedScheduleAndMissingModeBFromStore();
    }

    /**
     * Deletes all persisted records and restores clean factory Mode A + Mode B datasets.
     */
    @Transactional
    public void resetAllData() {
        timetableEntryRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
        facultyRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
        timeSlotRepository.deleteAllInBatch();
        classSectionRepository.deleteAllInBatch();
        courseRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        semesterRepository.deleteAllInBatch();

        seedSemesters();
        seedDepartmentsAndCourses();
        seedClassSections();
        seedTimeSlots();
        seedModeAData();
        seedModeBData();
        seedScheduleAndMissingModeBFromStore();
    }

    private void seedSemesters() {
        String[][] semRows = {
            {"1", "Sem 1", "Semester 1 (First Year - Foundation)", "ODD", "21"},
            {"2", "Sem 2", "Semester 2 (First Year - Core)", "EVEN", "21"},
            {"3", "Sem 3", "Semester 3 (Second Year - CSE Core)", "ODD", "24"},
            {"4", "Sem 4", "Semester 4 (Second Year - Systems)", "EVEN", "23"},
            {"5", "Sem 5", "Semester 5 (Third Year - Advanced)", "ODD", "23"},
            {"6", "Sem 6", "Semester 6 (Third Year - AI & Cloud)", "EVEN", "22"},
            {"7", "Sem 7", "Semester 7 (Final Year - Specializations)", "ODD", "20"},
            {"8", "Sem 8", "Semester 8 (Final Year Project & Internship)", "EVEN", "16"}
        };
        for (String[] s : semRows) {
            semesterRepository.save(new Semester(Integer.parseInt(s[0]), s[1], s[2], s[3], Integer.parseInt(s[4])));
        }
    }

    private void seedDepartmentsAndCourses() {
        // Exact Screenshot 6 rows: 1 CSE, 3 ECE, 2 IT, 4 MECH
        departmentRepository.save(new Department(1, "CSE", "Computer Science & Engineering", "Dr. Anil Kumar", "Block A & C", "BOTH"));
        departmentRepository.save(new Department(3, "ECE", "Electronics & Communication", "Dr. Rakesh Gupta", "Block B", "BOTH"));
        departmentRepository.save(new Department(2, "IT", "Information Technology", "Dr. Pooja Sharma", "Block C", "BOTH"));
        departmentRepository.save(new Department(4, "MECH", "Mechanical Engineering", "Dr. Vikram Singh", "Block D", "BOTH"));

        courseRepository.save(new Course(1, "BTECH-CSE", "B.Tech Computer Engineering", "CSE", "Computer Science & Engineering", 4, 8, 164, "Dr. Varsha Kumari"));
        courseRepository.save(new Course(2, "BTECH-AID", "B.Tech CS (AI & Data Science)", "CSE", "Computer Science & Engineering", 4, 8, 164, "Dr. Rajesh Verma"));
        courseRepository.save(new Course(3, "BTECH-IT", "B.Tech Information Technology", "IT", "Information Technology", 4, 8, 160, "Ms. Madhu Choudary"));
        courseRepository.save(new Course(4, "BTECH-ECE", "B.Tech Electronics & Comm.", "ECE", "Electronics & Communication", 4, 8, 160, "Dr. Ashwini"));
    }

    private void seedClassSections() {
        // Sem 1 - Sem 8 B.Tech Computer Engineering sections (aligned with SmartCampusDataStore)
        classSectionRepository.save(new ClassSection("1CSA", "Sem 1 - Sec A (1CSA)", "Sem 1", "B.Tech Computer Engineering", "CSE", "A1, A2", "C-208", 60, "2026-2027"));
        classSectionRepository.save(new ClassSection("2CSA", "Sem 2 - Sec A (2CSA)", "Sem 2", "B.Tech Computer Engineering", "CSE", "A1, A2", "C-308", 60, "2026-2027"));
        classSectionRepository.save(new ClassSection("3CSA", "Sem 3 - Sec A (Entire Section)", "Sem 3", "B.Tech Computer Engineering", "CSE", "A1, A2, A3", "A-202", 66, "2026-2027"));
        classSectionRepository.save(new ClassSection("3CSB", "Sem 3 - Sec B (3CSB)", "Sem 3", "B.Tech Computer Engineering", "CSE", "B1, B2, B3", "C-201", 64, "2026-2027"));
        classSectionRepository.save(new ClassSection("3CSC", "Sem 3 - Sec C (3CSC)", "Sem 3", "B.Tech Computer Engineering", "CSE", "C1, C2, C3", "C-202", 62, "2026-2027"));
        classSectionRepository.save(new ClassSection("3CSRD", "Sem 3 - Sec D (3CSRD)", "Sem 3", "B.Tech Computer Engineering", "CSE", "D1, D2", "A-203", 54, "2026-2027"));
        classSectionRepository.save(new ClassSection("4CSA", "Sem 4 - Sec A (4CSA)", "Sem 4", "B.Tech Computer Engineering", "CSE", "A1, A2", "A-203", 60, "2026-2027"));
        classSectionRepository.save(new ClassSection("5CSA", "Sem 5 - Sec A (5CSA)", "Sem 5", "B.Tech Computer Engineering", "CSE", "A1, A2", "C-208", 58, "2026-2027"));
        classSectionRepository.save(new ClassSection("6CSA", "Sem 6 - Sec A (6CSA)", "Sem 6", "B.Tech Computer Engineering", "CSE", "A1, A2", "C-308", 58, "2026-2027"));
        classSectionRepository.save(new ClassSection("7CSA", "Sem 7 - Sec A (7CSA)", "Sem 7", "B.Tech Computer Engineering", "CSE", "A1, A2", "A-201", 55, "2026-2027"));
        classSectionRepository.save(new ClassSection("8CSA", "Sem 8 - Sec A (Project & Internship)", "Sem 8", "B.Tech Computer Engineering", "CSE", "A1, A2", "SH01", 55, "2026-2027"));
    }

    private void seedTimeSlots() {
        timeSlotRepository.save(new TimeSlot("P1", "Period 1 (08:30 - 09:30)", "Monday - Saturday", "08:30 AM", "09:30 AM", "Lecture", 1, "BOTH"));
        timeSlotRepository.save(new TimeSlot("P2", "Period 2 (09:30 - 10:30)", "Monday - Saturday", "09:30 AM", "10:30 AM", "Lecture", 2, "BOTH"));
        timeSlotRepository.save(new TimeSlot("P3", "Period 3 (10:30 - 11:30)", "Monday - Saturday", "10:30 AM", "11:30 AM", "Lecture", 3, "BOTH"));
        timeSlotRepository.save(new TimeSlot("P4", "Period 4 (11:30 - 12:30)", "Monday - Saturday", "11:30 AM", "12:30 PM", "Lecture", 4, "BOTH"));
        timeSlotRepository.save(new TimeSlot("BREAK", "Lunch Break (12:30 - 01:10)", "Monday - Saturday", "12:30 PM", "01:10 PM", "Break", 5, "BOTH"));
        timeSlotRepository.save(new TimeSlot("P5", "Period 5 (01:10 - 02:00)", "Monday - Saturday", "01:10 PM", "02:00 PM", "Lecture", 6, "BOTH"));
        timeSlotRepository.save(new TimeSlot("P6", "Period 6 (02:00 - 02:50)", "Monday - Saturday", "02:00 PM", "02:50 PM", "Lecture", 7, "BOTH"));
    }

    private void seedModeAData() {
        // 4 Students from Screenshot 4
        studentRepository.save(new Student(1, "STU001", "Anurag Chaudhary", "student1@piet.edu", "9998887771",
                "Computer Science & Engineering", "B.Tech Computer Engineering", "Sem 3", "A (Batch A1)", 2025, "MODE_A"));
        studentRepository.save(new Student(2, "STU002", "Arnav Nagar", "student2@piet.edu", "9998887772",
                "Computer Science & Engineering", "B.Tech Computer Engineering", "Sem 3", "A (Batch A1)", 2025, "MODE_A"));
        studentRepository.save(new Student(3, "STU003", "Bhawesh Sharma", "bhaweshs1@piet.edu", "1234567891",
                "Computer Science & Engineering", "B.Tech Computer Engineering", "Sem 3", "A (Batch A1)", 2025, "MODE_A"));
        studentRepository.save(new Student(4, "STU004", "Abhishek Sharma", "1234@gmail.com", "1231231230",
                "Computer Science & Engineering", "B.Tech Computer Engineering", "Sem 3", "A (Batch A2)", 2025, "MODE_A"));

        // 6 Faculty from Screenshot 5
        facultyRepository.save(new Faculty(1, "FAC-001", "VK", "Dr. Varsha Kumari", "kumaridimple@piet.edu", "6375900934",
                "Computer Science & Engineering", "Associate Professor", "DSA", "CS-001, CS-002, CS-003", 18, false, "MODE_A"));
        facultyRepository.save(new Faculty(2, "FAC-002", "MC", "Madhu Choudary", "profmadhu1@piet.edu", "9876543211",
                "Computer Science & Engineering", "Associate Professor", "java-programming", "CS-004, CS-005", 18, false, "MODE_A"));
        facultyRepository.save(new Faculty(3, "FAC-003", "AK", "Dr.Ashwini", "profashwini@piet.edu", "9876543212",
                "Computer Science & Engineering", "Assistant Professor", "DSLD", "CS303, CS304, CS-006, CS-007", 18, false, "MODE_A"));
        facultyRepository.save(new Faculty(31, "FAC-004", "OPS", "OP sikhwal", "opsir@gmail.com", "32423432423",
                "Computer Science & Engineering", "Professor", "MATHS", "CS305", 18, false, "MODE_A"));
        facultyRepository.save(new Faculty(5, "FAC-005", "UO", "Udbhav Oajha", "profudbhav1@piet.edu", "983232423",
                "Computer Science & Engineering", "Professor", "UI/UX", "CS-008, CS-009", 18, false, "MODE_A"));
        facultyRepository.save(new Faculty(6, "FAC-006", "PD", "Pince Dabar", "profprince12@piet.edu", "998696776",
                "Computer Science & Engineering", "Professor", "Technical Communication", "CS-112", 18, false, "MODE_A"));

        // 14 Subjects from Screenshot 7 & Faculty Assignments
        subjectRepository.save(new Subject(1, "CS-001", "253CS4-04", "DSA", "Data Structures and Algorithms", "Computer Science & Engineering", "Sem 3", "Theory", 4, 4, "MODE_A"));
        subjectRepository.save(new Subject(2, "CS-002", "253CS4-21", "DSA-LAB", "Data Structures & Algorithms Lab", "Computer Science & Engineering", "Sem 3", "Lab", 2, 2, "MODE_A"));
        subjectRepository.save(new Subject(3, "CS-003", "253CS4-06", "DBMS", "Database Management Systems", "Computer Science & Engineering", "Sem 3", "Theory", 4, 4, "MODE_A"));
        subjectRepository.save(new Subject(4, "CS-004", "253CS4-05", "JAVA", "Object Oriented Programming (Java)", "Computer Science & Engineering", "Sem 3", "Theory", 3, 3, "MODE_A"));
        subjectRepository.save(new Subject(5, "CS-005", "253CS4-22", "JAVA-LAB", "Java Programming Lab", "Computer Science & Engineering", "Sem 3", "Lab", 2, 2, "MODE_A"));
        subjectRepository.save(new Subject(6, "CS-006", "253CS4-03", "DSLD", "Digital Systems & Logic Design", "Computer Science & Engineering", "Sem 3", "Theory", 3, 3, "MODE_A"));
        subjectRepository.save(new Subject(7, "CS-007", "253CS4-24", "DSLD-LAB", "Digital Logic Design Lab", "Computer Science & Engineering", "Sem 3", "Lab", 2, 2, "MODE_A"));
        subjectRepository.save(new Subject(8, "CS-008", "253CR4-08", "UI/UX", "UI/UX Design & Prototyping", "Computer Science & Engineering", "Sem 3", "Theory", 3, 3, "MODE_A"));
        subjectRepository.save(new Subject(9, "CS-009", "253CR4-28", "UI/UX-LAB", "UI/UX Interactive Studio Lab", "Computer Science & Engineering", "Sem 3", "Lab", 2, 2, "MODE_A"));
        subjectRepository.save(new Subject(10, "CS303", "253CS4-03A", "COA", "Computer Organization & Architecture", "Computer Science & Engineering", "Sem 3", "Theory", 3, 3, "MODE_A"));
        subjectRepository.save(new Subject(11, "CS304", "253CS4-06A", "OS", "Operating Systems Principles", "Computer Science & Engineering", "Sem 3", "Theory", 4, 4, "MODE_A"));
        subjectRepository.save(new Subject(12, "CS305", "253CS3-01", "AEM", "Advanced Engineering Mathematics", "Computer Science & Engineering", "Sem 3", "Theory", 3, 3, "MODE_A"));
        subjectRepository.save(new Subject(13, "CS-112", "253CS2-02", "TC", "Technical Communication", "Computer Science & Engineering", "Sem 3", "Theory", 2, 2, "MODE_A"));
        subjectRepository.save(new Subject(14, "CS-010", "253CS4-23", "DBMS-LAB", "Database Management Systems Lab", "Computer Science & Engineering", "Sem 3", "Lab", 2, 2, "MODE_A"));

        // 8 Rooms from Screenshot 10 with exact occupied counts summing to 90 entries
        roomRepository.save(new Room("A-202", "Room 102", "Classroom", "Block A", 60, 36, 25, "MODE_A"));
        roomRepository.save(new Room("A-209", "CS Lab 1", "Lab", "Block A", 30, 36, 16, "MODE_A"));
        roomRepository.save(new Room("C-201", "C-201", "Classroom", "Block C", 60, 36, 12, "MODE_A"));
        roomRepository.save(new Room("C-202", "Room 101", "Classroom", "Block C", 60, 36, 8, "MODE_A"));
        roomRepository.save(new Room("C-205", "CS Lab 3", "Lab", "Block C", 30, 36, 4, "MODE_A"));
        roomRepository.save(new Room("C-207", "CS Lab 2", "Lab", "Block C", 30, 36, 17, "MODE_A"));
        roomRepository.save(new Room("C-208", "C-208", "Classroom", "Block C", 60, 36, 0, "MODE_A"));
        roomRepository.save(new Room("SH01", "Seminar Hall 1", "Classroom", "Block A", 120, 36, 8, "MODE_A"));
    }

    private void seedModeBData() {
        // 19 Faculty Members from PIET III-Sem Master Excel Sheet
        String[][] pietFaculty = {
            {"1", "FAC-001", "VK", "Dr. Varsha Kumari", "kumaridimple@piet.edu", "6375900934", "Associate Professor", "Data Structures & Algorithms", "253CS4-04, 253CS4-21"},
            {"2", "FAC-002", "MC", "Madhu Choudary", "profmadhu1@piet.edu", "9876543211", "Associate Professor", "OOP & Java Programming", "253CS4-05, 253CS4-22"},
            {"3", "FAC-003", "AK", "Dr.Ashwini", "profashwini@piet.edu", "9876543212", "Assistant Professor", "Digital Electronics & Logic Design", "253CS4-03, 253CS4-24"},
            {"4", "FAC-004", "OPS", "OP sikhwal", "opsir@gmail.com", "9414000031", "Professor", "Advanced Engineering Mathematics", "253CS3-01"},
            {"5", "FAC-005", "UO", "Udbhav Oajha", "profudbhav1@piet.edu", "9832324230", "Professor", "UI/UX & Web Engineering", "253CR4-08, 253CR4-28"},
            {"6", "FAC-006", "PD", "Pince Dabar", "profprince12@piet.edu", "9986967760", "Professor", "Technical Communication", "253CS2-02"},
            {"7", "FAC-007", "RV", "Dr. Rajesh Verma", "rajesh.verma@piet.edu", "9829011223", "Professor", "Software Engineering & DBMS", "253CS4-06, 253CS4-23"},
            {"8", "FAC-008", "SK", "Ms. Shikha", "shikha.cs@piet.edu", "9829011224", "Assistant Professor", "Data Structures Lab & C++", "253CS4-04, 253CS4-21"},
            {"9", "FAC-009", "AS", "Mr. Abhishek Sharma", "abhishek.sharma@piet.edu", "9829011225", "Assistant Professor", "Software Engineering", "253CS4-06, 253CS4-23"},
            {"10", "FAC-010", "PG", "Ms. Priyanka Gupta", "priyanka.gupta@piet.edu", "9829011226", "Assistant Professor", "Digital Electronics", "253CS4-03, 253CS4-24"},
            {"11", "FAC-011", "NK", "Mr. Naveen Kumar", "naveen.kumar@piet.edu", "9829011227", "Assistant Professor", "OOP & C++ Lab", "253CS4-05, 253CS4-22"},
            {"12", "FAC-012", "SS", "Ms. Swati Sharma", "swati.sharma@piet.edu", "9829011228", "Assistant Professor", "Technical Communication", "253CS2-02"},
            {"13", "FAC-013", "DS", "Dr. Deepak Sharma", "deepak.sharma@piet.edu", "9829011229", "Associate Professor", "Engineering Mathematics", "253CS3-01"},
            {"14", "FAC-014", "RK", "Mr. Rahul Kumar", "rahul.kumar@piet.edu", "9829011230", "Assistant Professor", "Linux & Shell Programming Lab", "253CS4-25"},
            {"15", "FAC-015", "PK", "Ms. Pooja Kumari", "pooja.kumari@piet.edu", "9829011231", "Assistant Professor", "Database Systems", "253CS4-06"},
            {"16", "FAC-016", "MK", "Mr. Manish Kumar", "manish.kumar@piet.edu", "9829011232", "Assistant Professor", "Core Java & Web Lab", "253CS4-22"},
            {"17", "FAC-017", "GS", "Ms. Garima Sharma", "garima.sharma@piet.edu", "9829011233", "Assistant Professor", "Data Science & R", "253CR4-07"},
            {"18", "FAC-018", "AP", "Dr. Anil Pratap", "anil.pratap@piet.edu", "9829011234", "Professor", "Industrial Training & Seminar", "253CS4-25"},
            {"19", "FAC-019", "NPTEL", "NPTEL / Library Coordinator", "nptel.cell@piet.edu", "9829011235", "Coordinator", "NPTEL & Self-Growth", "NPTEL, LIB, MM, SG"}
        };
        for (String[] f : pietFaculty) {
            facultyRepository.save(new Faculty(Integer.parseInt(f[0]), f[1], f[2], f[3], f[4], f[5],
                    "Computer Science & Engineering", f[6], f[7], f[8], 18, false, "MODE_B"));
        }

        // 17 Rooms & Labs in Mode B
        String[][] pietRooms = {
            {"A-201", "Block A - Room 201 (3CSA)", "Classroom", "Block A", "66"},
            {"A-202", "Block A - Room 202 (3CSB)", "Classroom", "Block A", "60"},
            {"A-203", "Block A - Room 203", "Classroom", "Block A", "60"},
            {"A-207", "Block A - Lab 207", "Lab", "Block A", "30"},
            {"A-208", "Block A - Lab 208", "Lab", "Block A", "30"},
            {"A-209", "Block A - CS Lab 1 (209)", "Lab", "Block A", "30"},
            {"C-201", "Block C - Room 201 (3CSC)", "Classroom", "Block C", "60"},
            {"C-202", "Block C - Room 101 (3CSRD)", "Classroom", "Block C", "60"},
            {"C-205", "Block C - CS Lab 3 (205)", "Lab", "Block C", "30"},
            {"C-206", "Block C - Programming Lab 206", "Lab", "Block C", "30"},
            {"C-207", "Block C - CS Lab 2 (207)", "Lab", "Block C", "30"},
            {"C-208", "Block C - Room 208", "Classroom", "Block C", "60"},
            {"C-301", "Block C - Lab 301", "Lab", "Block C", "30"},
            {"C-302(A)", "Block C - Lab 302(A)", "Lab", "Block C", "30"},
            {"C-302(B)", "Block C - Lab 302(B)", "Lab", "Block C", "30"},
            {"C-308", "Block C - Room 308", "Classroom", "Block C", "60"},
            {"SH01", "Seminar Hall 1", "Classroom", "Block A", "120"}
        };
        for (String[] r : pietRooms) {
            roomRepository.save(new Room(r[0], r[1], r[2], r[3], Integer.parseInt(r[4]), 36, null, "MODE_B"));
        }
    }

    /**
     * Populates Mode B students, Mode B Sem 1..8 subjects, and all Mode A & Mode B (Sem 1..8)
     * timetable entries from SmartCampusDataStore so JPA/MySQL matches the live runtime store 100%.
     */
    private void seedScheduleAndMissingModeBFromStore() {
        SmartCampusDataStore store = new SmartCampusDataStore();

        List<Map<String, Object>> modeBStudents = store.studentsByMode.get("MODE_B");
        if (modeBStudents != null) {
            for (Map<String, Object> m : modeBStudents) {
                studentRepository.save(new Student(
                        intVal(m.get("displayId"), 1),
                        strVal(m.get("enrollmentNo"), ""),
                        strVal(m.get("fullName"), ""),
                        strVal(m.get("email"), ""),
                        strVal(m.get("phone"), ""),
                        strVal(m.get("departmentName"), "Computer Science & Engineering"),
                        strVal(m.get("courseName"), "B.Tech Computer Engineering"),
                        strVal(m.get("semester"), "Sem 3"),
                        strVal(m.get("sectionBatch"), "A (Batch A1)"),
                        intVal(m.get("admissionYear"), 2025),
                        "MODE_B"
                ));
            }
        }

        List<Map<String, Object>> modeBSubjects = store.subjectsByMode.get("MODE_B");
        if (modeBSubjects != null) {
            for (Map<String, Object> m : modeBSubjects) {
                subjectRepository.save(new Subject(
                        intVal(m.get("displayId"), 1),
                        strVal(m.get("subjectCode"), ""),
                        strVal(m.get("rtuCode"), ""),
                        strVal(m.get("shortCode"), ""),
                        strVal(m.get("subjectName"), ""),
                        strVal(m.get("departmentName"), "Computer Science & Engineering"),
                        strVal(m.get("semester"), "Sem 3"),
                        strVal(m.get("subjectType"), "Theory"),
                        intVal(m.get("credits"), 3),
                        intVal(m.get("weeklyHours"), 3),
                        "MODE_B"
                ));
            }
        }

        seedTimetableForMode("MODE_A", store.timetableByMode.get("MODE_A"));
        seedTimetableForMode("MODE_B", store.timetableByMode.get("MODE_B"));
    }

    private void seedTimetableForMode(String mode, List<Map<String, Object>> rawEntries) {
        if (rawEntries == null || rawEntries.isEmpty()) return;
        List<TimetableEntry> entities = new ArrayList<>(rawEntries.size());
        for (Map<String, Object> m : rawEntries) {
            TimetableEntry e = new TimetableEntry(
                    strVal(m.get("dayOfWeek"), "Monday"),
                    strVal(m.get("dateLabel"), ""),
                    strVal(m.get("periodCode"), "P1"),
                    strVal(m.get("timeRange"), "08:30 - 09:30"),
                    strVal(m.get("sectionCode"), "3CSA"),
                    strVal(m.get("batchCode"), "ALL"),
                    strVal(m.get("subjectCode"), ""),
                    strVal(m.get("subjectName"), ""),
                    strVal(m.get("sessionType"), "Lecture"),
                    strVal(m.get("facultyCode"), "-"),
                    strVal(m.get("facultyName"), "-"),
                    strVal(m.get("roomCode"), "-"),
                    strVal(m.get("colorTheme"), "blue"),
                    Boolean.TRUE.equals(m.get("hasConflict")),
                    strVal(m.get("conflictReason"), null),
                    mode
            );
            String subFac = strVal(m.get("substituteFaculty"), "");
            if (!subFac.isEmpty()) {
                e.setSubstituteFaculty(subFac);
            }
            e.setSemester(strVal(m.get("semester"), "Sem 3"));
            e.setIsGenerated(Boolean.TRUE.equals(m.get("isGenerated")));
            entities.add(e);
        }
        cspService.auditAndMarkConflicts(entities);
        timetableEntryRepository.saveAll(entities);
    }

    private static String strVal(Object val, String fallback) {
        return val == null ? fallback : String.valueOf(val);
    }

    private static int intVal(Object val, int fallback) {
        if (val instanceof Number n) return n.intValue();
        if (val != null) {
            try { return Integer.parseInt(String.valueOf(val)); } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }
}
