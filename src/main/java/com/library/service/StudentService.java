package com.library.service;

import com.library.entity.Student;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.IssueRecordRepository;
import com.library.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository students;
    private final IssueRecordRepository issues;

    public StudentService(StudentRepository students, IssueRecordRepository issues) {
        this.students = students;
        this.issues = issues;
    }

    public List<Student> list(String q) {
        if (q == null || q.isBlank()) {
            return students.findAll();
        }
        String s = q.trim();
        return students.findByNameContainingIgnoreCaseOrRollNoContainingIgnoreCaseOrEmailContainingIgnoreCase(s, s, s);
    }

    public Student get(Long id) {
        return students.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    @Transactional
    public Student create(Student s) {
        if (students.existsByEmail(s.getEmail().trim())) {
            throw new ConflictException("A student with this email already exists");
        }
        if (students.existsByRollNo(s.getRollNo().trim())) {
            throw new ConflictException("A student with this roll number already exists");
        }
        s.setId(null);
        s.setEmail(s.getEmail().trim());
        s.setRollNo(s.getRollNo().trim());
        return students.save(s);
    }

    @Transactional
    public Student update(Long id, Student in) {
        Student s = get(id);
        if (students.existsByEmailAndIdNot(in.getEmail().trim(), id)) {
            throw new ConflictException("A student with this email already exists");
        }
        if (students.existsByRollNoAndIdNot(in.getRollNo().trim(), id)) {
            throw new ConflictException("A student with this roll number already exists");
        }
        s.setName(in.getName());
        s.setRollNo(in.getRollNo().trim());
        s.setEmail(in.getEmail().trim());
        s.setDepartment(in.getDepartment());
        s.setPhone(in.getPhone());
        return students.save(s);
    }

    @Transactional
    public void delete(Long id) {
        Student s = get(id);
        if (issues.existsByStudentId(id)) {
            throw new ConflictException("Cannot delete: this student has issue history");
        }
        students.delete(s);
    }
}
