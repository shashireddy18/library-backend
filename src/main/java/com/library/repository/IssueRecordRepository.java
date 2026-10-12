package com.library.repository;

import com.library.entity.IssueRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findAllByOrderByIdDesc();
    List<IssueRecord> findByStatusOrderByIdDesc(IssueRecord.Status status);
    boolean existsByBookId(Long bookId);
    boolean existsByStudentId(Long studentId);
    boolean existsByBookIdAndStudentIdAndStatus(Long bookId, Long studentId, IssueRecord.Status status);
    long countByStatus(IssueRecord.Status status);
    long countByStatusAndDueDateBefore(IssueRecord.Status status, LocalDate date);

    @Query("select sum(r.fine) from IssueRecord r")
    BigDecimal sumCollectedFines();
}
