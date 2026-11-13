package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByDatasetModeOrderByIdAsc(String datasetMode);
    List<TimetableEntry> findByDatasetModeAndSemesterOrderByIdAsc(String datasetMode, String semester);
}
