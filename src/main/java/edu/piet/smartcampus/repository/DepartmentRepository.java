package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByDatasetModeInOrderByDisplayIdAsc(List<String> modes);
}
