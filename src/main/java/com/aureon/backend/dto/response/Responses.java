package com.aureon.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.aureon.backend.entity.FinancialRecord;
import com.aureon.backend.entity.User;
import com.aureon.backend.enums.Role;
import com.aureon.backend.enums.TransactionType;
import com.aureon.backend.enums.UserStatus;

public class Responses {

    // --- Auth ---

    public record AuthResponse(String token, String tokenType, UserSummary user) {
        public static AuthResponse of(String token, User user) {
            return new AuthResponse(token, "Bearer", UserSummary.from(user));
        }
    }

    public record MessageResponse(String message) {
        public static MessageResponse of(String msg) { return new MessageResponse(msg); }
    }

    // --- User ---

    public record UserResponse(
            Long id, String email, String name,
            Role role, UserStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getName(),
                    u.getRole(), u.getStatus(), u.getCreatedAt(), u.getUpdatedAt());
        }
    }

    public record UserSummary(Long id, String email, String name, Role role) {
        public static UserSummary from(User u) {
            return new UserSummary(u.getId(), u.getEmail(), u.getName(), u.getRole());
        }
    }

    // --- Financial Record ---

    public record RecordResponse(
            Long id,
            BigDecimal amount,
            TransactionType type,
            String category,
            LocalDate recordDate,
            String notes,
            UserSummary createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static RecordResponse from(FinancialRecord r) {
            return new RecordResponse(
                    r.getId(), r.getAmount(), r.getType(), r.getCategory(),
                    r.getRecordDate(), r.getNotes(),
                    UserSummary.from(r.getCreatedBy()),
                    r.getCreatedAt(), r.getUpdatedAt()
            );
        }
    }

    // --- Dashboard ---

    public record DashboardSummary(
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal netBalance,
            long totalRecords
    ) {}

    public record CategoryTotal(String category, TransactionType type, BigDecimal total) {}

    public record MonthlyTrend(int year, int month, TransactionType type, BigDecimal total) {}

    public record PagedResponse<T>(
            java.util.List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> PagedResponse<T> from(org.springframework.data.domain.Page<T> page) {
            return new PagedResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
        }
    }
}
