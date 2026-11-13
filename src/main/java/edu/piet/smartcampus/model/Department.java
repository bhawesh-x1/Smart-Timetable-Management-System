package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_id", nullable = false)
    private Integer displayId;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "hod_name", length = 120)
    private String hodName;

    @Column(name = "building_block", length = 60)
    private String buildingBlock;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public Department() {}

    public Department(Integer displayId, String code, String name, String hodName, String buildingBlock, String datasetMode) {
        this.displayId = displayId;
        this.code = code;
        this.name = name;
        this.hodName = hodName;
        this.buildingBlock = buildingBlock;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDisplayId() { return displayId; }
    public void setDisplayId(Integer displayId) { this.displayId = displayId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHodName() { return hodName; }
    public void setHodName(String hodName) { this.hodName = hodName; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
