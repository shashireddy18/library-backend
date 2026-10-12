package com.library.dto;

import java.math.BigDecimal;

public record DashboardStats(long totalBooks, long totalStudents, long issuedBooks,
                             long overdueBooks, BigDecimal finesCollected) {}
