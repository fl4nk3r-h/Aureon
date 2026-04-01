package com.aureon.backend.service;

import java.time.LocalDate;
import java.util.List;

import com.aureon.backend.dto.response.Responses;

public interface DashboardService {
    Responses.DashboardSummary getSummary();
    List<Responses.CategoryTotal> getCategoryTotals();
    List<Responses.MonthlyTrend> getMonthlyTrends(LocalDate from, LocalDate to);
    List<Responses.RecordResponse> getRecentActivity(int limit);
}
