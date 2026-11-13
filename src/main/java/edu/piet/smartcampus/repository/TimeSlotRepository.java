package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByDatasetModeInOrderByPeriodNumberAsc(List<String> modes);
}
