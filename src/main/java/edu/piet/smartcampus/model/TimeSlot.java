package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_code", nullable = false, length = 20)
    private String slotCode;

    @Column(name = "slot_label", nullable = false, length = 60)
    private String slotLabel;

    @Column(name = "day_of_week", nullable = false, length = 40)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false, length = 20)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 20)
    private String endTime;

    @Column(name = "slot_type", nullable = false, length = 30)
    private String slotType;

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public TimeSlot() {}

    public TimeSlot(String slotCode, String slotLabel, String dayOfWeek, String startTime,
                    String endTime, String slotType, Integer periodNumber, String datasetMode) {
        this.slotCode = slotCode;
        this.slotLabel = slotLabel;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotType = slotType;
        this.periodNumber = periodNumber;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSlotCode() { return slotCode; }
    public void setSlotCode(String slotCode) { this.slotCode = slotCode; }
    public String getSlotLabel() { return slotLabel; }
    public void setSlotLabel(String slotLabel) { this.slotLabel = slotLabel; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }
    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
