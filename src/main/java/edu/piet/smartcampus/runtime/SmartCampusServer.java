package edu.piet.smartcampus.runtime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.piet.smartcampus.service.AuthHelper;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Standalone Enterprise HTTP & REST Server for Smart Campus Timetable Management System (v1.0.0 • College Edition).
 * Runs out-of-the-box on JDK 17/21 using com.sun.net.httpserver.HttpServer on port 8080.
 */
public class SmartCampusServer {

    private static final SmartCampusDataStore DATA_STORE = new SmartCampusDataStore();

    public static void main(String[] args) throws Exception {
        int port = 8085;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        startServer(port);
    }

    public static HttpServer startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve Frontend UI
        server.createContext("/", SmartCampusServer::handleFrontend);

        // Authentication API
        server.createContext("/api/auth/login", SmartCampusServer::handleAuthLogin);
        server.createContext("/api/auth/logout", SmartCampusServer::handleAuthLogout);

        // Serve REST API State & CRUD
        server.createContext("/api/state", SmartCampusServer::handleGetState);
        server.createContext("/api/entity/", SmartCampusServer::handleEntityCrud);
        server.createContext("/api/csp/resolve/", SmartCampusServer::handleResolveSingleConflict);
        server.createContext("/api/csp/resolve-all", SmartCampusServer::handleResolveAllConflicts);
        server.createContext("/api/faculty/leave/", SmartCampusServer::handleToggleLeave);
        server.createContext("/api/generator/run", SmartCampusServer::handleRunGenerator);
        server.createContext("/api/generator/preview-update", SmartCampusServer::handlePreviewUpdate);
        server.createContext("/api/generator/publish", SmartCampusServer::handlePublishGenerator);
        server.createContext("/api/reset", SmartCampusServer::handleReset);

        // Serve Export Suite (CSV, HTML/PDF)
        server.createContext("/api/export/rooms.csv", SmartCampusServer::handleExportRoomsCsv);
        server.createContext("/api/export/timetable.csv", SmartCampusServer::handleExportTimetableCsv);
        server.createContext("/api/export/report.html", SmartCampusServer::handleExportHtmlReport);

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("================================================================================");
        System.out.println(" SMART CAMPUS TIMETABLE MANAGEMENT SYSTEM (v1.0.0 • College Edition)");
        System.out.println(" Poornima Institute of Engineering & Technology (PIET) - Dept. of Computer Engg.");
        System.out.println(" Server listening at: http://localhost:" + port);
        System.out.println(" Mode A: UI Screenshot Demo Dataset (90 Entries, 8 Conflicts, 8 Rooms)");
        System.out.println(" Mode B: PIET III-Sem Master Excel Dataset (309 Cells across Oct 5-9, 2026)");
        System.out.println("================================================================================");
        return server;
    }

    public static boolean isServerReachable(int port) {
        try {
            java.net.URL url = java.net.URI.create("http://127.0.0.1:" + port + "/api/state?mode=MODE_A").toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1200);
            conn.setReadTimeout(1200);
            conn.setRequestMethod("GET");
            return conn.getResponseCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static SmartCampusDataStore getDataStore() {
        return DATA_STORE;
    }

    private static void handleFrontend(HttpExchange ex) throws IOException {
        String reqPath = ex.getRequestURI().getPath();
        String templateFile = ("/login".equalsIgnoreCase(reqPath) || "/login.html".equalsIgnoreCase(reqPath))
                ? "login.html"
                : "index.html";
        byte[] bytes = null;
        Path htmlPath = Paths.get("src", "main", "resources", "templates", templateFile);
        if (Files.exists(htmlPath)) {
            bytes = Files.readAllBytes(htmlPath);
        } else {
            try (InputStream is = SmartCampusServer.class.getResourceAsStream("/templates/" + templateFile)) {
                if (is != null) bytes = is.readAllBytes();
            }
        }
        if (bytes == null) {
            sendText(ex, 404, "text/plain", "Template not found: " + templateFile);
            return;
        }
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleAuthLogin(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, Map.of("error", "Method not allowed"));
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> payload = parseSimpleJsonObject(body);
        String username = String.valueOf(payload.getOrDefault("username", ""));
        String password = String.valueOf(payload.getOrDefault("password", ""));
        String roleCategory = String.valueOf(payload.getOrDefault("roleCategory", ""));
        String mode = String.valueOf(payload.getOrDefault("mode", "MODE_A"));

        List<AuthHelper.AuthCandidate> facCandidates = new ArrayList<>();
        for (String m : Arrays.asList("MODE_A", "MODE_B")) {
            for (Map<String, Object> f : DATA_STORE.facultyByMode.getOrDefault(m, Collections.emptyList())) {
                facCandidates.add(new AuthHelper.AuthCandidate(
                        strOrEmpty(f.get("facultyCode")),
                        strOrEmpty(f.get("shortAbbr")),
                        strOrEmpty(f.get("fullName")),
                        strOrEmpty(f.get("email")),
                        strOrEmpty(f.get("phone")),
                        null, null));
            }
        }
        List<AuthHelper.AuthCandidate> stuCandidates = new ArrayList<>();
        for (String m : Arrays.asList("MODE_A", "MODE_B")) {
            for (Map<String, Object> s : DATA_STORE.studentsByMode.getOrDefault(m, Collections.emptyList())) {
                stuCandidates.add(new AuthHelper.AuthCandidate(
                        strOrEmpty(s.get("enrollmentNo")),
                        null,
                        strOrEmpty(s.get("fullName")),
                        strOrEmpty(s.get("email")),
                        strOrEmpty(s.get("phone")),
                        strOrEmpty(s.get("sectionBatch")),
                        strOrEmpty(s.get("semester"))));
            }
        }

        Map<String, Object> result = AuthHelper.authenticate(username, password, roleCategory, mode, facCandidates, stuCandidates);
        if (result == null) {
            sendJson(ex, 401, Map.of("authenticated", false, "error", "Invalid Institutional ID/Email or Password for selected role."));
            return;
        }
        sendJson(ex, 200, result);
    }

    private static void handleAuthLogout(HttpExchange ex) throws IOException {
        sendJson(ex, 200, Map.of("authenticated", false, "status", "LOGGED_OUT"));
    }

    private static String strOrEmpty(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    private static void handleGetState(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String mode = getQueryParam(query, "mode", "MODE_A");
        String semester = getQueryParam(query, "semester", "ALL");
        List<Map<String, Object>> conflicts = DATA_STORE.auditConflicts(mode);
        List<Map<String, Object>> roomUtilization = DATA_STORE.computeRoomUtilization(mode, semester);
        List<Map<String, Object>> facultyWorkload = DATA_STORE.computeFacultyWorkload(mode, semester);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mode", mode);
        response.put("semester", semester);
        response.put("semesters", DATA_STORE.semesters);
        response.put("departments", DATA_STORE.departments);
        response.put("courses", DATA_STORE.courses);
        response.put("sections", DATA_STORE.sections);
        response.put("timeSlots", DATA_STORE.timeSlots);
        response.put("students", DATA_STORE.studentsByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("faculty", DATA_STORE.facultyByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("subjects", DATA_STORE.subjectsByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("rooms", DATA_STORE.roomsByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("timetableEntries", DATA_STORE.timetableByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("conflicts", conflicts);
        response.put("roomUtilization", roomUtilization);
        response.put("facultyWorkload", facultyWorkload);
        response.put("generatorPreview", DATA_STORE.generatorPreviewByMode.getOrDefault(mode, Collections.emptyList()));
        response.put("generatorMeta", DATA_STORE.generatorMetaByMode.getOrDefault(mode, Collections.emptyMap()));

        sendJson(ex, 200, response);
    }

    private static void handleEntityCrud(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); // /api/entity/{type} or /api/entity/{type}/{id}
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendJson(ex, 400, Map.of("error", "Invalid entity path"));
            return;
        }
        String entityType = parts[3];
        String mode = getQueryParam(ex.getRequestURI().getQuery(), "mode", "MODE_A");
        String method = ex.getRequestMethod().toUpperCase();

        if ("POST".equals(method) || "PUT".equals(method)) {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> payload = parseSimpleJsonObject(body);
            Map<String, Object> saved = DATA_STORE.saveEntity(mode, entityType, payload);
            sendJson(ex, 200, saved);
        } else if ("DELETE".equals(method)) {
            if (parts.length < 5) {
                sendJson(ex, 400, Map.of("error", "Missing entity ID"));
                return;
            }
            long id = Long.parseLong(parts[4]);
            boolean deleted = DATA_STORE.deleteEntity(mode, entityType, id);
            sendJson(ex, 200, Map.of("deleted", deleted, "id", id));
        } else {
            sendJson(ex, 405, Map.of("error", "Method not allowed"));
        }
    }

    private static void handleResolveSingleConflict(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        long entryId = Long.parseLong(parts[parts.length - 1]);
        String mode = getQueryParam(ex.getRequestURI().getQuery(), "mode", "MODE_A");
        Map<String, Object> result = DATA_STORE.resolveConflictByEntryId(mode, entryId);
        sendJson(ex, 200, result);
    }

    private static void handleResolveAllConflicts(HttpExchange ex) throws IOException {
        String mode = getQueryParam(ex.getRequestURI().getQuery(), "mode", "MODE_A");
        Map<String, Object> result = DATA_STORE.resolveAllConflicts(mode);
        sendJson(ex, 200, result);
    }

    private static void handleToggleLeave(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        long facId = Long.parseLong(parts[parts.length - 1]);
        String mode = getQueryParam(ex.getRequestURI().getQuery(), "mode", "MODE_A");
        Map<String, Object> result = DATA_STORE.toggleFacultyLeave(mode, facId);
        sendJson(ex, 200, result);
    }

    private static void handleRunGenerator(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> payload = body.isBlank() ? Collections.emptyMap() : parseSimpleJsonObject(body);

        String mode = String.valueOf(payload.getOrDefault("mode", getQueryParam(query, "mode", "MODE_B")));
        String targetScope = String.valueOf(payload.getOrDefault("targetScope", getQueryParam(query, "targetScope", "ALL")));
        String enforcementLevel = String.valueOf(payload.getOrDefault("enforcementLevel", getQueryParam(query, "enforcementLevel", "STRICT")));
        boolean minimizeGaps = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("minimizeGaps", "true")));
        boolean balanceLabs = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("balanceLabs", "true")));
        boolean respectCapacity = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("respectCapacity", "true")));
        boolean enforceMaxLoad = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("enforceMaxLoad", "true")));

        Map<String, Object> result = DATA_STORE.runAutoGenerator(
                mode, targetScope, enforcementLevel, minimizeGaps, balanceLabs, respectCapacity, enforceMaxLoad);
        sendJson(ex, 200, result);
    }

    private static void handlePreviewUpdate(HttpExchange ex) throws IOException {
        String mode = getQueryParam(ex.getRequestURI().getQuery(), "mode", "MODE_B");
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> payload = parseSimpleJsonObject(body);
        if (payload.containsKey("mode")) mode = String.valueOf(payload.get("mode"));
        long entryId = Long.parseLong(String.valueOf(payload.getOrDefault("entryId", "0")));
        Map<String, Object> result = DATA_STORE.updatePreviewSlot(mode, entryId, payload);
        sendJson(ex, 200, result);
    }

    private static void handlePublishGenerator(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> payload = body.isBlank() ? Collections.emptyMap() : parseSimpleJsonObject(body);
        String mode = String.valueOf(payload.getOrDefault("mode", getQueryParam(query, "mode", "MODE_B")));
        String targetScope = String.valueOf(payload.getOrDefault("targetScope", getQueryParam(query, "targetScope", "ALL")));
        Map<String, Object> result = DATA_STORE.publishGeneratedSchedule(mode, targetScope);
        sendJson(ex, 200, result);
    }

    private static void handleReset(HttpExchange ex) throws IOException {
        DATA_STORE.resetAllData();
        sendJson(ex, 200, Map.of("status", "RESET_COMPLETE", "message", "Both Mode A (Screenshots) and Mode B (PIET Sem 1-8 + III-Sem 309-Cell Master) datasets restored."));
    }

    private static void handleExportRoomsCsv(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String mode = getQueryParam(query, "mode", "MODE_A");
        String semester = getQueryParam(query, "semester", "ALL");
        List<Map<String, Object>> metrics = DATA_STORE.computeRoomUtilization(mode, semester);
        StringBuilder sb = new StringBuilder();
        sb.append("Room Code,Room Name,Type,Capacity,Weekly Slots,Occupied Slots,Free Slots,Utilization %,Status\n");
        for (Map<String, Object> r : metrics) {
            sb.append(csv(r.get("roomCode"))).append(",")
              .append(csv(r.get("roomName"))).append(",")
              .append(csv(r.get("roomType"))).append(",")
              .append(r.get("capacity")).append(",")
              .append(r.get("totalWeeklySlots")).append(",")
              .append(r.get("occupiedSlots")).append(",")
              .append(r.get("freeSlots")).append(",")
              .append(r.get("utilizationPct")).append("%,")
              .append(csv(r.get("status"))).append("\n");
        }
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"PIET_Room_Utilization_" + mode + "_" + semester + ".csv\"");
        sendText(ex, 200, "text/csv; charset=UTF-8", sb.toString());
    }

    private static void handleExportTimetableCsv(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String mode = getQueryParam(query, "mode", "MODE_A");
        String semester = getQueryParam(query, "semester", "ALL");
        DATA_STORE.auditConflicts(mode);
        List<Map<String, Object>> allEntries = DATA_STORE.timetableByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> entries = "ALL".equalsIgnoreCase(semester)
                ? allEntries
                : allEntries.stream().filter(e -> semester.equalsIgnoreCase(String.valueOf(e.get("semester")))).toList();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Semester,Day,Date,Period,Time Range,Section,Batch,Subject Code,Subject Name,Session Type,Faculty,Substitute,Room,Conflict Status\n");
        for (Map<String, Object> e : entries) {
            boolean clash = Boolean.parseBoolean(String.valueOf(e.get("hasConflict")));
            sb.append(e.get("id")).append(",")
              .append(csv(e.get("semester"))).append(",")
              .append(csv(e.get("dayOfWeek"))).append(",")
              .append(csv(e.get("dateLabel"))).append(",")
              .append(csv(e.get("periodCode"))).append(",")
              .append(csv(e.get("timeRange"))).append(",")
              .append(csv(e.get("sectionCode"))).append(",")
              .append(csv(e.get("batchCode"))).append(",")
              .append(csv(e.get("subjectCode"))).append(",")
              .append(csv(e.get("subjectName"))).append(",")
              .append(csv(e.get("sessionType"))).append(",")
              .append(csv(e.get("facultyName"))).append(",")
              .append(csv(e.get("substituteFaculty"))).append(",")
              .append(csv(e.get("roomCode"))).append(",")
              .append(csv(clash ? e.get("conflictReason") : "Verified")).append("\n");
        }
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"PIET_Master_Timetable_" + mode + "_" + semester + ".csv\"");
        sendText(ex, 200, "text/csv; charset=UTF-8", sb.toString());
    }

    private static void handleExportHtmlReport(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        String mode = getQueryParam(query, "mode", "MODE_A");
        String semester = getQueryParam(query, "semester", "ALL");
        List<Map<String, Object>> roomMetrics = DATA_STORE.computeRoomUtilization(mode, semester);
        List<Map<String, Object>> conflicts = DATA_STORE.auditConflicts(mode);
        List<Map<String, Object>> allEntries = DATA_STORE.timetableByMode.getOrDefault(mode, Collections.emptyList());
        List<Map<String, Object>> entries = "ALL".equalsIgnoreCase(semester)
                ? allEntries
                : allEntries.stream().filter(e -> semester.equalsIgnoreCase(String.valueOf(e.get("semester")))).toList();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
            .append("<title>PIET Smart Campus Timetable & Analytics Report (").append(mode).append(")</title>")
            .append("<style>")
            .append("body{font-family:'Segoe UI',Inter,Arial,sans-serif;margin:28px;color:#0F172A;background:#FFFFFF;}")
            .append(".hdr{display:flex;justify-content:space-between;align-items:center;border-bottom:2px solid #1A2234;padding-bottom:14px;margin-bottom:20px;}")
            .append("h1{margin:0;font-size:20px;color:#1A2234;letter-spacing:0.4px;}")
            .append(".sub{margin:4px 0 0 0;color:#475569;font-size:13px;}")
            .append(".btn{background:#1E6BF1;color:#fff;border:none;padding:9px 18px;border-radius:6px;font-weight:600;cursor:pointer;font-size:13px;}")
            .append("h2{font-size:15px;color:#1A2234;margin:22px 0 10px 0;text-transform:uppercase;}")
            .append("table{width:100%;border-collapse:collapse;font-size:12px;margin-bottom:22px;}")
            .append("th{background:#1A2234;color:#FFFFFF;padding:8px 10px;text-align:left;font-weight:600;}")
            .append("td{border:1px solid #CBD5E1;padding:7px 10px;}")
            .append("tr:nth-child(even){background:#F8FAFC;}")
            .append(".clash{background:#FEF2F2!important;color:#B91C1C;font-weight:600;}")
            .append("@media print{.no-print{display:none!important;}}")
            .append("</style></head><body>")
            .append("<div class='hdr'><div>")
            .append("<h1>POORNIMA INSTITUTE OF ENGINEERING & TECHNOLOGY (PIET)</h1>")
            .append("<p class='sub'>Department of Computer Engineering • Smart Campus Timetable & Analytics Report • Session 2026–2027 (")
            .append("MODE_B".equals(mode) ? "PIET III-Sem Master Excel Dataset - 309 Cells" : "UI Screenshot Demo Dataset - 90 Slots")
            .append(")</p></div>")
            .append("<div class='no-print'><button class='btn' onclick='window.print()'>Print / Save as PDF</button></div></div>");

        html.append("<h2>1. Room & Lab Utilization Summary (36 Weekly Slots Standard)</h2>")
            .append("<table><thead><tr><th>Room Code</th><th>Room Name</th><th>Type</th><th>Capacity</th><th>Weekly Slots</th><th>Occupied</th><th>Free</th><th>Utilization</th><th>Status</th></tr></thead><tbody>");
        for (Map<String, Object> r : roomMetrics) {
            html.append("<tr><td><strong>").append(r.get("roomCode")).append("</strong></td>")
                .append("<td>").append(r.get("roomName")).append("</td>")
                .append("<td>").append(r.get("roomType")).append("</td>")
                .append("<td>").append(r.get("capacity")).append("</td>")
                .append("<td>").append(r.get("totalWeeklySlots")).append("</td>")
                .append("<td>").append(r.get("occupiedSlots")).append("</td>")
                .append("<td>").append(r.get("freeSlots")).append("</td>")
                .append("<td><strong>").append(r.get("utilizationPct")).append("%</strong></td>")
                .append("<td>").append(r.get("status")).append("</td></tr>");
        }
        html.append("</tbody></table>");

        html.append("<h2>2. Active CSP Conflict Audit Log (").append(conflicts.size()).append(" Clashes)</h2>");
        if (conflicts.isEmpty()) {
            html.append("<p style='color:#059669;font-weight:600;'>Zero schedule conflicts detected. All CSP hard constraints satisfied.</p>");
        } else {
            html.append("<table><thead><tr><th>#</th><th>Type</th><th>Day / Date</th><th>Period</th><th>Resource</th><th>Conflict Description</th></tr></thead><tbody>");
            for (Map<String, Object> c : conflicts) {
                html.append("<tr class='clash'><td>").append(c.get("conflictId")).append("</td>")
                    .append("<td>").append(c.get("type")).append("</td>")
                    .append("<td>").append(c.get("dayOfWeek")).append(" (").append(c.get("dateLabel")).append(")</td>")
                    .append("<td>").append(c.get("periodCode")).append("</td>")
                    .append("<td>").append(c.get("resource")).append("</td>")
                    .append("<td>").append(c.get("description")).append("</td></tr>");
            }
            html.append("</tbody></table>");
        }

        html.append("<h2>3. Complete Scheduled Timetable Entries (").append(entries.size()).append(" Slots)</h2>")
            .append("<table><thead><tr><th>ID</th><th>Day / Date</th><th>Period</th><th>Time Range</th><th>Section (Batch)</th><th>Subject</th><th>Type</th><th>Faculty</th><th>Room</th><th>CSP Status</th></tr></thead><tbody>");
        for (Map<String, Object> e : entries) {
            boolean clash = Boolean.parseBoolean(String.valueOf(e.get("hasConflict")));
            html.append("<tr class='").append(clash ? "clash" : "").append("'>")
                .append("<td>").append(e.get("id")).append("</td>")
                .append("<td>").append(e.get("dayOfWeek")).append(" (").append(e.get("dateLabel")).append(")</td>")
                .append("<td>").append(e.get("periodCode")).append("</td>")
                .append("<td>").append(e.get("timeRange")).append("</td>")
                .append("<td>").append(e.get("sectionCode")).append(" (").append(e.get("batchCode")).append(")</td>")
                .append("<td><strong>").append(e.get("subjectCode")).append("</strong> - ").append(e.get("subjectName")).append("</td>")
                .append("<td>").append(e.get("sessionType")).append("</td>")
                .append("<td>").append(e.get("facultyName")).append("</td>")
                .append("<td>").append(e.get("roomCode")).append("</td>")
                .append("<td>").append(clash ? "CLASH: " + e.get("conflictReason") : "Verified").append("</td></tr>");
        }
        html.append("</tbody></table></body></html>");
        sendText(ex, 200, "text/html; charset=UTF-8", html.toString());
    }

    private static String getQueryParam(String query, String key, String defaultVal) {
        if (query == null || query.isBlank()) return defaultVal;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                if (k.equals(key)) return v;
            }
        }
        return defaultVal;
    }

    private static String csv(Object val) {
        if (val == null) return "";
        String s = String.valueOf(val);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static void sendJson(HttpExchange ex, int status, Object obj) throws IOException {
        String json = toJson(obj);
        sendText(ex, status, "application/json; charset=UTF-8", json);
    }

    private static void sendText(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    @SuppressWarnings("unchecked")
    public static String toJson(Object val) {
        if (val == null) return "null";
        if (val instanceof Number || val instanceof Boolean) return val.toString();
        if (val instanceof String s) return "\"" + escapeJson(s) + "\"";
        if (val instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":")
                  .append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (val instanceof Collection<?> col) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(String.valueOf(val)) + "\"";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static Map<String, Object> parseSimpleJsonObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        int i = 0;
        int n = trimmed.length();
        while (i < n) {
            while (i < n && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) i++;
            if (i >= n) break;
            if (trimmed.charAt(i) != '"') break;
            int keyStart = ++i;
            StringBuilder key = new StringBuilder();
            while (i < n) {
                char c = trimmed.charAt(i++);
                if (c == '\\' && i < n) {
                    key.append(trimmed.charAt(i++));
                } else if (c == '"') {
                    break;
                } else {
                    key.append(c);
                }
            }
            while (i < n && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ':')) i++;
            if (i >= n) break;

            Object value;
            char vc = trimmed.charAt(i);
            if (vc == '"') {
                i++;
                StringBuilder val = new StringBuilder();
                while (i < n) {
                    char c = trimmed.charAt(i++);
                    if (c == '\\' && i < n) {
                        char esc = trimmed.charAt(i++);
                        if (esc == 'n') val.append('\n');
                        else if (esc == 't') val.append('\t');
                        else val.append(esc);
                    } else if (c == '"') {
                        break;
                    } else {
                        val.append(c);
                    }
                }
                value = val.toString();
            } else {
                int valStart = i;
                while (i < n && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') i++;
                String raw = trimmed.substring(valStart, i).trim();
                if ("true".equalsIgnoreCase(raw)) value = true;
                else if ("false".equalsIgnoreCase(raw)) value = false;
                else if ("null".equalsIgnoreCase(raw)) value = "";
                else {
                    try {
                        if (raw.contains(".")) value = Double.parseDouble(raw);
                        else value = Long.parseLong(raw);
                    } catch (NumberFormatException ex) {
                        value = raw;
                    }
                }
            }
            map.put(key.toString(), value);
        }
        return map;
    }
}
