package edu.piet.smartcampus.service;

import java.util.*;

/**
 * Shared credential authentication helper used by both Spring Boot REST Controller
 * and Standalone JDK SmartCampusServer. Zero third-party dependencies.
 */
public final class AuthHelper {

    public record AuthCandidate(
            String code,
            String abbr,
            String fullName,
            String email,
            String phone,
            String sectionBatch,
            String semester
    ) {}

    private AuthHelper() {}

    public static Map<String, Object> authenticate(String username,
                                                   String password,
                                                   String roleCategory,
                                                   String requestedMode,
                                                   List<AuthCandidate> facultyList,
                                                   List<AuthCandidate> studentList) {
        if (isBlank(username) || isBlank(password)) {
            return null;
        }
        String u = username.trim();
        String p = password.trim();
        String cat = isBlank(roleCategory) ? "" : roleCategory.trim().toUpperCase(Locale.ROOT);
        String mode = "MODE_B".equalsIgnoreCase(requestedMode) ? "MODE_B" : "MODE_A";

        // 1. Administrator Account
        if (cat.isEmpty() || "ADMIN".equals(cat)) {
            boolean adminUserMatch = u.equalsIgnoreCase("admin@piet.edu")
                    || u.equalsIgnoreCase("admin")
                    || u.equalsIgnoreCase("PIET-ADMIN")
                    || u.equalsIgnoreCase("sysadmin");
            boolean adminPassMatch = p.equals("admin123") || p.equals("piet2026") || p.equals("smartcampus");
            if (adminUserMatch && adminPassMatch) {
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("authenticated", true);
                res.put("token", "sc-admin-" + UUID.randomUUID().toString().substring(0, 8));
                res.put("role", "ADMIN");
                res.put("roleCategory", "ADMIN");
                res.put("displayName", "System Administrator");
                res.put("email", "admin@piet.edu");
                res.put("userCode", "PIET-ADMIN");
                res.put("mode", mode);
                return res;
            }
        }

        // 2. Faculty Accounts (Mode A + Mode B)
        if ((cat.isEmpty() || "FACULTY".equals(cat)) && facultyList != null) {
            for (AuthCandidate f : facultyList) {
                boolean idMatch = equalsAnyIgnoreCase(u, f.email(), f.code(), f.abbr(), f.fullName());
                boolean passMatch = p.equals("faculty123") || p.equals("piet123")
                        || (!isBlank(f.phone()) && p.equals(f.phone().trim()));
                if (idMatch && passMatch) {
                    String roleKey = "FACULTY_CUSTOM";
                    if (f.fullName() != null) {
                        if (f.fullName().toLowerCase(Locale.ROOT).contains("rajesh verma")) roleKey = "FACULTY_RV";
                        else if (f.fullName().toLowerCase(Locale.ROOT).contains("varsha kumari")) roleKey = "FACULTY_VK";
                    }
                    Map<String, Object> res = new LinkedHashMap<>();
                    res.put("authenticated", true);
                    res.put("token", "sc-fac-" + UUID.randomUUID().toString().substring(0, 8));
                    res.put("role", roleKey);
                    res.put("roleCategory", "FACULTY");
                    res.put("displayName", safe(f.fullName()));
                    res.put("email", safe(f.email()));
                    res.put("userCode", safe(f.code()));
                    res.put("facultyName", safe(f.fullName()));
                    res.put("mode", mode);
                    return res;
                }
            }
        }

        // 3. Student Accounts (Mode A + Mode B)
        if ((cat.isEmpty() || "STUDENT".equals(cat)) && studentList != null) {
            for (AuthCandidate s : studentList) {
                boolean idMatch = equalsAnyIgnoreCase(u, s.email(), s.code(), s.fullName());
                boolean passMatch = p.equals("student123") || p.equals("piet123")
                        || (!isBlank(s.phone()) && p.equals(s.phone().trim()));
                if (idMatch && passMatch) {
                    String roleKey = (s.fullName() != null && s.fullName().toLowerCase(Locale.ROOT).contains("bhawesh"))
                            ? "STUDENT_BS"
                            : "STUDENT_CUSTOM";
                    String semNum = extractSemNumber(s.semester());
                    String secLetter = extractSectionLetter(s.sectionBatch());
                    String sectionCode = ("3".equals(semNum) && "D".equals(secLetter))
                            ? "3CSRD"
                            : (semNum + "CS" + secLetter);
                    String batchCode = extractBatchCode(s.sectionBatch(), secLetter);

                    Map<String, Object> res = new LinkedHashMap<>();
                    res.put("authenticated", true);
                    res.put("token", "sc-stu-" + UUID.randomUUID().toString().substring(0, 8));
                    res.put("role", roleKey);
                    res.put("roleCategory", "STUDENT");
                    res.put("displayName", safe(s.fullName()));
                    res.put("email", safe(s.email()));
                    res.put("userCode", safe(s.code()));
                    res.put("studentSemester", isBlank(s.semester()) ? "Sem 3" : s.semester());
                    res.put("studentSection", sectionCode);
                    res.put("studentBatch", batchCode);
                    res.put("mode", mode);
                    return res;
                }
            }
        }

        return null;
    }

    private static String extractSemNumber(String sem) {
        if (isBlank(sem)) return "3";
        String digits = sem.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? "3" : digits;
    }

    private static String extractSectionLetter(String sb) {
        if (isBlank(sb)) return "A";
        String t = sb.trim().toUpperCase(Locale.ROOT);
        char c = t.charAt(0);
        return (c >= 'A' && c <= 'Z') ? String.valueOf(c) : "A";
    }

    private static String extractBatchCode(String sb, String secLetter) {
        if (!isBlank(sb)) {
            int idx = sb.toUpperCase(Locale.ROOT).indexOf("BATCH ");
            if (idx >= 0) {
                String tail = sb.substring(idx + 6).replace(")", "").trim();
                if (!tail.isEmpty()) return tail;
            }
        }
        return secLetter + "1";
    }

    private static boolean equalsAnyIgnoreCase(String target, String... candidates) {
        if (target == null || candidates == null) return false;
        for (String c : candidates) {
            if (c != null && target.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
