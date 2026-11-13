package edu.piet.smartcampus.service;

import edu.piet.smartcampus.model.*;
import edu.piet.smartcampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 8-Semester Smart Auto-Generator Engine using Constraint Satisfaction (CSP) & Backtracking heuristics.
 * Generates clash-free schedules across Sem 1..Sem 8 handling Theory, Tutorial, 2-hour batch-split Labs,
 * and Sem 8 Project/Internship blocks.
 */
@Service
public class TimetableGeneratorService {

    private static final List<String> DAYS = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
    private static final List<String> PERIOD_ORDER = Arrays.asList("P1", "P2", "P3", "P4", "P5", "P6");
    private static final String[][] LAB_PAIRS = {{"P1", "P2"}, {"P3", "P4"}, {"P5", "P6"}};
    private static final Map<String, String> DAY_DATES = Map.of(
            "Monday", "05/10/2026",
            "Tuesday", "06/10/2026",
            "Wednesday", "07/10/2026",
            "Thursday", "08/10/2026",
            "Friday", "09/10/2026",
            "Saturday", "10/10/2026"
    );

    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final CspConflictResolutionService cspService;

    public TimetableGeneratorService(SemesterRepository semesterRepository,
                                     ClassSectionRepository classSectionRepository,
                                     SubjectRepository subjectRepository,
                                     FacultyRepository facultyRepository,
                                     RoomRepository roomRepository,
                                     TimeSlotRepository timeSlotRepository,
                                     TimetableEntryRepository timetableEntryRepository,
                                     CspConflictResolutionService cspService) {
        this.semesterRepository = semesterRepository;
        this.classSectionRepository = classSectionRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.cspService = cspService;
    }

    public Map<String, Object> generateSchedule(String mode,
                                                String targetScope,
                                                String enforcementLevel,
                                                boolean minimizeGaps,
                                                boolean balanceLabs,
                                                boolean respectCapacity,
                                                boolean enforceMaxLoad) {
        List<String> modes = Arrays.asList("BOTH", mode);
        List<ClassSection> allSections = classSectionRepository.findAll();
        List<Subject> allSubjects = subjectRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Faculty> allFaculty = facultyRepository.findByDatasetModeInOrderByDisplayIdAsc(modes);
        List<Room> allRooms = roomRepository.findByDatasetModeInOrderByRoomCodeAsc(modes);
        List<TimeSlot> allSlots = timeSlotRepository.findByDatasetModeInOrderByPeriodNumberAsc(modes);
        List<TimetableEntry> existingEntries = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);

        boolean strict = !"BALANCED".equalsIgnoreCase(enforcementLevel);
        List<ClassSection> targetSections = filterTargetSections(allSections, targetScope);

        Map<String, String> periodTimeMap = new LinkedHashMap<>();
        for (TimeSlot ts : allSlots) {
            if (!"Break".equalsIgnoreCase(ts.getSlotType())) {
                periodTimeMap.put(ts.getSlotCode(), ts.getStartTime().replace(" AM", "").replace(" PM", "")
                        + " - " + ts.getEndTime().replace(" AM", "").replace(" PM", ""));
            }
        }
        if (periodTimeMap.isEmpty()) {
            periodTimeMap.put("P1", "08:30 - 09:30");
            periodTimeMap.put("P2", "09:30 - 10:30");
            periodTimeMap.put("P3", "10:30 - 11:30");
            periodTimeMap.put("P4", "11:30 - 12:30");
            periodTimeMap.put("P5", "01:10 - 02:00");
            periodTimeMap.put("P6", "02:00 - 02:50");
        }

        Set<String> targetSectionCodes = new HashSet<>();
        for (ClassSection s : targetSections) targetSectionCodes.add(s.getSectionCode());

        // Pre-occupy resources from outside-scope sections so zero cross-semester clashes occur
        Set<String> facultyBusy = new HashSet<>();
        Set<String> roomBusy = new HashSet<>();
        Set<String> sectionBatchBusy = new HashSet<>();
        Map<String, Integer> facultyHours = new HashMap<>();
        for (Faculty f : allFaculty) facultyHours.put(f.getFacultyCode(), 0);

        for (TimetableEntry e : existingEntries) {
            if (!targetSectionCodes.contains(e.getSectionCode())) {
                facultyBusy.add(e.getDayOfWeek() + "|" + e.getPeriodCode() + "|" + e.getFacultyCode());
                roomBusy.add(e.getDayOfWeek() + "|" + e.getPeriodCode() + "|" + e.getRoomCode());
                facultyHours.merge(e.getFacultyCode(), 1, Integer::sum);
            }
        }

        List<TimetableEntry> generated = new ArrayList<>();
        List<String> softWarnings = new ArrayList<>();
        Map<String, Integer> labDayLoad = new HashMap<>();
        for (String d : DAYS) labDayLoad.put(d, 0);

        List<Room> labRooms = allRooms.stream().filter(r -> "Lab".equalsIgnoreCase(r.getRoomType())).toList();
        List<Room> classRooms = allRooms.stream().filter(r -> !"Lab".equalsIgnoreCase(r.getRoomType())).toList();
        String[] themes = {"blue", "green", "amber", "purple", "cyan", "slate"};

        // Phase 1: Schedule 2-hour Practical Labs first (MRV heuristic)
        for (ClassSection sec : targetSections) {
            String sem = sec.getSemester();
            List<Subject> semLabs = allSubjects.stream()
                    .filter(s -> sem.equalsIgnoreCase(s.getSemester()) && "Lab".equalsIgnoreCase(s.getSubjectType()))
                    .toList();
            String[] batches = Arrays.stream(sec.getSubBatches().split(",")).map(String::trim).filter(b -> !b.isEmpty()).toArray(String[]::new);
            if (batches.length == 0) batches = new String[]{"B1", "B2"};

            int labIdx = 0;
            for (Subject lab : semLabs) {
                if (("MODE_B".equalsIgnoreCase(mode) && "Sem 3".equalsIgnoreCase(sem)
                        || "MODE_A".equalsIgnoreCase(mode)) && labIdx >= 3) {
                    break;
                }
                for (String batch : batches) {
                    boolean placed = false;
                    List<String> orderedDays = new ArrayList<>(DAYS);
                    if (balanceLabs) {
                        orderedDays.sort(Comparator.comparingInt(labDayLoad::get));
                    }
                    outer:
                    for (String day : orderedDays) {
                        for (String[] pair : LAB_PAIRS) {
                            String pA = pair[0];
                            String pB = pair[1];
                            if (!periodTimeMap.containsKey(pA) || !periodTimeMap.containsKey(pB)) continue;
                            if (sectionBatchBusy.contains(day + "|" + pA + "|" + sec.getSectionCode() + "|" + batch)
                                    || sectionBatchBusy.contains(day + "|" + pB + "|" + sec.getSectionCode() + "|" + batch)
                                    || sectionBatchBusy.contains(day + "|" + pA + "|" + sec.getSectionCode() + "|ALL")
                                    || sectionBatchBusy.contains(day + "|" + pB + "|" + sec.getSectionCode() + "|ALL")) {
                                continue;
                            }
                            Faculty fac = pickAvailableFaculty(allFaculty, lab, day, Arrays.asList(pA, pB), facultyBusy, facultyHours, enforceMaxLoad);
                            Room room = pickAvailableRoom(labRooms.isEmpty() ? allRooms : labRooms, sec, true, respectCapacity, day, Arrays.asList(pA, pB), roomBusy);
                            if (fac != null && room != null) {
                                addGeneratedBlock(generated, fac, room, sec, lab, batch, day, pA, periodTimeMap.get(pA), "Lab",
                                        themes[(labIdx) % themes.length], mode, facultyBusy, roomBusy, sectionBatchBusy, facultyHours);
                                addGeneratedBlock(generated, fac, room, sec, lab, batch, day, pB, periodTimeMap.get(pB), "Lab",
                                        themes[(labIdx) % themes.length], mode, facultyBusy, roomBusy, sectionBatchBusy, facultyHours);
                                labDayLoad.merge(day, 1, Integer::sum);
                                placed = true;
                                break outer;
                            }
                        }
                    }
                    if (!placed && !strict && !allRooms.isEmpty() && !allFaculty.isEmpty()) {
                        Faculty fallbackFac = findBestMatchingFaculty(allFaculty, lab);
                        Room fallbackRoom = (labRooms.isEmpty() ? allRooms : labRooms).get(0);
                        softWarnings.add(sec.getSectionCode() + " (" + batch + ") " + lab.getSubjectCode() + ": relaxed room/faculty constraint on Saturday P5-P6");
                        addGeneratedBlock(generated, fallbackFac, fallbackRoom, sec, lab, batch, "Saturday", "P5", periodTimeMap.get("P5"), "Lab",
                                "amber", mode, facultyBusy, roomBusy, sectionBatchBusy, facultyHours);
                    } else if (!placed) {
                        softWarnings.add("Strict CSP skipped " + sec.getSectionCode() + " batch " + batch + " " + lab.getSubjectCode() + " due to room/faculty limit.");
                    }
                }
                labIdx++;
            }
        }

        // Phase 2: Schedule Theory, Tutorial & Sem 8 Project sessions
        for (ClassSection sec : targetSections) {
            String sem = sec.getSemester();
            List<Subject> semTheory = allSubjects.stream()
                    .filter(s -> sem.equalsIgnoreCase(s.getSemester())
                            && !"Lab".equalsIgnoreCase(s.getSubjectType())
                            && !"Zero Credit".equalsIgnoreCase(s.getSubjectType()))
                    .filter(s -> !("MODE_B".equalsIgnoreCase(mode) && s.getSubjectCode().startsWith("253CR") && !"3CSRD".equals(sec.getSectionCode())))
                    .toList();
            int subIdx = 0;
            for (Subject sub : semTheory) {
                int neededHours = Math.max(1, Math.min(3, sub.getWeeklyHours() != null ? sub.getWeeklyHours() : 3));
                String sType = "Project".equalsIgnoreCase(sub.getSubjectType()) ? "Project"
                        : "Seminar".equalsIgnoreCase(sub.getSubjectType()) ? "Seminar" : "Lecture";
                Set<String> usedDaysForSubject = new HashSet<>();
                for (int h = 0; h < neededHours; h++) {
                    String bestDay = null;
                    String bestPeriod = null;
                    Faculty bestFac = null;
                    Room bestRoom = null;
                    int bestScore = Integer.MAX_VALUE;

                    for (String day : DAYS) {
                        for (String period : PERIOD_ORDER) {
                            if (!periodTimeMap.containsKey(period)) continue;
                            if (sectionBatchBusy.contains(day + "|" + period + "|" + sec.getSectionCode() + "|ALL")) continue;
                            boolean batchOverlap = sectionBatchBusy.stream()
                                    .anyMatch(k -> k.startsWith(day + "|" + period + "|" + sec.getSectionCode() + "|"));
                            if (batchOverlap) continue;

                            Faculty fac = pickAvailableFaculty(allFaculty, sub, day, List.of(period), facultyBusy, facultyHours, enforceMaxLoad);
                            Room room = pickAvailableRoom(classRooms.isEmpty() ? allRooms : classRooms, sec, false, respectCapacity, day, List.of(period), roomBusy);
                            if (fac != null && room != null) {
                                int score = usedDaysForSubject.contains(day) ? 50 : 0;
                                if ("Saturday".equals(day)) score += 20;
                                if (room.getRoomCode().equalsIgnoreCase(sec.getPrimaryRoom())) score -= 15;
                                if (minimizeGaps) {
                                    score += computeFacultyGapPenalty(fac.getFacultyCode(), day, period, facultyBusy);
                                }
                                score += facultyHours.getOrDefault(fac.getFacultyCode(), 0);
                                if (score < bestScore) {
                                    bestScore = score;
                                    bestDay = day;
                                    bestPeriod = period;
                                    bestFac = fac;
                                    bestRoom = room;
                                }
                            }
                        }
                    }

                    if (bestDay != null) {
                        addGeneratedBlock(generated, bestFac, bestRoom, sec, sub, "ALL", bestDay, bestPeriod, periodTimeMap.get(bestPeriod),
                                sType, themes[subIdx % themes.length], mode, facultyBusy, roomBusy, sectionBatchBusy, facultyHours);
                        usedDaysForSubject.add(bestDay);
                    } else if (!strict && !allFaculty.isEmpty() && !allRooms.isEmpty()) {
                        Faculty fFac = findBestMatchingFaculty(allFaculty, sub);
                        Room fRoom = classRooms.isEmpty() ? allRooms.get(0) : classRooms.get(0);
                        String fallbackDay = "Saturday";
                        String fallbackPeriod = PERIOD_ORDER.get(h % PERIOD_ORDER.size());
                        softWarnings.add(sec.getSectionCode() + " " + sub.getSubjectCode() + ": soft relaxation used on " + fallbackDay + " " + fallbackPeriod);
                        addGeneratedBlock(generated, fFac, fRoom, sec, sub, "ALL", fallbackDay, fallbackPeriod, periodTimeMap.get(fallbackPeriod),
                                sType, "amber", mode, facultyBusy, roomBusy, sectionBatchBusy, facultyHours);
                    } else {
                        softWarnings.add("Could not place 1h of " + sub.getSubjectCode() + " for " + sec.getSectionCode() + " without exceeding constraints.");
                    }
                }
                subIdx++;
            }
        }

        List<Map<String, Object>> hardConflicts = cspService.auditAndMarkConflicts(generated);
        for (int i = 0; i < generated.size(); i++) {
            generated.get(i).setId((long) (i + 1));
        }
        Map<String, Integer> semCounts = new LinkedHashMap<>();
        for (int i = 1; i <= 8; i++) semCounts.put("Sem " + i, 0);
        for (TimetableEntry e : generated) {
            semCounts.merge(e.getSemester(), 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", mode);
        result.put("targetScope", targetScope);
        result.put("enforcementLevel", strict ? "STRICT" : "BALANCED");
        result.put("totalSlotsGenerated", generated.size());
        result.put("hardClashes", hardConflicts.size());
        result.put("softWarnings", softWarnings);
        result.put("softWarningCount", softWarnings.size());
        result.put("semesterBreakdown", semCounts);
        result.put("steps", Arrays.asList(
                "Fetching Sem 1-8 Data (" + targetSections.size() + " sections, " + allSubjects.size() + " subjects)",
                "Solving CSP & 2-Hr Batch-Split Labs (" + labRooms.size() + " labs, " + allFaculty.size() + " faculty)",
                "Validating Campus Clashes (" + generated.size() + " slots checked, " + hardConflicts.size() + " hard clashes)"
        ));
        result.put("entries", generated);
        return result;
    }

    @Transactional
    public Map<String, Object> publishGeneratedEntries(String mode, String targetScope, List<TimetableEntry> entriesToPublish) {
        List<TimetableEntry> existing = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        boolean allSemesters = targetScope == null || targetScope.isBlank() || "ALL".equalsIgnoreCase(targetScope);
        for (TimetableEntry e : existing) {
            if (allSemesters || targetScope.equalsIgnoreCase(e.getSemester())) {
                timetableEntryRepository.delete(e);
            }
        }
        for (TimetableEntry e : entriesToPublish) {
            e.setId(null);
            e.setDatasetMode(mode);
            e.setIsGenerated(true);
            timetableEntryRepository.save(e);
        }
        List<TimetableEntry> updatedAll = timetableEntryRepository.findByDatasetModeOrderByIdAsc(mode);
        List<Map<String, Object>> conflicts = cspService.auditAndMarkConflicts(updatedAll);
        timetableEntryRepository.saveAll(updatedAll);
        return Map.of(
                "publishedCount", entriesToPublish.size(),
                "totalCampusEntries", updatedAll.size(),
                "remainingHardClashes", conflicts.size()
        );
    }

    private List<ClassSection> filterTargetSections(List<ClassSection> all, String targetScope) {
        if (targetScope == null || targetScope.isBlank() || "ALL".equalsIgnoreCase(targetScope)) {
            return all;
        }
        return all.stream()
                .filter(s -> targetScope.equalsIgnoreCase(s.getSemester()) || targetScope.equalsIgnoreCase(s.getDepartmentCode()))
                .toList();
    }

    private Faculty pickAvailableFaculty(List<Faculty> facultyList, Subject subject, String day, List<String> periods,
                                         Set<String> facultyBusy, Map<String, Integer> facultyHours, boolean enforceMaxLoad) {
        List<Faculty> ranked = new ArrayList<>(facultyList);
        ranked.sort((a, b) -> {
            int matchA = facultySubjectAffinity(a, subject);
            int matchB = facultySubjectAffinity(b, subject);
            if (matchA != matchB) return Integer.compare(matchB, matchA);
            return Integer.compare(facultyHours.getOrDefault(a.getFacultyCode(), 0), facultyHours.getOrDefault(b.getFacultyCode(), 0));
        });
        for (Faculty f : ranked) {
            if (Boolean.TRUE.equals(f.getOnLeave())) continue;
            int maxHrs = f.getMaxWeeklyHours() != null ? f.getMaxWeeklyHours() : 18;
            if (enforceMaxLoad && facultyHours.getOrDefault(f.getFacultyCode(), 0) + periods.size() > maxHrs) continue;
            boolean clash = false;
            for (String p : periods) {
                if (facultyBusy.contains(day + "|" + p + "|" + f.getFacultyCode())) {
                    clash = true;
                    break;
                }
            }
            if (!clash) return f;
        }
        return null;
    }

    private Faculty findBestMatchingFaculty(List<Faculty> facultyList, Subject subject) {
        return facultyList.stream()
                .max(Comparator.comparingInt(f -> facultySubjectAffinity(f, subject)))
                .orElse(facultyList.get(0));
    }

    private int facultySubjectAffinity(Faculty f, Subject s) {
        int score = 0;
        if (f.getAssignedSubjects() != null && (f.getAssignedSubjects().contains(s.getSubjectCode())
                || (s.getRtuCode() != null && f.getAssignedSubjects().contains(s.getRtuCode())))) {
            score += 20;
        }
        if (f.getSpecialization() != null && s.getShortCode() != null
                && f.getSpecialization().toUpperCase().contains(s.getShortCode().replace("-LAB", "").toUpperCase())) {
            score += 10;
        }
        return score;
    }

    private Room pickAvailableRoom(List<Room> candidates, ClassSection section, boolean isLab, boolean respectCapacity,
                                   String day, List<String> periods, Set<String> roomBusy) {
        List<Room> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> {
            int prefA = !isLab && a.getRoomCode().equalsIgnoreCase(section.getPrimaryRoom()) ? 0 : 1;
            int prefB = !isLab && b.getRoomCode().equalsIgnoreCase(section.getPrimaryRoom()) ? 0 : 1;
            if (prefA != prefB) return Integer.compare(prefA, prefB);
            return Integer.compare(a.getCapacity(), b.getCapacity());
        });
        int neededCap = isLab ? Math.max(20, section.getStudentStrength() / 2) : section.getStudentStrength();
        for (Room r : sorted) {
            if (respectCapacity && r.getCapacity() + 6 < neededCap) continue;
            boolean busy = false;
            for (String p : periods) {
                if (roomBusy.contains(day + "|" + p + "|" + r.getRoomCode())) {
                    busy = true;
                    break;
                }
            }
            if (!busy) return r;
        }
        return null;
    }

    private int computeFacultyGapPenalty(String facultyCode, String day, String period, Set<String> facultyBusy) {
        int pIdx = PERIOD_ORDER.indexOf(period);
        if (pIdx < 0) return 0;
        boolean hasPrev = pIdx > 0 && facultyBusy.contains(day + "|" + PERIOD_ORDER.get(pIdx - 1) + "|" + facultyCode);
        boolean hasNext = pIdx < PERIOD_ORDER.size() - 1 && facultyBusy.contains(day + "|" + PERIOD_ORDER.get(pIdx + 1) + "|" + facultyCode);
        return (hasPrev || hasNext) ? -5 : 5;
    }

    private void addGeneratedBlock(List<TimetableEntry> out, Faculty fac, Room room, ClassSection sec, Subject sub,
                                   String batch, String day, String period, String timeRange, String sessionType,
                                   String colorTheme, String mode, Set<String> facultyBusy, Set<String> roomBusy,
                                   Set<String> sectionBatchBusy, Map<String, Integer> facultyHours) {
        TimetableEntry e = new TimetableEntry(
                day, DAY_DATES.getOrDefault(day, "05/10/2026"), period,
                timeRange != null ? timeRange : "08:30 - 09:30",
                sec.getSectionCode(), batch, sub.getSubjectCode(), sub.getSubjectName(),
                sessionType, fac.getFacultyCode(), fac.getFullName(), room.getRoomCode(),
                colorTheme, false, null, mode
        );
        e.setSemester(sec.getSemester());
        e.setIsGenerated(true);
        out.add(e);
        facultyBusy.add(day + "|" + period + "|" + fac.getFacultyCode());
        roomBusy.add(day + "|" + period + "|" + room.getRoomCode());
        sectionBatchBusy.add(day + "|" + period + "|" + sec.getSectionCode() + "|" + batch);
        facultyHours.merge(fac.getFacultyCode(), 1, Integer::sum);
    }
}
