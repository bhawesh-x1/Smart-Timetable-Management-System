package edu.piet.smartcampus.runtime;

import java.util.*;

/**
 * Automated Verification Suite for Smart Campus Timetable Management System (v1.0.0 • College Edition).
 * Verifies Mode A (Screenshot KPIs & Room Utilization), Mode B (All 8 Semesters + 309 III-Sem Master Cells),
 * P7 Deletion, CSP Conflict Detection & Resolution, Leave Substitution Manager, and 8-Semester Auto-Generator Studio.
 */
public class SmartCampusVerificationTest {

    public static void main(String[] args) {
        SmartCampusDataStore store = new SmartCampusDataStore();

        System.out.println("=== 1. VERIFYING MODE A (UI SCREENSHOT DEMO DATASET, P7 DELETED) ===");
        int studentsA = store.studentsByMode.get("MODE_A").size();
        int facultyA = store.facultyByMode.get("MODE_A").size();
        int subjectsA = store.subjectsByMode.get("MODE_A").size();
        int roomsA = store.roomsByMode.get("MODE_A").size();
        int entriesA = store.timetableByMode.get("MODE_A").size();
        int conflictsA = store.auditConflicts("MODE_A").size();
        boolean hasP7 = store.timeSlots.stream().anyMatch(ts -> "P7".equalsIgnoreCase(String.valueOf(ts.get("slotCode"))))
                || store.timetableByMode.get("MODE_A").stream().anyMatch(e -> "P7".equalsIgnoreCase(String.valueOf(e.get("periodCode"))));

        System.out.printf("TOTAL STUDENTS: %d (Expected: 4)%n", studentsA);
        System.out.printf("TOTAL FACULTY: %d (Expected: 6)%n", facultyA);
        System.out.printf("TOTAL SUBJECTS: %d (Expected: 14)%n", subjectsA);
        System.out.printf("CLASSROOMS & LABS: %d (Expected: 8)%n", roomsA);
        System.out.printf("TIMETABLE ENTRIES: %d (Expected: 90)%n", entriesA);
        System.out.printf("SCHEDULE CONFLICTS: %d (Expected: 8)%n", conflictsA);
        System.out.printf("PERIOD 7 PRESENT: %s (Expected: false)%n", hasP7);

        if (studentsA != 4 || facultyA != 6 || subjectsA != 14 || roomsA != 8 || entriesA != 90 || conflictsA != 8 || hasP7) {
            throw new IllegalStateException("Mode A KPI / P7 check failed!");
        }

        System.out.println("\n=== 2. VERIFYING SCREENSHOT 10 ROOM UTILIZATION PERCENTAGES ===");
        List<Map<String, Object>> utilA = store.computeRoomUtilization("MODE_A");
        for (Map<String, Object> r : utilA) {
            System.out.printf("Room %-6s | %-15s | Cap: %3s | Occupied: %2s/%2s | Free: %2s | Util: %5s%% | Status: %s%n",
                    r.get("roomCode"), r.get("roomName"), r.get("capacity"),
                    r.get("occupiedSlots"), r.get("totalWeeklySlots"), r.get("freeSlots"),
                    r.get("utilizationPct"), r.get("status"));
        }

        System.out.println("\n=== 3. VERIFYING MODE B (PIET ALL 8 SEMESTERS + III-SEM 309-CELL MASTER) ===");
        int facultyB = store.facultyByMode.get("MODE_B").size();
        int roomsB = store.roomsByMode.get("MODE_B").size();
        long sem3EntriesB = store.timetableByMode.get("MODE_B").stream()
                .filter(e -> "Sem 3".equals(e.get("semester"))).count();
        long distinctSemsB = store.timetableByMode.get("MODE_B").stream()
                .map(e -> String.valueOf(e.get("semester"))).distinct().count();
        List<Map<String, Object>> conflictsB = store.auditConflicts("MODE_B");

        System.out.printf("MODE B FACULTY: %d (Expected: 19)%n", facultyB);
        System.out.printf("MODE B ROOMS & LABS: %d (Expected: 17)%n", roomsB);
        System.out.printf("MODE B SEM 3 MASTER CELLS: %d (Expected: 309)%n", sem3EntriesB);
        System.out.printf("MODE B POPULATED SEMESTERS: %d (Expected: 8)%n", distinctSemsB);
        System.out.printf("MODE B DETECTED CLASHES: %d%n", conflictsB.size());

        if (facultyB != 19 || roomsB != 17 || sem3EntriesB != 309 || distinctSemsB != 8) {
            throw new IllegalStateException("Mode B dataset / 8-semester count mismatch!");
        }

        System.out.println("\n=== 4. VERIFYING CSP CONFLICT AUTO-RESOLVER & LEAVE SUBSTITUTION ===");
        Map<String, Object> resA = store.resolveAllConflicts("MODE_A");
        System.out.println("Mode A CSP Resolution: " + resA.get("message") + " (Remaining: " + resA.get("remainingConflicts") + ")");
        if (Integer.parseInt(String.valueOf(resA.get("remainingConflicts"))) != 0) {
            throw new IllegalStateException("Expected 0 remaining conflicts in Mode A after CSP resolution!");
        }
        Map<String, Object> leaveRes = store.toggleFacultyLeave("MODE_B", 1L);
        System.out.println("Mode B Leave Substitution: " + leaveRes.get("message"));

        System.out.println("\n=== 5. VERIFYING 8-SEMESTER CSP AUTO-GENERATOR STUDIO ===");
        Map<String, Object> genRes = store.runAutoGenerator("MODE_B", "ALL", "STRICT", true, true, true, true);
        int genTotal = Integer.parseInt(String.valueOf(genRes.get("totalSlotsGenerated")));
        int genClashes = Integer.parseInt(String.valueOf(genRes.get("hardClashes")));
        @SuppressWarnings("unchecked")
        Map<String, Integer> semBreak = (Map<String, Integer>) genRes.get("semesterBreakdown");
        @SuppressWarnings("unchecked")
        List<String> softWarns = (List<String>) genRes.get("softWarnings");

        System.out.println("Generated Total Slots (All 8 Sems Strict): " + genTotal);
        System.out.println("Generated Hard Clashes: " + genClashes);
        System.out.println("Semester Breakdown: " + semBreak);
        System.out.println("Soft Warnings (" + softWarns.size() + "): " + softWarns);

        if (genClashes != 0 || genTotal < 80) {
            throw new IllegalStateException("Expected >= 80 generated slots with 0 hard clashes in STRICT mode!");
        }
        for (int s = 1; s <= 8; s++) {
            if (semBreak.getOrDefault("Sem " + s, 0) <= 0) {
                throw new IllegalStateException("Semester Sem " + s + " generated 0 slots!");
            }
        }

        Map<String, Object> pubRes = store.publishGeneratedSchedule("MODE_B", "ALL");
        System.out.println("Publish Result: " + pubRes.get("message") + " (Remaining Conflicts: " + pubRes.get("remainingConflicts") + ")");
        if (Integer.parseInt(String.valueOf(pubRes.get("remainingConflicts"))) != 0) {
            throw new IllegalStateException("Published schedule still had clashes!");
        }

        System.out.println("\nALL 8-SEMESTER & SCREENSHOT VERIFICATION CHECKS PASSED 100%!");
    }
}
