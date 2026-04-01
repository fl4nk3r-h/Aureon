package com.aureon.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.aureon.backend.dto.request.RecordRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.security.AuthenticatedUser;
import com.aureon.backend.service.FinancialRecordService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD operations on financial entries. GET: ANALYST+, mutate: ADMIN only.")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @GetMapping
    @Operation(summary = "List records with filtering and pagination")
    public ResponseEntity<Responses.PagedResponse<Responses.RecordResponse>> getRecords(
            @Parameter(description = "Filter by type") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by category (partial match)") @RequestParam(required = false) String category,
            @Parameter(description = "Date range start (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Date range end (yyyy-MM-dd)")   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Search in category and notes")  @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(
                recordService.getRecords(type, category, from, to, search, PageRequest.of(page, size, sort))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single record by ID")
    public ResponseEntity<Responses.RecordResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    @PostMapping
    @Operation(summary = "Create a financial record (ADMIN only)")
    public ResponseEntity<Responses.RecordResponse> create(
            @Valid @RequestBody RecordRequests.CreateRecordRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recordService.createRecord(request, actor));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a record (ADMIN only)")
    public ResponseEntity<Responses.RecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequests.UpdateRecordRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return ResponseEntity.ok(recordService.updateRecord(id, request, actor));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a record (ADMIN only)")
    public ResponseEntity<Responses.MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.deleteRecord(id));
    }
}
