package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_id", nullable = false)
    private Integer displayId;

    @Column(name = "enrollment_no", nullable = false, length = 30)
    private String enrollmentNo;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "section_batch", nullable = false, length = 40)
    private String sectionBatch;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public Student() {}

    public Student(Integer displayId, String enrollmentNo, String fullName, String email, String phone,
                   String departmentName, String courseName, String semester, String sectionBatch,
                   Integer admissionYear, String datasetMode) {
        this.displayId = displayId;
        this.enrollmentNo = enrollmentNo;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.departmentName = departmentName;
        this.courseName = courseName;
        this.semester = semester;
        this.sectionBatch = sectionBatch;
        this.admissionYear = admissionYear;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDisplayId() { return displayId; }
    public void setDisplayId(Integer displayId) { this.displayId = displayId; }
    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSectionBatch() { return sectionBatch; }
    public void setSectionBatch(String sectionBatch) { this.sectionBatch = sectionBatch; }
    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
