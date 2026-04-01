package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.repository.FinancialRecordRepository;
import com.aureon.backend.service.DashboardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Override
    @Transactional(readOnly = true)
    public Responses.DashboardSummary getSummary() {
        BigDecimal totalIncome   = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);
        long totalRecords        = recordRepository.count();

        return new Responses.DashboardSummary(totalIncome, totalExpenses, netBalance, totalRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Responses.CategoryTotal> getCategoryTotals() {
        return recordRepository.findCategoryTotals().stream()
                .map(row -> new Responses.CategoryTotal(
                        (String) row[0],
                        TransactionType.valueOf((String) row[1]),
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Responses.MonthlyTrend> getMonthlyTrends(LocalDate from, LocalDate to) {
        // Default: last 12 months if not specified
        LocalDate effectiveTo   = to   != null ? to   : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusMonths(11).withDayOfMonth(1);

        return recordRepository.findMonthlyTrends(effectiveFrom, effectiveTo).stream()
                .map(row -> new Responses.MonthlyTrend(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        TransactionType.valueOf((String) row[2]),
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Responses.RecordResponse> getRecentActivity(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50); // clamp between 1–50
        return recordRepository.findRecentRecords(PageRequest.of(0, safeLimit))
                .stream()
                .map(Responses.RecordResponse::from)
                .toList();
    }
}
