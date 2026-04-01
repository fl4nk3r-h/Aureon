package com.aureon.backend.service;

import org.springframework.data.domain.Pageable;

import com.aureon.backend.dto.request.RecordRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.security.AuthenticatedUser;

import java.time.LocalDate;

public interface FinancialRecordService {
    Responses.PagedResponse<Responses.RecordResponse> getRecords(
            TransactionType type, String category,
            LocalDate from, LocalDate to, String search,
            Pageable pageable);

    Responses.RecordResponse getRecordById(Long id);

    Responses.RecordResponse createRecord(RecordRequests.CreateRecordRequest request, AuthenticatedUser actor);

    Responses.RecordResponse updateRecord(Long id, RecordRequests.UpdateRecordRequest request, AuthenticatedUser actor);

    Responses.MessageResponse deleteRecord(Long id);
}
