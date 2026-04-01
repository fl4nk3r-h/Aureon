package com.finance.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.aureon.backend.dto.request.RecordRequests;
import com.aureon.backend.dto.response.Responses;
import com.aureon.backend.entity.FinancialRecord;
import com.aureon.backend.entity.User;
import com.aureon.backend.enums.Role;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.enums.UserStatus;
import com.aureon.backend.exception.ResourceNotFoundException;
import com.aureon.backend.repository.FinancialRecordRepository;
import com.aureon.backend.repository.UserRepository;
import com.aureon.backend.security.AuthenticatedUser;
import com.aureon.backend.service.impl.FinancialRecordServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialRecordService")
class FinancialRecordServiceTest {

    @Mock FinancialRecordRepository recordRepository;
    @Mock UserRepository userRepository;
    @InjectMocks FinancialRecordServiceImpl recordService;

    private User adminUser;
    private AuthenticatedUser actor;
    private FinancialRecord sampleRecord;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).email("admin@test.com").name("Admin")
                .role(Role.ADMIN).status(UserStatus.ACTIVE).build();

        actor = new AuthenticatedUser(1L, "admin@test.com", Role.ADMIN);

        sampleRecord = FinancialRecord.builder()
                .id(10L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .recordDate(LocalDate.now())
                .notes("Monthly salary")
                .createdBy(adminUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getRecords: returns paged results from specification")
    @SuppressWarnings("unchecked")
    void getRecords_returnsPaged() {
        var pageable = PageRequest.of(0, 10);
        when(recordRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(sampleRecord)));

        var result = recordService.getRecords(null, null, null, null, null, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).category()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("getRecordById: returns record when found")
    void getRecordById_found() {
        when(recordRepository.findById(10L)).thenReturn(Optional.of(sampleRecord));

        Responses.RecordResponse resp = recordService.getRecordById(10L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.amount()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("getRecordById: throws ResourceNotFoundException when missing")
    void getRecordById_notFound_throws() {
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createRecord: saves and returns new record")
    void createRecord_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(recordRepository.save(any())).thenReturn(sampleRecord);

        var request = new RecordRequests.CreateRecordRequest(
                new BigDecimal("500.00"), TransactionType.INCOME,
                "Salary", LocalDate.now(), "Monthly salary");

        Responses.RecordResponse resp = recordService.createRecord(request, actor);

        assertThat(resp.type()).isEqualTo(TransactionType.INCOME);
        assertThat(resp.category()).isEqualTo("Salary");
        verify(recordRepository).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("updateRecord: updates only provided fields")
    void updateRecord_partialUpdate() {
        when(recordRepository.findById(10L)).thenReturn(Optional.of(sampleRecord));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(recordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new RecordRequests.UpdateRecordRequest(
                new BigDecimal("750.00"), null, null, null, null);

        Responses.RecordResponse resp = recordService.updateRecord(10L, request, actor);

        assertThat(resp.amount()).isEqualByComparingTo("750.00");
        assertThat(resp.category()).isEqualTo("Salary"); // unchanged
    }

    @Test
    @DisplayName("deleteRecord: soft-deletes by setting deletedAt")
    void deleteRecord_softDeletes() {
        when(recordRepository.findById(10L)).thenReturn(Optional.of(sampleRecord));
        when(recordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Responses.MessageResponse resp = recordService.deleteRecord(10L);

        assertThat(resp.message()).contains("deleted");
        assertThat(sampleRecord.getDeletedAt()).isNotNull();
    }
}
