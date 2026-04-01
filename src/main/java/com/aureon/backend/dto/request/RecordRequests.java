package com.aureon.backend.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.aureon.backend.enums.TransactionType;

public class RecordRequests {

    public record CreateRecordRequest(
            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
            @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
            BigDecimal amount,

            @NotNull(message = "Type is required")
            TransactionType type,

            @NotBlank(message = "Category is required")
            @Size(max = 100, message = "Category max 100 characters")
            String category,

            @NotNull(message = "Date is required")
            LocalDate recordDate,

            @Size(max = 2000, message = "Notes max 2000 characters")
            String notes
    ) {}

    public record UpdateRecordRequest(
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
            @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
            BigDecimal amount,

            TransactionType type,

            @Size(max = 100, message = "Category max 100 characters")
            String category,

            LocalDate recordDate,

            @Size(max = 2000, message = "Notes max 2000 characters")
            String notes
    ) {}
}
