package com.aureon.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aureon.backend.entity.FinancialRecord;
import com.aureon.backend.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>,
        JpaSpecificationExecutor<FinancialRecord> {

    // Total by type (for dashboard)
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type AND r.deletedAt IS NULL")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Category-wise totals
    @Query("""
        SELECT r.category, r.type, COALESCE(SUM(r.amount), 0) as total
        FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
        GROUP BY r.category, r.type
        ORDER BY total DESC
        """)
    List<Object[]> findCategoryTotals();

    // Monthly trends
    @Query(value = """
        SELECT
            EXTRACT(YEAR FROM record_date) as year,
            EXTRACT(MONTH FROM record_date) as month,
            type,
            COALESCE(SUM(amount), 0) as total
        FROM financial_records
        WHERE deleted_at IS NULL
          AND record_date >= :from
          AND record_date <= :to
        GROUP BY year, month, type
        ORDER BY year ASC, month ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyTrends(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Recent activity
    @Query("SELECT r FROM FinancialRecord r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);

    // By date range
    Page<FinancialRecord> findByRecordDateBetweenAndDeletedAtIsNull(
            LocalDate from, LocalDate to, Pageable pageable);
}
