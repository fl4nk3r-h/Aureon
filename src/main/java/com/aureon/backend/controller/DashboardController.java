package com.aureon.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.service.DashboardService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated analytics — accessible by VIEWER, ANALYST, and ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(
        summary = "Overall summary",
        description = "Returns total income, total expenses, net balance, and record count."
    )
    public ResponseEntity<Responses.DashboardSummary> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/categories")
    @Operation(
        summary = "Category-wise totals",
        description = "Breakdown of totals by category and transaction type."
    )
    public ResponseEntity<List<Responses.CategoryTotal>> getCategoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    @GetMapping("/trends")
    @Operation(
        summary = "Monthly income/expense trends",
        description = "Month-by-month aggregation. Defaults to the last 12 months if dates are omitted."
    )
    public ResponseEntity<List<Responses.MonthlyTrend>> getMonthlyTrends(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)")   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(from, to));
    }

    @GetMapping("/recent")
    @Operation(
        summary = "Recent activity",
        description = "Returns the most recent N financial records. Max 50."
    )
    public ResponseEntity<List<Responses.RecordResponse>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }
}
