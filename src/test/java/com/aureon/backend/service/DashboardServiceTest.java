package com.aureon.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.repository.FinancialRecordRepository;
import com.aureon.backend.service.impl.DashboardServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    @Mock FinancialRecordRepository recordRepository;
    @InjectMocks DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("getSummary: calculates net balance correctly")
    void getSummary_calculatesNetBalance() {
        when(recordRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("10000.00"));
        when(recordRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("4500.00"));
        when(recordRepository.count()).thenReturn(25L);

        Responses.DashboardSummary summary = dashboardService.getSummary();

        assertThat(summary.totalIncome()).isEqualByComparingTo("10000.00");
        assertThat(summary.totalExpenses()).isEqualByComparingTo("4500.00");
        assertThat(summary.netBalance()).isEqualByComparingTo("5500.00");
        assertThat(summary.totalRecords()).isEqualTo(25L);
    }

    @Test
    @DisplayName("getCategoryTotals: maps raw DB rows correctly")
    void getCategoryTotals_mapsRows() {
        Object[] row = new Object[]{"Salary", "INCOME", new BigDecimal("8000.00")};
        when(recordRepository.findCategoryTotals()).thenReturn(List.of(row));

        List<Responses.CategoryTotal> totals = dashboardService.getCategoryTotals();

        assertThat(totals).hasSize(1);
        assertThat(totals.get(0).category()).isEqualTo("Salary");
        assertThat(totals.get(0).type()).isEqualTo(TransactionType.INCOME);
        assertThat(totals.get(0).total()).isEqualByComparingTo("8000.00");
    }

    @Test
    @DisplayName("getRecentActivity: clamps limit to max 50")
    void getRecentActivity_clampsLimit() {
        when(recordRepository.findRecentRecords(any())).thenReturn(List.of());

        dashboardService.getRecentActivity(200); // way over the limit

        // Verify it was called with a PageRequest of size ≤ 50
        verify(recordRepository).findRecentRecords(argThat(p -> p.getPageSize() == 50));
    }
}
