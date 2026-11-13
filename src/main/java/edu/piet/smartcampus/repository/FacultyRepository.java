package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByDatasetModeInOrderByDisplayIdAsc(List<String> modes);
}
