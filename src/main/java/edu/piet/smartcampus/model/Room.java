package edu.piet.smartcampus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, length = 40)
    private String roomCode;

    @Column(name = "room_name", nullable = false, length = 120)
    private String roomName;

    @Column(name = "room_type", nullable = false, length = 40)
    private String roomType;

    @Column(name = "building_block", nullable = false, length = 40)
    private String buildingBlock;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "total_weekly_slots", nullable = false)
    private Integer totalWeeklySlots = 36;

    @Column(name = "occupied_slots_override")
    private Integer occupiedSlotsOverride;

    @Column(name = "dataset_mode", nullable = false, length = 20)
    private String datasetMode = "BOTH";

    public Room() {}

    public Room(String roomCode, String roomName, String roomType, String buildingBlock,
                Integer capacity, Integer totalWeeklySlots, Integer occupiedSlotsOverride, String datasetMode) {
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.roomType = roomType;
        this.buildingBlock = buildingBlock;
        this.capacity = capacity;
        this.totalWeeklySlots = totalWeeklySlots;
        this.occupiedSlotsOverride = occupiedSlotsOverride;
        this.datasetMode = datasetMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getBuildingBlock() { return buildingBlock; }
    public void setBuildingBlock(String buildingBlock) { this.buildingBlock = buildingBlock; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getTotalWeeklySlots() { return totalWeeklySlots; }
    public void setTotalWeeklySlots(Integer totalWeeklySlots) { this.totalWeeklySlots = totalWeeklySlots; }
    public Integer getOccupiedSlotsOverride() { return occupiedSlotsOverride; }
    public void setOccupiedSlotsOverride(Integer occupiedSlotsOverride) { this.occupiedSlotsOverride = occupiedSlotsOverride; }
    public String getDatasetMode() { return datasetMode; }
    public void setDatasetMode(String datasetMode) { this.datasetMode = datasetMode; }
}
