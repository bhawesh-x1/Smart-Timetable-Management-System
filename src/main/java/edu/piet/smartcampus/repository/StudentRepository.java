package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDatasetModeInOrderByDisplayIdAsc(List<String> modes);
}
