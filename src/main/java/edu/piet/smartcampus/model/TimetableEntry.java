package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable_entries")
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek;

    @Column(name = "date_label", length = 30)
    private String dateLabel;

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode;

    @Column(name = "time_range", nullable = false, length = 50)
    private String timeRange;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "batch_code", nullable = false, length = 30)
    private String batchCode;

    @Column(name = "subject_code", nullable = false, length = 50)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false, length = 180)
    private String subjectName;

    @Column(name = "session_type", nullable = false, length = 30)
    private String sessionType;

    @Column(name = "faculty_code", nullable = false, length = 40)
    private String facultyCode;

    @Column(name = "faculty_name", nullable = false, length = 120)
    private String facultyName;

    @Column(name = "substitute_faculty", length = 120)
    private String substituteFaculty;

    @Column(name = "room_code", nullable = false, length = 60)
    private String roomCode;

    @Column(name = "color_theme", length = 30)
    private String colorTheme;

    @Column(name = "has_conflict", nullable = false)
    private Boolean hasConflict = false;

    @Column(name = "conflict_reason", length = 255)
    private String conflictReason;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "MODE_A";

    @Column(name = "semester", nullable = false, length = 20)
    private String semester = "Sem 3";

    @Column(name = "is_generated", nullable = false)
    private Boolean isGenerated = false;

    public TimetableEntry() {}

    public TimetableEntry(String dayOfWeek, String dateLabel, String periodCode, String timeRange,
                          String sectionCode, String batchCode, String subjectCode, String subjectName,
                          String sessionType, String facultyCode, String facultyName, String roomCode,
                          String colorTheme, Boolean hasConflict, String conflictReason, String datasetMode) {
        this.dayOfWeek = dayOfWeek;
        this.dateLabel = dateLabel;
        this.periodCode = periodCode;
        this.timeRange = timeRange;
        this.sectionCode = sectionCode;
        this.batchCode = batchCode;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.sessionType = sessionType;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
        this.roomCode = roomCode;
        this.colorTheme = colorTheme;
        this.hasConflict = hasConflict;
        this.conflictReason = conflictReason;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getDateLabel() { return dateLabel; }
    public void setDateLabel(String dateLabel) { this.dateLabel = dateLabel; }
    public String getPeriodCode() { return periodCode; }
    public void setPeriodCode(String periodCode) { this.periodCode = periodCode; }
    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public String getSubstituteFaculty() { return substituteFaculty; }
    public void setSubstituteFaculty(String substituteFaculty) { this.substituteFaculty = substituteFaculty; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getColorTheme() { return colorTheme; }
    public void setColorTheme(String colorTheme) { this.colorTheme = colorTheme; }
    public Boolean getHasConflict() { return hasConflict; }
    public void setHasConflict(Boolean hasConflict) { this.hasConflict = hasConflict; }
    public String getConflictReason() { return conflictReason; }
    public void setConflictReason(String conflictReason) { this.conflictReason = conflictReason; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Boolean getIsGenerated() { return isGenerated; }
    public void setIsGenerated(Boolean isGenerated) { this.isGenerated = isGenerated; }
}
