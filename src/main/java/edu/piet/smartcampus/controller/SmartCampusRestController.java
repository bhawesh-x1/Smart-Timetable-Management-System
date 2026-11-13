package edu.piet.smartcampus.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.piet.smartcampus.model.*;
import edu.piet.smartcampus.repository.*;
import edu.piet.smartcampus.service.AuthHelper;
import edu.piet.smartcampus.service.CspConflictResolutionService;
import edu.piet.smartcampus.service.DatasetSeederService;
import edu.piet.smartcampus.service.ExportService;
import edu.piet.smartcampus.service.TimetableGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class SmartCampusRestController {

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
    private final ExportService exportService;
    private final TimetableGeneratorService timetableGeneratorService;
    private final DatasetSeederService datasetSeederService;
    private final ObjectMapper objectMapper;

    private final Map<String, List<TimetableEntry>> previewCacheByMode = new ConcurrentHashMap<>();

    public SmartCampusRestController(DepartmentRepository departmentRepository,
                                     CourseRepository courseRepository,
                                     SemesterRepository semesterRepository,
                                     ClassSectionRepository classSectionRepository,
                                     StudentRepository studentRepository,
                                     SubjectRepository subjectRepository,
                                     FacultyRepository facultyRepository,
                                     RoomRepository roomRepository,
                                     TimeSlotRepository timeSlotRepository,
                                     TimetableEntryRepository timetableEntryRepository,
                                     CspConflictResolutionService cspService,
                                     ExportService exportService,
                                     TimetableGeneratorService timetableGeneratorService,
                                     DatasetSeederService datasetSeederService,
                                     ObjectMapper objectMapper) {
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
        this.exportService = exportService;
        this.timetableGeneratorService = timetableGeneratorService;
        this.datasetSeederService = datasetSeederService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String username = body.get("username") != null ? String.valueOf(body.get("username")) : "";
        String password = body.get("password") != null ? String.valueOf(body.get("password")) : "";
        String roleCategory = body.get("roleCategory") != null ? String.valueOf(body.get("roleCategory")) : "";
        String mode = body.get("mode") != null ? String.valueOf(body.get("mode")) : "MODE_A";

        List<AuthHelper.AuthCandidate> facCandidates = facultyRepository.findAll().stream()
                .map(f -> new AuthHelper.AuthCandidate(
                        f.getFacultyCode(), f.getShortAbbr(), f.getFullName(),
                        f.getEmail(), f.getPhone(), null, null))
                .toList();
        List<AuthHelper.AuthCandidate> stuCandidates = studentRepository.findAll().stream()
                .map(s -> new AuthHelper.AuthCandidate(
                        s.getEnrollmentNo(), null, s.getFullName(),
                        s.getEmail(), s.getPhone(), s.getSectionBatch(), s.getSemester()))
                .toList();

        Map<String, Object> result = AuthHelper.authenticate(username, password, roleCategory, mode, facCandidates, stuCandidates);
        if (result == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "error", "Invalid Institutional ID/Email or Password for selected role."
            ));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of("authenticated", false, "status", "LOGGED_OUT"));
    }

    @GetMapping("/semesters")
    public List<Semester> getSemesters() {
        return semesterRepository.findAllByOrderBySemesterNumberAsc();
    }

    @GetMapping("/state")
    public Map<String, Object> getFullApplicationState(@RequestParam(name = "mode", defaultValue = "MODE_A") String mode,
                                                     @RequestParam(name = "semester", defaultValue = "ALL") String semester) {
        List<String> modes = Arrays.asList("BOTH", mode);
        List<Semester> semesters = semesterRepository.findAllByOrderBySemesterNumberAsc();
        List<Department> departments = departmentRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Course> courses = courseRepository.findAll();
        List<ClassSection> sections = classSectionRepository.findAll();
        List<Student> students = studentRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Subject> subjects = subjectRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Faculty> faculty = facultyRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Room> rooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(modes);
        List<TimeSlot> timeSlots = timeSlotRepository.findByDatasetModeInOrderByPeriodNumberAsc(modes);
        List<TimetableEntry> allEntries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<TimetableEntry> entries = "ALL".equalsIgnoreCase(semester)
                ? allEntries
                : timetableEntryRepository.findByDatasetModeAndSemesterOrderByIdAsc(mode, semester);

        List<Map<String, Object>> conflicts = cspService.auditAndMarkConflicts(allEntries);
        List<Map<String, Object>> roomUtilization = computeRoomUtilization(rooms, allEntries, semester);
        List<Map<String, Object>> facultyWorkload = computeFacultyWorkload(faculty, entries);
        List<TimetableEntry> cachedPreview = previewCacheByMode.getOrDefault(mode, Collections.emptyList());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mode", mode);
        res.put("semester", semester);
        res.put("semesters", semesters);
        res.put("departments", departments);
        res.put("courses", courses);
        res.put("sections", sections);
        res.put("students", students);
        res.put("subjects", subjects);
        res.put("faculty", faculty);
        res.put("rooms", rooms);
        res.put("timeSlots", timeSlots);
        res.put("timetableEntries", entries);
        res.put("conflicts", conflicts);
        res.put("roomUtilization", roomUtilization);
        res.put("facultyWorkload", facultyWorkload);
        res.put("hasGeneratedPreview", !cachedPreview.isEmpty());
        res.put("generatorPreview", cachedPreview);
        return res;
    }

    /**
     * Unified CRUD endpoint matching the UI modal actions (/api/entity/{entityType}).
     */
    @PostMapping("/entity/{entityType}")
    public Object saveUnifiedEntity(@PathVariable("entityType") String entityType,
                                    @RequestParam(name = "mode", defaultValue = "MODE_A") String mode,
                                    @RequestBody Map<String, Object> payload) {
        return switch (entityType.toLowerCase(Locale.ROOT)) {
            case "students" -> {
                Student s = objectMapper.convertValue(payload, Student.class);
                if (s.getDisplayId() == null) s.setDisplayId((int) studentRepository.count() + 1);
                if (isBlank(s.getDepartmentName())) s.setDepartmentName("Computer Science & Engineering");
                if (isBlank(s.getCourseName())) s.setCourseName("B.Tech Computer Engineering");
                if (isBlank(s.getSemester())) s.setSemester("Sem 3");
                if (isBlank(s.getSectionBatch())) s.setSectionBatch("A (Batch A1)");
                if (s.getAdmissionYear() == null) s.setAdmissionYear(2025);
                if (isBlank(s.getDatasetMode())) s.setDatasetMode(mode);
                yield studentRepository.save(s);
            }
            case "faculty" -> {
                Faculty f = objectMapper.convertValue(payload, Faculty.class);
                if (f.getDisplayId() == null) f.setDisplayId((int) facultyRepository.count() + 1);
                if (isBlank(f.getDepartmentName())) f.setDepartmentName("Computer Science & Engineering");
                if (isBlank(f.getDesignation())) f.setDesignation("Assistant Professor");
                if (isBlank(f.getSpecialization())) f.setSpecialization("Computer Science");
                if (isBlank(f.getAssignedSubjects())) f.setAssignedSubjects("");
                if (f.getMaxWeeklyHours() == null) f.setMaxWeeklyHours(18);
                if (f.getOnLeave() == null) f.setOnLeave(false);
                if (isBlank(f.getDatasetMode())) f.setDatasetMode(mode);
                yield facultyRepository.save(f);
            }
            case "departments" -> {
                Department d = objectMapper.convertValue(payload, Department.class);
                if (d.getDisplayId() == null) d.setDisplayId((int) departmentRepository.count() + 1);
                if (isBlank(d.getDatasetMode())) d.setDatasetMode("BOTH");
                yield departmentRepository.save(d);
            }
            case "courses" -> {
                Course c = objectMapper.convertValue(payload, Course.class);
                if (c.getDisplayId() == null) c.setDisplayId((int) courseRepository.count() + 1);
                if (isBlank(c.getDepartmentCode())) c.setDepartmentCode("CSE");
                if (isBlank(c.getDepartmentName())) c.setDepartmentName("Computer Science & Engineering");
                if (c.getDurationYears() == null) c.setDurationYears(4);
                if (c.getTotalSemesters() == null) c.setTotalSemesters(8);
                if (c.getTotalCredits() == null) c.setTotalCredits(160);
                yield courseRepository.save(c);
            }
            case "subjects" -> {
                Subject s = objectMapper.convertValue(payload, Subject.class);
                if (s.getDisplayId() == null) s.setDisplayId((int) subjectRepository.count() + 1);
                if (isBlank(s.getDepartmentName())) s.setDepartmentName("Computer Science & Engineering");
                if (isBlank(s.getSemester())) s.setSemester("Sem 3");
                if (isBlank(s.getSubjectType())) s.setSubjectType("Theory");
                if (s.getCredits() == null) s.setCredits(3);
                if (s.getWeeklyHours() == null) s.setWeeklyHours(3);
                if (isBlank(s.getDatasetMode())) s.setDatasetMode(mode);
                yield subjectRepository.save(s);
            }
            case "rooms" -> {
                Room r = objectMapper.convertValue(payload, Room.class);
                if (isBlank(r.getRoomType())) r.setRoomType("Classroom");
                if (isBlank(r.getBuildingBlock())) r.setBuildingBlock("Block A");
                if (r.getCapacity() == null) r.setCapacity(60);
                if (r.getTotalWeeklySlots() == null) r.setTotalWeeklySlots(36);
                if (isBlank(r.getDatasetMode())) r.setDatasetMode(mode);
                yield roomRepository.save(r);
            }
            case "timeslots" -> {
                TimeSlot t = objectMapper.convertValue(payload, TimeSlot.class);
                if (isBlank(t.getDayOfWeek())) t.setDayOfWeek("Monday - Saturday");
                if (isBlank(t.getSlotType())) t.setSlotType("Lecture");
                if (t.getPeriodNumber() == null) t.setPeriodNumber((int) timeSlotRepository.count() + 1);
                if (isBlank(t.getDatasetMode())) t.setDatasetMode("BOTH");
                yield timeSlotRepository.save(t);
            }
            case "timetable" -> saveNormalizedTimetableEntry(mode, objectMapper.convertValue(payload, TimetableEntry.class));
            case "semesters" -> semesterRepository.save(objectMapper.convertValue(payload, Semester.class));
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        };
    }

    @DeleteMapping("/entity/{entityType}/{id}")
    public Map<String, Object> deleteUnifiedEntity(@PathVariable("entityType") String entityType,
                                                   @PathVariable("id") Long id) {
        switch (entityType.toLowerCase(Locale.ROOT)) {
            case "students" -> studentRepository.deleteById(id);
            case "faculty" -> facultyRepository.deleteById(id);
            case "departments" -> departmentRepository.deleteById(id);
            case "courses" -> courseRepository.deleteById(id);
            case "subjects" -> subjectRepository.deleteById(id);
            case "rooms" -> roomRepository.deleteById(id);
            case "timeslots" -> timeSlotRepository.deleteById(id);
            case "timetable" -> timetableEntryRepository.deleteById(id);
            case "semesters" -> semesterRepository.deleteById(id);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        return Map.of("status", "DELETED", "entityType", entityType, "id", id);
    }

    @PostMapping("/students")
    public Student saveStudent(@RequestBody Student s) {
        if (s.getDisplayId() == null) s.setDisplayId((int) studentRepository.count() + 1);
        return studentRepository.save(s);
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable("id") Long id) {
        studentRepository.deleteById(id);
    }

    @PostMapping("/faculty")
    public Faculty saveFaculty(@RequestBody Faculty f) {
        if (f.getDisplayId() == null) f.setDisplayId((int) facultyRepository.count() + 1);
        return facultyRepository.save(f);
    }

    @DeleteMapping("/faculty/{id}")
    public void deleteFaculty(@PathVariable("id") Long id) {
        facultyRepository.deleteById(id);
    }

    @PostMapping("/departments")
    public Department saveDepartment(@RequestBody Department d) {
        if (d.getDisplayId() == null) d.setDisplayId((int) departmentRepository.count() + 1);
        return departmentRepository.save(d);
    }

    @DeleteMapping("/departments/{id}")
    public void deleteDepartment(@PathVariable("id") Long id) {
        departmentRepository.deleteById(id);
    }

    @PostMapping("/courses")
    public Course saveCourse(@RequestBody Course c) {
        if (c.getDisplayId() == null) c.setDisplayId((int) courseRepository.count() + 1);
        return courseRepository.save(c);
    }

    @DeleteMapping("/courses/{id}")
    public void deleteCourse(@PathVariable("id") Long id) {
        courseRepository.deleteById(id);
    }

    @PostMapping("/subjects")
    public Subject saveSubject(@RequestBody Subject s) {
        if (s.getDisplayId() == null) s.setDisplayId((int) subjectRepository.count() + 1);
        return subjectRepository.save(s);
    }

    @DeleteMapping("/subjects/{id}")
    public void deleteSubject(@PathVariable("id") Long id) {
        subjectRepository.deleteById(id);
    }

    @PostMapping("/rooms")
    public Room saveRoom(@RequestBody Room r) {
        return roomRepository.save(r);
    }

    @DeleteMapping("/rooms/{id}")
    public void deleteRoom(@PathVariable("id") Long id) {
        roomRepository.deleteById(id);
    }

    @PostMapping("/timeslots")
    public TimeSlot saveTimeSlot(@RequestBody TimeSlot t) {
        return timeSlotRepository.save(t);
    }

    @DeleteMapping("/timeslots/{id}")
    public void deleteTimeSlot(@PathVariable("id") Long id) {
        timeSlotRepository.deleteById(id);
    }

    @PostMapping("/timetable")
    public TimetableEntry saveTimetableEntry(@RequestBody TimetableEntry e) {
        return saveNormalizedTimetableEntry(e.getDatasetMode() != null ? e.getDatasetMode() : "MODE_A", e);
    }

    @DeleteMapping("/timetable/{id}")
    public void deleteTimetableEntry(@PathVariable("id") Long id) {
        timetableEntryRepository.deleteById(id);
    }

    @PostMapping("/csp/resolve/{entryId}")
    public Map<String, Object> resolveConflict(@PathVariable("entryId") Long entryId,
                                               @RequestParam(name = "mode", defaultValue = "MODE_A") String mode) {
        TimetableEntry target = timetableEntryRepository.findById(entryId).orElseThrow();
        List<TimetableEntry> allEntries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<Room> rooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(Arrays.asList("BOTH", mode));
        List<Faculty> faculty = facultyRepository.findByDatasetModeInOrderByDisplayIdAsc(Arrays.asList("BOTH", mode));
        Map<String, Object> result = cspService.resolveConflictViaCsp(target, allEntries, rooms, faculty);
        timetableEntryRepository.save(target);
        return result;
    }

    @PostMapping("/csp/resolve-all")
    public Map<String, Object> resolveAllConflicts(@RequestParam(name = "mode", defaultValue = "MODE_A") String mode) {
        List<TimetableEntry> allEntries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<Room> rooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(Arrays.asList("BOTH", mode));
        List<Faculty> faculty = facultyRepository.findByDatasetModeInOrderByDisplayIdAsc(Arrays.asList("BOTH", mode));

        int resolvedCount = 0;
        for (int pass = 0; pass < 3; pass++) {
            List<Map<String, Object>> detected = cspService.auditAndMarkConflicts(allEntries);
            if (detected.isEmpty()) break;
            for (TimetableEntry entry : allEntries) {
                if (Boolean.TRUE.equals(entry.getHasConflict())) {
                    cspService.resolveConflictViaCsp(entry, allEntries, rooms, faculty);
                    resolvedCount++;
                }
            }
        }
        List<Map<String, Object>> remaining = cspService.auditAndMarkConflicts(allEntries);
        timetableEntryRepository.saveAll(allEntries);
        return Map.of(
                "resolvedCount", resolvedCount,
                "remainingConflicts", remaining.size(),
                "message", "CSP Engine auto-resolved " + resolvedCount + " clash entries (" + remaining.size() + " remaining)."
        );
    }

    @PostMapping({"/faculty/{id}/toggle-leave", "/faculty/leave/{id}"})
    public Map<String, Object> toggleFacultyLeave(@PathVariable("id") Long id,
                                                  @RequestParam(name = "mode", defaultValue = "MODE_A") String mode) {
        Faculty fac = facultyRepository.findById(id).orElseThrow();
        fac.setOnLeave(!Boolean.TRUE.equals(fac.getOnLeave()));
        facultyRepository.save(fac);
        List<TimetableEntry> entries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<Faculty> allFaculty = facultyRepository.findByDatasetModeInOrderByDisplayIdAsc(Arrays.asList("BOTH", mode));
        List<Map<String, Object>> subs = cspService.assignSubstitutesForFacultyLeave(fac, entries, allFaculty);
        timetableEntryRepository.saveAll(entries);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("faculty", fac);
        res.put("substitutions", subs);
        res.put("message", Boolean.TRUE.equals(fac.getOnLeave())
                ? fac.getFullName() + " marked ON LEAVE (" + subs.size() + " substitute assignments made)"
                : fac.getFullName() + " returned to ACTIVE duty");
        return res;
    }

    @PostMapping("/generator/run")
    @SuppressWarnings("unchecked")
    public Map<String, Object> runAutoGenerator(@RequestParam(name = "mode", defaultValue = "MODE_B") String mode,
                                                @RequestParam(name = "targetScope", defaultValue = "ALL") String targetScope,
                                                @RequestParam(name = "enforcementLevel", defaultValue = "STRICT") String enforcementLevel,
                                                @RequestParam(name = "minimizeGaps", defaultValue = "true") boolean minimizeGaps,
                                                @RequestParam(name = "balanceLabs", defaultValue = "true") boolean balanceLabs,
                                                @RequestParam(name = "respectCapacity", defaultValue = "true") boolean respectCapacity,
                                                @RequestParam(name = "enforceMaxLoad", defaultValue = "true") boolean enforceMaxLoad,
                                                @RequestBody(required = false) Map<String, Object> body) {
        String effMode = body != null && body.get("mode") != null ? String.valueOf(body.get("mode")) : mode;
        String effScope = body != null && body.get("targetScope") != null ? String.valueOf(body.get("targetScope")) : targetScope;
        String effLevel = body != null && body.get("enforcementLevel") != null ? String.valueOf(body.get("enforcementLevel")) : enforcementLevel;
        boolean effGaps = boolParam(body, "minimizeGaps", minimizeGaps);
        boolean effLabs = boolParam(body, "balanceLabs", balanceLabs);
        boolean effCap = boolParam(body, "respectCapacity", respectCapacity);
        boolean effLoad = boolParam(body, "enforceMaxLoad", enforceMaxLoad);

        Map<String, Object> result = timetableGeneratorService.generateSchedule(
                effMode, effScope, effLevel, effGaps, effLabs, effCap, effLoad);
        Object rawEntries = result.get("entries");
        if (rawEntries instanceof List<?> list) {
            previewCacheByMode.put(effMode, new ArrayList<>((List<TimetableEntry>) list));
        }
        return result;
    }

    @PostMapping("/generator/preview-update")
    public Map<String, Object> updatePreviewSlot(@RequestParam(name = "mode", defaultValue = "MODE_B") String mode,
                                                 @RequestBody Map<String, Object> body) {
        String effMode = body.get("mode") != null ? String.valueOf(body.get("mode")) : mode;
        long entryId = body.get("entryId") != null ? Long.parseLong(String.valueOf(body.get("entryId"))) : -1L;
        List<TimetableEntry> preview = previewCacheByMode.getOrDefault(effMode, Collections.emptyList());

        for (TimetableEntry e : preview) {
            if (Objects.equals(e.getId(), entryId)) {
                if (body.get("dayOfWeek") != null) e.setDayOfWeek(String.valueOf(body.get("dayOfWeek")));
                if (body.get("periodCode") != null) {
                    String p = String.valueOf(body.get("periodCode"));
                    e.setPeriodCode(p);
                    e.setTimeRange(defaultTimeRange(p));
                }
                if (body.get("roomCode") != null) e.setRoomCode(String.valueOf(body.get("roomCode")));
                if (body.get("facultyName") != null) e.setFacultyName(String.valueOf(body.get("facultyName")));
                break;
            }
        }
        List<Map<String, Object>> clashes = cspService.auditAndMarkConflicts(preview);
        return Map.of("entries", preview, "hardClashes", clashes.size());
    }

    @PostMapping("/generator/publish")
    public Map<String, Object> publishGeneratedSchedule(@RequestParam(name = "mode", defaultValue = "MODE_B") String mode,
                                                        @RequestParam(name = "targetScope", defaultValue = "ALL") String targetScope,
                                                        @RequestBody(required = false) Object rawBody) {
        String effMode = mode;
        String effScope = targetScope;
        List<TimetableEntry> entriesToPublish = previewCacheByMode.getOrDefault(mode, Collections.emptyList());

        if (rawBody instanceof List<?> list) {
            entriesToPublish = objectMapper.convertValue(list, new TypeReference<List<TimetableEntry>>() {});
        } else if (rawBody instanceof Map<?, ?> map) {
            if (map.get("mode") != null) effMode = String.valueOf(map.get("mode"));
            if (map.get("targetScope") != null) effScope = String.valueOf(map.get("targetScope"));
            if (map.get("entries") != null) {
                entriesToPublish = objectMapper.convertValue(map.get("entries"), new TypeReference<List<TimetableEntry>>() {});
            } else {
                entriesToPublish = previewCacheByMode.getOrDefault(effMode, Collections.emptyList());
            }
        }

        if (entriesToPublish.isEmpty()) {
            Map<String, Object> generated = timetableGeneratorService.generateSchedule(
                    effMode, effScope, "STRICT", true, true, true, true);
            @SuppressWarnings("unchecked")
            List<TimetableEntry> fresh = (List<TimetableEntry>) generated.getOrDefault("entries", Collections.emptyList());
            entriesToPublish = fresh;
        }

        Map<String, Object> pubRes = timetableGeneratorService.publishGeneratedEntries(effMode, effScope, entriesToPublish);
        Map<String, Object> out = new LinkedHashMap<>(pubRes);
        out.put("message", "Published " + pubRes.get("publishedCount") + " generated slots to active campus timetable ("
                + pubRes.get("remainingHardClashes") + " remaining clashes).");
        return out;
    }

    @PostMapping("/reset")
    public Map<String, Object> resetFactoryDatasets() {
        previewCacheByMode.clear();
        datasetSeederService.resetAllData();
        return Map.of(
                "status", "RESET_COMPLETE",
                "message", "Both Mode A (Screenshots) and Mode B (PIET Sem 1-8 + III-Sem 309-Cell Master) datasets restored."
        );
    }

    @GetMapping("/export/timetable.csv")
    public ResponseEntity<String> exportTimetableCsv(@RequestParam(name = "mode", defaultValue = "MODE_A") String mode,
                                                     @RequestParam(name = "semester", defaultValue = "ALL") String semester) {
        List<TimetableEntry> entries = "ALL".equalsIgnoreCase(semester)
                ? timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode)
                : timetableEntryRepository.findByDatasetModeAndSemesterOrderByIdAsc(mode, semester);
        cspService.auditAndMarkConflicts(entries);
        String csv = exportService.generateTimetableCsv(entries);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"PIET_Timetable_" + mode + "_" + semester + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/rooms.csv")
    public ResponseEntity<String> exportRoomsCsv(@RequestParam(name = "mode", defaultValue = "MODE_A") String mode,
                                                 @RequestParam(name = "semester", defaultValue = "ALL") String semester) {
        List<Room> rooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(Arrays.asList("BOTH", mode));
        List<TimetableEntry> entries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<Map<String, Object>> metrics = computeRoomUtilization(rooms, entries, semester);
        String csv = exportService.generateRoomUtilizationCsv(metrics);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"PIET_Room_Utilization_" + mode + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/report.html")
    public ResponseEntity<String> exportHtmlReport(@RequestParam(name = "mode", defaultValue = "MODE_A") String mode,
                                                   @RequestParam(name = "semester", defaultValue = "ALL") String semester) {
        List<Room> rooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(Arrays.asList("BOTH", mode));
        List<TimetableEntry> allEntries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<TimetableEntry> entries = "ALL".equalsIgnoreCase(semester)
                ? allEntries
                : timetableEntryRepository.findByDatasetModeAndSemesterOrderByIdAsc(mode, semester);
        cspService.auditAndMarkConflicts(entries);
        List<Map<String, Object>> metrics = computeRoomUtilization(rooms, allEntries, semester);
        String html = exportService.generatePrintableHtmlReport(mode, metrics, entries);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private TimetableEntry saveNormalizedTimetableEntry(String mode, TimetableEntry e) {
        if (isBlank(e.getDatasetMode())) e.setDatasetMode(mode);
        if (isBlank(e.getBatchCode())) e.setBatchCode("ALL");
        if (isBlank(e.getSessionType())) e.setSessionType("Lecture");
        if (isBlank(e.getFacultyCode())) e.setFacultyCode("FAC");
        if (isBlank(e.getTimeRange())) e.setTimeRange(defaultTimeRange(e.getPeriodCode()));
        if (isBlank(e.getSemester())) {
            String inferred = inferSemesterFromSection(e.getSectionCode());
            e.setSemester(inferred);
        }
        if (e.getHasConflict() == null) e.setHasConflict(false);
        if (e.getIsGenerated() == null) e.setIsGenerated(false);
        TimetableEntry saved = timetableEntryRepository.save(e);
        List<TimetableEntry> allForMode = timetableEntryRepository.findByDatasetModeOrderByIdAsc(saved.getDatasetMode());
        cspService.auditAndMarkConflicts(allForMode);
        timetableEntryRepository.saveAll(allForMode);
        return timetableEntryRepository.findById(saved.getId()).orElse(saved);
    }

    private String inferSemesterFromSection(String sectionCode) {
        if (!isBlank(sectionCode) && Character.isDigit(sectionCode.charAt(0))) {
            return "Sem " + sectionCode.charAt(0);
        }
        return "Sem 3";
    }

    private List<Map<String, Object>> computeRoomUtilization(List<Room> rooms, List<TimetableEntry> allEntries, String semester) {
        boolean filterSem = !isBlank(semester) && !"ALL".equalsIgnoreCase(semester);
        Map<String, Integer> occupiedCounts = new HashMap<>();
        for (TimetableEntry e : allEntries) {
            if (filterSem && !semester.equalsIgnoreCase(e.getSemester())) continue;
            occupiedCounts.merge(e.getRoomCode(), 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room r : rooms) {
            int totalSlots = r.getTotalWeeklySlots() != null && r.getTotalWeeklySlots() > 0 ? r.getTotalWeeklySlots() : 36;
            int occupied = (!filterSem && r.getOccupiedSlotsOverride() != null)
                    ? r.getOccupiedSlotsOverride()
                    : occupiedCounts.getOrDefault(r.getRoomCode(), 0);
            int free = Math.max(0, totalSlots - occupied);
            double pct = Math.round((occupied * 1000.0) / totalSlots) / 10.0;
            String status = pct >= 65.0 ? "Moderate" : (pct >= 40.0 ? "Optimal" : "Underutilized");

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("roomCode", r.getRoomCode());
            row.put("roomName", r.getRoomName());
            row.put("roomType", r.getRoomType());
            row.put("buildingBlock", r.getBuildingBlock());
            row.put("capacity", r.getCapacity());
            row.put("totalWeeklySlots", totalSlots);
            row.put("occupiedSlots", occupied);
            row.put("freeSlots", free);
            row.put("utilizationPct", String.format(Locale.US, "%.1f", pct));
            row.put("status", status);
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> computeFacultyWorkload(List<Faculty> faculty, List<TimetableEntry> entries) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Faculty f : faculty) {
            int assignedHours = 0;
            int lectureHours = 0;
            int labHours = 0;
            for (TimetableEntry e : entries) {
                String sub = e.getSubstituteFaculty() != null ? e.getSubstituteFaculty().trim() : "";
                boolean match = (!sub.isEmpty() && sub.equalsIgnoreCase(f.getFullName()))
                        || (sub.isEmpty() && (f.getFacultyCode().equalsIgnoreCase(e.getFacultyCode())
                        || (f.getShortAbbr() != null && f.getShortAbbr().equalsIgnoreCase(e.getFacultyCode()))
                        || f.getFullName().equalsIgnoreCase(e.getFacultyName())));
                if (match) {
                    assignedHours++;
                    if ("Lab".equalsIgnoreCase(e.getSessionType())) labHours++;
                    else lectureHours++;
                }
            }
            int maxHrs = f.getMaxWeeklyHours() != null && f.getMaxWeeklyHours() > 0 ? f.getMaxWeeklyHours() : 18;
            double pct = Math.min(100.0, Math.round((assignedHours * 1000.0) / maxHrs) / 10.0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", f.getId());
            row.put("displayId", f.getDisplayId());
            row.put("facultyCode", f.getFacultyCode());
            row.put("shortAbbr", f.getShortAbbr());
            row.put("fullName", f.getFullName());
            row.put("email", f.getEmail());
            row.put("phone", f.getPhone());
            row.put("departmentName", f.getDepartmentName());
            row.put("designation", f.getDesignation());
            row.put("specialization", f.getSpecialization());
            row.put("assignedSubjects", f.getAssignedSubjects());
            row.put("maxWeeklyHours", maxHrs);
            row.put("onLeave", f.getOnLeave());
            row.put("assignedHours", assignedHours);
            row.put("lectureHours", lectureHours);
            row.put("labHours", labHours);
            row.put("workloadPct", String.format(Locale.US, "%.1f", pct));
            result.add(row);
        }
        return result;
    }

    private static String defaultTimeRange(String periodCode) {
        if (periodCode == null) return "08:30 - 09:30";
        return switch (periodCode.toUpperCase(Locale.ROOT)) {
            case "P2" -> "09:30 - 10:30";
            case "P3" -> "10:30 - 11:30";
            case "P4" -> "11:30 - 12:30";
            case "P5" -> "01:10 - 02:00";
            case "P6" -> "02:00 - 02:50";
            default -> "08:30 - 09:30";
        };
    }

    private static boolean boolParam(Map<String, Object> body, String key, boolean fallback) {
        if (body == null || body.get(key) == null) return fallback;
        return Boolean.parseBoolean(String.valueOf(body.get(key)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Invalid request"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("error", "Requested record not found"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
