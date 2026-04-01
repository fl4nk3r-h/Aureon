package com.aureon.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aureon.backend.dto.request.RecordRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.FinancialRecord;
import com.aureon.backend.entity.User;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.repository.FinancialRecordRepository;
import com.aureon.backend.repository.FinancialRecordSpecification;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.security.AuthenticatedUser;
import com.aureon.backend.service.FinancialRecordService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Responses.PagedResponse<Responses.RecordResponse> getRecords(
            TransactionType type, String category,
            LocalDate from, LocalDate to, String search,
            Pageable pageable) {

        var spec = FinancialRecordSpecification.withFilters(type, category, from, to, search);
        return Responses.PagedResponse.from(
                recordRepository.findAll(spec, pageable).map(Responses.RecordResponse::from)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Responses.RecordResponse getRecordById(Long id) {
        return Responses.RecordResponse.from(findById(id));
    }

    @Override
    @Transactional
    public Responses.RecordResponse createRecord(RecordRequests.CreateRecordRequest req, AuthenticatedUser actor) {
        User creator = userRepository.findById(actor.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", actor.getId()));

        FinancialRecord record = FinancialRecord.builder()
                .amount(req.amount())
                .type(req.type())
                .category(req.category().trim())
                .recordDate(req.recordDate())
                .notes(req.notes())
                .createdBy(creator)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Record created: id={} type={} amount={} by={}", saved.getId(), saved.getType(), saved.getAmount(), actor.getEmail());
        return Responses.RecordResponse.from(saved);
    }

    @Override
    @Transactional
    public Responses.RecordResponse updateRecord(Long id, RecordRequests.UpdateRecordRequest req, AuthenticatedUser actor) {
        FinancialRecord record = findById(id);
        User updater = userRepository.findById(actor.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", actor.getId()));

        if (req.amount()     != null) record.setAmount(req.amount());
        if (req.type()       != null) record.setType(req.type());
        if (req.category()   != null) record.setCategory(req.category().trim());
        if (req.recordDate() != null) record.setRecordDate(req.recordDate());
        if (req.notes()      != null) record.setNotes(req.notes());
        record.setUpdatedBy(updater);

        FinancialRecord saved = recordRepository.save(record);
        log.info("Record updated: id={} by={}", saved.getId(), actor.getEmail());
        return Responses.RecordResponse.from(saved);
    }

    @Override
    @Transactional
    public Responses.MessageResponse deleteRecord(Long id) {
        FinancialRecord record = findById(id);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
        log.info("Soft-deleted record id={}", id);
        return Responses.MessageResponse.of("Record deleted successfully.");
    }

    private FinancialRecord findById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("FinancialRecord", id));
    }
}
