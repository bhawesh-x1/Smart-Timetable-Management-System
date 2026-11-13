package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_id", nullable = false)
    private Integer displayId;

    @Column(name = "subject_code", nullable = false, length = 40)
    private String subjectCode;

    @Column(name = "rtu_code", length = 40)
    private String rtuCode;

    @Column(name = "short_code", length = 30)
    private String shortCode;

    @Column(name = "subject_name", nullable = false, length = 180)
    private String subjectName;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "subject_type", nullable = false, length = 30)
    private String subjectType;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name = "weekly_hours", nullable = false)
    private Integer weeklyHours;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public Subject() {}

    public Subject(Integer displayId, String subjectCode, String rtuCode, String shortCode, String subjectName,
                   String departmentName, String semester, String subjectType, Integer credits, Integer weeklyHours,
                   String datasetMode) {
        this.displayId = displayId;
        this.subjectCode = subjectCode;
        this.rtuCode = rtuCode;
        this.shortCode = shortCode;
        this.subjectName = subjectName;
        this.departmentName = departmentName;
        this.semester = semester;
        this.subjectType = subjectType;
        this.credits = credits;
        this.weeklyHours = weeklyHours;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDisplayId() { return displayId; }
    public void setDisplayId(Integer displayId) { this.displayId = displayId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getRtuCode() { return rtuCode; }
    public void setRtuCode(String rtuCode) { this.rtuCode = rtuCode; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
