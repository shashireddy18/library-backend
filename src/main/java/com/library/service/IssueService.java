package com.library.service;

import com.library.dto.DashboardStats;
import com.library.dto.IssueRequest;
import com.library.dto.IssueResponse;
import com.library.entity.Book;
import com.library.entity.IssueRecord;
import com.library.entity.Student;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.IssueRecordRepository;
import com.library.repository.StudentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {
    private final IssueRecordRepository issues;
    private final BookRepository books;
    private final StudentRepository students;

    @Value("${library.fine-per-day}")
    private BigDecimal finePerDay;

    @Value("${library.loan-days}")
    private int loanDays;

    public IssueService(IssueRecordRepository issues, BookRepository books, StudentRepository students) {
        this.issues = issues;
        this.books = books;
        this.students = students;
    }

    public List<IssueResponse> list(String status) {
        List<IssueRecord> records;
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            records = issues.findAllByOrderByIdDesc();
        } else {
            IssueRecord.Status st;
            try {
                st = IssueRecord.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid status filter");
            }
            records = issues.findByStatusOrderByIdDesc(st);
        }
        return records.stream().map(this::toResponse).toList();
    }

    @Transactional
    public IssueResponse issue(IssueRequest req) {
        Student student = students.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Book book = books.findById(req.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No copies of this book are currently available");
        }
        if (issues.existsByBookIdAndStudentIdAndStatus(book.getId(), student.getId(), IssueRecord.Status.ISSUED)) {
            throw new BadRequestException("This student already has this book issued");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        books.save(book);

        IssueRecord r = new IssueRecord();
        r.setBook(book);
        r.setStudent(student);
        r.setIssueDate(LocalDate.now());
        r.setDueDate(LocalDate.now().plusDays(loanDays));
        r.setFine(BigDecimal.ZERO);
        r.setStatus(IssueRecord.Status.ISSUED);
        return toResponse(issues.save(r));
    }

    @Transactional
    public IssueResponse returnBook(Long id) {
        IssueRecord r = issues.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue record not found"));
        if (r.getStatus() == IssueRecord.Status.RETURNED) {
            throw new BadRequestException("This book has already been returned");
        }
        LocalDate today = LocalDate.now();
        r.setReturnDate(today);
        r.setFine(calcFine(r.getDueDate(), today));
        r.setStatus(IssueRecord.Status.RETURNED);

        Book book = r.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        books.save(book);

        return toResponse(issues.save(r));
    }

    public DashboardStats stats() {
        BigDecimal fines = issues.sumCollectedFines();
        return new DashboardStats(
                books.count(),
                students.count(),
                issues.countByStatus(IssueRecord.Status.ISSUED),
                issues.countByStatusAndDueDateBefore(IssueRecord.Status.ISSUED, LocalDate.now()),
                fines == null ? BigDecimal.ZERO : fines);
    }

    private BigDecimal calcFine(LocalDate due, LocalDate end) {
        long late = Math.max(0, ChronoUnit.DAYS.between(due, end));
        return finePerDay.multiply(BigDecimal.valueOf(late));
    }

    private IssueResponse toResponse(IssueRecord r) {
        LocalDate end = r.getReturnDate() != null ? r.getReturnDate() : LocalDate.now();
        long overdue = Math.max(0, ChronoUnit.DAYS.between(r.getDueDate(), end));
        BigDecimal fine = r.getStatus() == IssueRecord.Status.RETURNED
                ? r.getFine()
                : finePerDay.multiply(BigDecimal.valueOf(overdue));
        return new IssueResponse(r.getId(), r.getBook().getId(), r.getBook().getTitle(),
                r.getStudent().getId(), r.getStudent().getName(), r.getStudent().getRollNo(),
                r.getIssueDate(), r.getDueDate(), r.getReturnDate(), fine, overdue, r.getStatus().name());
    }
}
