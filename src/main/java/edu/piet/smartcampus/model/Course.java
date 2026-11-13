package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_id", nullable = false)
    private Integer displayId;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears = 4;

    @Column(name = "total_semesters", nullable = false)
    private Integer totalSemesters = 8;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits = 160;

    @Column(name = "coordinator", length = 120)
    private String coordinator;

    public Course() {}

    public Course(Integer displayId, String code, String name, String departmentCode, String departmentName,
                  Integer durationYears, Integer totalSemesters, Integer totalCredits, String coordinator) {
        this.displayId = displayId;
        this.code = code;
        this.name = name;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.durationYears = durationYears;
        this.totalSemesters = totalSemesters;
        this.totalCredits = totalCredits;
        this.coordinator = coordinator;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDisplayId() { return displayId; }
    public void setDisplayId(Integer displayId) { this.displayId = displayId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer durationYears) { this.durationYears = durationYears; }
    public Integer getTotalSemesters() { return totalSemesters; }
    public void setTotalSemesters(Integer totalSemesters) { this.totalSemesters = totalSemesters; }
    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }
    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }
}
