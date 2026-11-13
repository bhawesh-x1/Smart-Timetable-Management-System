package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "semester_number", nullable = false, unique = true)
    private Integer semesterNumber;

    @Column(name = "semester_code", nullable = false, unique = true, length = 20)
    private String semesterCode;

    @Column(name = "semester_name", nullable = false, length = 80)
    private String semesterName;

    @Column(name = "academic_term", nullable = false, length = 20)
    private String academicTerm; // ODD or EVEN

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear = "2026-2027";

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits = 22;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    public Semester() {}

    public Semester(Integer semesterNumber, String semesterCode, String semesterName, String academicTerm, Integer totalCredits) {
        this.semesterNumber = semesterNumber;
        this.semesterCode = semesterCode;
        this.semesterName = semesterName;
        this.academicTerm = academicTerm;
        this.totalCredits = totalCredits;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(Integer semesterNumber) { this.semesterNumber = semesterNumber; }

    public String getSemesterCode() { return semesterCode; }
    public void setSemesterCode(String semesterCode) { this.semesterCode = semesterCode; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getAcademicTerm() { return academicTerm; }
    public void setAcademicTerm(String academicTerm) { this.academicTerm = academicTerm; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Integer totalCredits) { this.totalCredits = totalCredits; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
