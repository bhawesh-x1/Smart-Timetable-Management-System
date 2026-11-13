package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_id", nullable = false)
    private Integer displayId;

    @Column(name = "faculty_code", nullable = false, length = 30)
    private String facultyCode;

    @Column(name = "short_abbr", length = 20)
    private String shortAbbr;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "designation", nullable = false, length = 80)
    private String designation;

    @Column(name = "specialization", nullable = false, length = 120)
    private String specialization;

    @Column(name = "assigned_subjects", nullable = false, length = 255)
    private String assignedSubjects;

    @Column(name = "max_weekly_hours", nullable = false)
    private Integer maxWeeklyHours = 18;

    @Column(name = "on_leave", nullable = false)
    private Boolean onLeave = false;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public Faculty() {}

    public Faculty(Integer displayId, String facultyCode, String shortAbbr, String fullName, String email,
                   String phone, String departmentName, String designation, String specialization,
                   String assignedSubjects, Integer maxWeeklyHours, Boolean onLeave, String datasetMode) {
        this.displayId = displayId;
        this.facultyCode = facultyCode;
        this.shortAbbr = shortAbbr;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.departmentName = departmentName;
        this.designation = designation;
        this.specialization = specialization;
        this.assignedSubjects = assignedSubjects;
        this.maxWeeklyHours = maxWeeklyHours;
        this.onLeave = onLeave;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDisplayId() { return displayId; }
    public void setDisplayId(Integer displayId) { this.displayId = displayId; }
    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }
    public String getShortAbbr() { return shortAbbr; }
    public void setShortAbbr(String shortAbbr) { this.shortAbbr = shortAbbr; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getAssignedSubjects() { return assignedSubjects; }
    public void setAssignedSubjects(String assignedSubjects) { this.assignedSubjects = assignedSubjects; }
    public Integer getMaxWeeklyHours() { return maxWeeklyHours; }
    public void setMaxWeeklyHours(Integer maxWeeklyHours) { this.maxWeeklyHours = maxWeeklyHours; }
    public Boolean getOnLeave() { return onLeave; }
    public void setOnLeave(Boolean onLeave) { this.onLeave = onLeave; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
