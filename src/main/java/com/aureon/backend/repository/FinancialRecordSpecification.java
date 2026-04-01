package com.aureon.backend.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.aureon.backend.entity.FinancialRecord;
import com.aureon.backend.enums.TransactionType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinancialRecordSpecification {

    private FinancialRecordSpecification() {}

    public static Specification<FinancialRecord> withFilters(
            TransactionType type,
            String category,
            LocalDate from,
            LocalDate to,
            String search) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), to));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("notes")), pattern),
                        cb.like(cb.lower(root.get("category")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
