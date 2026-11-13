package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "class_sections")
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, unique = true, length = 30)
    private String sectionCode;

    @Column(name = "section_name", nullable = false, length = 60)
    private String sectionName;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester = "Sem 3";

    @Column(name = "course_name", nullable = false, length = 120)
    private String courseName = "B.Tech Computer Engineering";

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode = "CSE";

    @Column(name = "sub_batches", nullable = false, length = 60)
    private String subBatches;

    @Column(name = "primary_room", nullable = false, length = 40)
    private String primaryRoom;

    @Column(name = "student_strength", nullable = false)
    private Integer studentStrength = 60;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear = "2026-2027";

    public ClassSection() {}

    public ClassSection(String sectionCode, String sectionName, String semester, String courseName,
                        String departmentCode, String subBatches, String primaryRoom, Integer studentStrength, String academicYear) {
        this.sectionCode = sectionCode;
        this.sectionName = sectionName;
        this.semester = semester;
        this.courseName = courseName;
        this.departmentCode = departmentCode;
        this.subBatches = subBatches;
        this.primaryRoom = primaryRoom;
        this.studentStrength = studentStrength;
        this.academicYear = academicYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getSubBatches() { return subBatches; }
    public void setSubBatches(String subBatches) { this.subBatches = subBatches; }
    public String getPrimaryRoom() { return primaryRoom; }
    public void setPrimaryRoom(String primaryRoom) { this.primaryRoom = primaryRoom; }
    public Integer getStudentStrength() { return studentStrength; }
    public void setStudentStrength(Integer studentStrength) { this.studentStrength = studentStrength; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
