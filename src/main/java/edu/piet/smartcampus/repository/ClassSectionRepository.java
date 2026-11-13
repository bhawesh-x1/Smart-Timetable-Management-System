package edu.piet.smartcampus.repository;

import edu.piet.smartcampus.model.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
}
