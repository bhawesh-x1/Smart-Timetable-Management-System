package edu.piet.smartcampus.service;

import edu.piet.smartcampus.model.Faculty;
import edu.piet.smartcampus.model.Room;
import edu.piet.smartcampus.model.TimetableEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    public String generateRoomUtilizationCsv(List<Map<String, Object>> roomMetrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Room Code,Room Name,Type,Capacity,Weekly Slots,Occupied Slots,Free Slots,Utilization %,Status\n");
        for (Map<String, Object> row : roomMetrics) {
            sb.append(escapeCsv(String.valueOf(row.get("roomCode")))).append(",")
              .append(escapeCsv(String.valueOf(row.get("roomName")))).append(",")
              .append(escapeCsv(String.valueOf(row.get("roomType")))).append(",")
              .append(row.get("capacity")).append(",")
              .append(row.get("totalWeeklySlots")).append(",")
              .append(row.get("occupiedSlots")).append(",")
              .append(row.get("freeSlots")).append(",")
              .append(row.get("utilizationPct")).append("%,")
              .append(escapeCsv(String.valueOf(row.get("status")))).append("\n");
        }
        return sb.toString();
    }

    public String generateTimetableCsv(List<TimetableEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Day,Date,Period,Time Range,Section,Batch,Subject Code,Subject Name,Session Type,Faculty,Substitute,Room,Conflict Status\n");
        for (TimetableEntry e : entries) {
            sb.append(e.getId()).append(",")
              .append(escapeCsv(e.getDayOfWeek())).append(",")
              .append(escapeCsv(e.getDateLabel() != null ? e.getDateLabel() : "-")).append(",")
              .append(escapeCsv(e.getPeriodCode())).append(",")
              .append(escapeCsv(e.getTimeRange())).append(",")
              .append(escapeCsv(e.getSectionCode())).append(",")
              .append(escapeCsv(e.getBatchCode())).append(",")
              .append(escapeCsv(e.getSubjectCode())).append(",")
              .append(escapeCsv(e.getSubjectName())).append(",")
              .append(escapeCsv(e.getSessionType())).append(",")
              .append(escapeCsv(e.getFacultyName())).append(",")
              .append(escapeCsv(e.getSubstituteFaculty() != null ? e.getSubstituteFaculty() : "-")).append(",")
              .append(escapeCsv(e.getRoomCode())).append(",")
              .append(Boolean.TRUE.equals(e.getHasConflict()) ? escapeCsv(e.getConflictReason()) : "OK").append("\n");
        }
        return sb.toString();
    }

    public String generatePrintableHtmlReport(String mode, List<Map<String, Object>> roomMetrics, List<TimetableEntry> entries) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
            .append("<title>PIET Smart Campus Report - ").append(mode).append("</title>")
            .append("<style>")
            .append("body{font-family:'Segoe UI',Arial,sans-serif;margin:28px;color:#0F172A;}")
            .append("h1{margin:0 0 4px 0;font-size:20px;color:#1A2234;}")
            .append("p.sub{margin:0 0 20px 0;color:#64748B;font-size:13px;}")
            .append("table{width:100%;border-collapse:collapse;margin-bottom:26px;font-size:12px;}")
            .append("th{background:#1A2234;color:#FFF;padding:8px 10px;text-align:left;}")
            .append("td{border:1px solid #CBD5E1;padding:7px 10px;}")
            .append("tr:nth-child(even){background:#F8FAFC;}")
            .append(".conflict{background:#FEF2F2!important;color:#B91C1C;font-weight:600;}")
            .append("@media print{.no-print{display:none;}}")
            .append("</style></head><body>")
            .append("<div class='no-print' style='margin-bottom:16px;'>")
            .append("<button onclick='window.print()' style='background:#1E6BF1;color:#fff;border:none;padding:8px 16px;border-radius:6px;cursor:pointer;font-weight:600;'>Print / Save as PDF</button>")
            .append("</div>")
            .append("<h1>POORNIMA INSTITUTE OF ENGINEERING & TECHNOLOGY (PIET)</h1>")
            .append("<p class='sub'>Department of Computer Engineering • Smart Campus Timetable & Room Utilization Report (AY 2026-2027 • ")
            .append(mode).append(")</p>")
            .append("<h3>1. Room & Lab Utilization Summary (36 Weekly Slots Standard)</h3>")
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

        html.append("<h3>2. Scheduled Timetable Entries (").append(entries.size()).append(" Slots)</h3>")
            .append("<table><thead><tr><th>Day / Date</th><th>Period</th><th>Time</th><th>Section (Batch)</th><th>Subject</th><th>Type</th><th>Faculty</th><th>Room</th><th>CSP Status</th></tr></thead><tbody>");

        for (TimetableEntry e : entries) {
            boolean clash = Boolean.TRUE.equals(e.getHasConflict());
            html.append("<tr class='").append(clash ? "conflict" : "").append("'>")
                .append("<td>").append(e.getDayOfWeek()).append(e.getDateLabel() != null ? " (" + e.getDateLabel() + ")" : "").append("</td>")
                .append("<td>").append(e.getPeriodCode()).append("</td>")
                .append("<td>").append(e.getTimeRange()).append("</td>")
                .append("<td>").append(e.getSectionCode()).append(" (").append(e.getBatchCode()).append(")</td>")
                .append("<td><strong>").append(e.getSubjectCode()).append("</strong> - ").append(e.getSubjectName()).append("</td>")
                .append("<td>").append(e.getSessionType()).append("</td>")
                .append("<td>").append(e.getFacultyName()).append("</td>")
                .append("<td>").append(e.getRoomCode()).append("</td>")
                .append("<td>").append(clash ? "CONFLICT: " + e.getConflictReason() : "Verified").append("</td></tr>");
        }
        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
