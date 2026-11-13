package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByDatasetModeInOrderByDisplayIdAsc(List<String> modes);
}
