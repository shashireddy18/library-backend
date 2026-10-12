package com.library.repository;

import com.library.entity.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByRollNo(String rollNo);
    boolean existsByRollNoAndIdNot(String rollNo, Long id);
    List<Student> findByNameContainingIgnoreCaseOrRollNoContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String rollNo, String email);
}
