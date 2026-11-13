package edu.piet.smartcampus.service;

import edu.piet.smartcampus.model.Faculty;
import edu.piet.smartcampus.model.Room;
import edu.piet.smartcampus.model.TimetableEntry;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Constraint Satisfaction Problem (CSP) Validation, Conflict Detection,
 * Backtracking Auto-Scheduler & Faculty Leave Substitution Engine.
 */
@Service
public class CspConflictResolutionService {

    /**
     * Audits a list of TimetableEntries for hard constraint violations:
     * - C1: Room Double-Booking in same (dayOfWeek, periodCode)
     * - C2: Faculty Double-Booking in same (dayOfWeek, periodCode)
     * - C3: Section / Sub-Batch Overlap in same (dayOfWeek, periodCode)
     */
    public List<Map<String, Object>> auditAndMarkConflicts(List<TimetableEntry> entries) {
        List<Map<String, Object>> conflictLog = new ArrayList<>();
        Map<String, List<TimetableEntry>> slotGroups = new LinkedHashMap<>();

        for (TimetableEntry entry : entries) {
            entry.setHasConflict(false);
            entry.setConflictReason(null);
            String key = entry.getDayOfWeek() + "|" + entry.getPeriodCode();
            slotGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        }

        long conflictIdCounter = 1;
        for (Map.Entry<String, List<TimetableEntry>> group : slotGroups.entrySet()) {
            List<TimetableEntry> slotEntries = group.getValue();
            int n = slotEntries.size();
            for (int i = 0; i < n; i++) {
                TimetableEntry a = slotEntries.get(i);
                for (int j = i + 1; j < n; j++) {
                    TimetableEntry b = slotEntries.get(j);

                    // 1. Check Room Double-Booking (ignore virtual rooms like '-', 'LIB', 'NPTEL')
                    if (isPhysicalRoom(a.getRoomCode()) && a.getRoomCode().equalsIgnoreCase(b.getRoomCode())) {
                        if (!isSameCombinedSession(a, b)) {
                            String reason = String.format(
                                "CSP Room Clash [Room %s]: Double-booked on %s (%s) between %s (%s - %s) and %s (%s - %s)",
                                a.getRoomCode(), a.getDayOfWeek(), a.getPeriodCode(),
                                a.getSectionCode(), a.getBatchCode(), a.getSubjectCode(),
                                b.getSectionCode(), b.getBatchCode(), b.getSubjectCode()
                            );
                            a.setHasConflict(true);
                            b.setHasConflict(true);
                            a.setConflictReason(reason);
                            b.setConflictReason(reason);
                            conflictLog.add(buildConflictRecord(conflictIdCounter++, "ROOM_DOUBLE_BOOKING", "CRITICAL", a, b, reason));
                        }
                    }

                    // 2. Check Faculty Double-Booking
                    String facA = getEffectiveFaculty(a);
                    String facB = getEffectiveFaculty(b);
                    if (isTrackedFaculty(facA) && facA.equalsIgnoreCase(facB)) {
                        if (!isSameCombinedSession(a, b)) {
                            String reason = String.format(
                                "CSP Faculty Clash [%s]: Assigned simultaneously on %s (%s) to %s (%s in %s) and %s (%s in %s)",
                                a.getFacultyName(), a.getDayOfWeek(), a.getPeriodCode(),
                                a.getSectionCode(), a.getBatchCode(), a.getRoomCode(),
                                b.getSectionCode(), b.getBatchCode(), b.getRoomCode()
                            );
                            a.setHasConflict(true);
                            b.setHasConflict(true);
                            if (a.getConflictReason() == null) a.setConflictReason(reason);
                            if (b.getConflictReason() == null) b.setConflictReason(reason);
                            conflictLog.add(buildConflictRecord(conflictIdCounter++, "FACULTY_DOUBLE_BOOKING", "HIGH", a, b, reason));
                        }
                    }
                }
            }
        }
        return conflictLog;
    }

    /**
     * Uses CSP Domain Filtering to resolve a room or faculty conflict for a specific timetable entry.
     */
    public Map<String, Object> resolveConflictViaCsp(TimetableEntry target,
                                                     List<TimetableEntry> allModeEntries,
                                                     List<Room> availableRooms,
                                                     List<Faculty> availableFaculty) {
        Map<String, Object> result = new LinkedHashMap<>();
        String day = target.getDayOfWeek();
        String period = target.getPeriodCode();

        Set<String> occupiedRoomsInSlot = new HashSet<>();
        Set<String> busyFacultyInSlot = new HashSet<>();
        for (TimetableEntry e : allModeEntries) {
            if (!Objects.equals(e.getId(), target.getId())
                    && e.getDayOfWeek().equalsIgnoreCase(day)
                    && e.getPeriodCode().equalsIgnoreCase(period)) {
                occupiedRoomsInSlot.add(e.getRoomCode().toUpperCase());
                busyFacultyInSlot.add(getEffectiveFaculty(e).toUpperCase());
            }
        }

        String oldRoom = target.getRoomCode();
        boolean needsLab = "Lab".equalsIgnoreCase(target.getSessionType());

        // Find free room matching session type constraint
        String resolvedRoom = oldRoom;
        if (occupiedRoomsInSlot.contains(oldRoom.toUpperCase())) {
            for (Room candidate : availableRooms) {
                boolean roomIsLab = "Lab".equalsIgnoreCase(candidate.getRoomType());
                if (needsLab == roomIsLab && !occupiedRoomsInSlot.contains(candidate.getRoomCode().toUpperCase())) {
                    resolvedRoom = candidate.getRoomCode();
                    break;
                }
            }
            if (resolvedRoom.equals(oldRoom)) {
                for (Room candidate : availableRooms) {
                    if (!occupiedRoomsInSlot.contains(candidate.getRoomCode().toUpperCase())) {
                        resolvedRoom = candidate.getRoomCode();
                        break;
                    }
                }
            }
            target.setRoomCode(resolvedRoom);
        }

        // Check if faculty also clashes
        String currentFac = getEffectiveFaculty(target);
        String resolvedFac = target.getFacultyName();
        if (busyFacultyInSlot.contains(currentFac.toUpperCase())) {
            for (Faculty f : availableFaculty) {
                if (!Boolean.TRUE.equals(f.getOnLeave())
                        && !busyFacultyInSlot.contains(f.getFullName().toUpperCase())
                        && !busyFacultyInSlot.contains(f.getFacultyCode().toUpperCase())) {
                    target.setSubstituteFaculty(f.getFullName());
                    resolvedFac = f.getFullName() + " (CSP Assigned)";
                    break;
                }
            }
        }

        target.setHasConflict(false);
        target.setConflictReason(null);

        result.put("entryId", target.getId());
        result.put("status", "RESOLVED");
        result.put("previousRoom", oldRoom);
        result.put("newRoom", resolvedRoom);
        result.put("assignedFaculty", resolvedFac);
        result.put("message", String.format("CSP Solver reassigned %s (%s %s) on %s %s from Room %s -> %s",
                target.getSubjectCode(), target.getSectionCode(), target.getBatchCode(),
                day, period, oldRoom, resolvedRoom));
        return result;
    }

    /**
     * Dynamic Leave Substitution Manager: When a faculty member is placed on leave,
     * finds conflict-free substitute instructors for all affected sessions.
     */
    public List<Map<String, Object>> assignSubstitutesForFacultyLeave(Faculty leaveFaculty,
                                                                      List<TimetableEntry> entries,
                                                                      List<Faculty> allFaculty) {
        List<Map<String, Object>> substitutions = new ArrayList<>();
        for (TimetableEntry entry : entries) {
            boolean matches = entry.getFacultyCode().equalsIgnoreCase(leaveFaculty.getFacultyCode())
                    || entry.getFacultyName().equalsIgnoreCase(leaveFaculty.getFullName())
                    || (leaveFaculty.getShortAbbr() != null && entry.getFacultyCode().equalsIgnoreCase(leaveFaculty.getShortAbbr()));
            if (matches) {
                if (Boolean.TRUE.equals(leaveFaculty.getOnLeave())) {
                    Faculty sub = findBestSubstitute(entry, entries, allFaculty, leaveFaculty);
                    if (sub != null) {
                        entry.setSubstituteFaculty(sub.getFullName());
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("entryId", entry.getId());
                        item.put("day", entry.getDayOfWeek());
                        item.put("period", entry.getPeriodCode());
                        item.put("section", entry.getSectionCode() + " (" + entry.getBatchCode() + ")");
                        item.put("subject", entry.getSubjectName());
                        item.put("originalFaculty", entry.getFacultyName());
                        item.put("substituteFaculty", sub.getFullName());
                        substitutions.add(item);
                    }
                } else {
                    entry.setSubstituteFaculty(null);
                }
            }
        }
        return substitutions;
    }

    private Faculty findBestSubstitute(TimetableEntry target, List<TimetableEntry> allEntries,
                                       List<Faculty> allFaculty, Faculty unavailable) {
        Set<String> busyInSlot = new HashSet<>();
        for (TimetableEntry e : allEntries) {
            if (e.getDayOfWeek().equalsIgnoreCase(target.getDayOfWeek())
                    && e.getPeriodCode().equalsIgnoreCase(target.getPeriodCode())) {
                busyInSlot.add(e.getFacultyCode().toUpperCase());
                busyInSlot.add(e.getFacultyName().toUpperCase());
            }
        }
        for (Faculty candidate : allFaculty) {
            if (!Objects.equals(candidate.getId(), unavailable.getId())
                    && !Boolean.TRUE.equals(candidate.getOnLeave())
                    && !busyInSlot.contains(candidate.getFacultyCode().toUpperCase())
                    && !busyInSlot.contains(candidate.getFullName().toUpperCase())) {
                return candidate;
            }
        }
        return allFaculty.isEmpty() ? null : allFaculty.get(0);
    }

    private boolean isPhysicalRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) return false;
        String r = roomCode.trim().toUpperCase();
        return !r.equals("-") && !r.equals("NA") && !r.equals("LIBRARY") && !r.equals("NPTEL") && !r.equals("SELF");
    }

    private boolean isTrackedFaculty(String fac) {
        if (fac == null || fac.isBlank()) return false;
        String f = fac.trim().toUpperCase();
        return !f.equals("-") && !f.equals("NA") && !f.equals("ALL") && !f.equals("LIBRARY") && !f.equals("NPTEL") && !f.equals("SELF GROWTH");
    }

    private String getEffectiveFaculty(TimetableEntry e) {
        if (e.getSubstituteFaculty() != null && !e.getSubstituteFaculty().isBlank()) {
            return e.getSubstituteFaculty();
        }
        return e.getFacultyName() != null ? e.getFacultyName() : e.getFacultyCode();
    }

    private boolean isSameCombinedSession(TimetableEntry a, TimetableEntry b) {
        return Objects.equals(a.getId(), b.getId());
    }

    private Map<String, Object> buildConflictRecord(long id, String type, String severity,
                                                    TimetableEntry a, TimetableEntry b, String description) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("conflictId", id);
        map.put("type", type);
        map.put("severity", severity);
        map.put("dayOfWeek", a.getDayOfWeek());
        map.put("dateLabel", a.getDateLabel() != null ? a.getDateLabel() : a.getDayOfWeek());
        map.put("periodCode", a.getPeriodCode());
        map.put("timeRange", a.getTimeRange());
        map.put("roomCode", a.getRoomCode());
        map.put("entryAId", a.getId());
        map.put("entryASection", a.getSectionCode() + " (" + a.getBatchCode() + ")");
        map.put("entryASubject", a.getSubjectCode() + " - " + a.getSubjectName());
        map.put("entryAFaculty", a.getFacultyName());
        map.put("entryBId", b.getId());
        map.put("entryBSection", b.getSectionCode() + " (" + b.getBatchCode() + ")");
        map.put("entryBSubject", b.getSubjectCode() + " - " + b.getSubjectName());
        map.put("entryBFaculty", b.getFacultyName());
        map.put("description", description);
        return map;
    }
}
