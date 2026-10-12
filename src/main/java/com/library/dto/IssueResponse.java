package com.library.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IssueResponse(Long id, Long bookId, String bookTitle, Long studentId, String studentName,
                            String rollNo, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                            BigDecimal fine, long overdueDays, String status) {}
