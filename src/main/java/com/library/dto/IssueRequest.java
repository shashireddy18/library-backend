package com.library.dto;

import jakarta.validation.constraints.NotNull;

public record IssueRequest(@NotNull(message = "Student is required") Long studentId,
                           @NotNull(message = "Book is required") Long bookId) {}
