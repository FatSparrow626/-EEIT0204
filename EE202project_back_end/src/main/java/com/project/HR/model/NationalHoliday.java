package com.project.HR.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing a national holiday, makeup workday, or company-specific
 * day off.
 * This table serves as the Single Source of Truth (SSOT) for non-weekend day
 * calculations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hr_national_holidays", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "date", "type" }) // Avoid duplicate entries for the same date and type
})
public class NationalHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate date; // The effective date of the event

    @Column(length = 80, nullable = false)
    private String name; // e.g., "國慶日", "補班"

    @Column(length = 32, nullable = false)
    private String type; // e.g., NATIONAL_HOLIDAY, MAKEUP_WORKDAY, COMPANY_OFF

    @Column(nullable = false)
    private boolean allDay = true; // Typically, holidays are all-day events

    @Column(length = 16, nullable = false)
    private String source; // e.g., ICS, API, XLS, MANUAL

    @Column(nullable = false)
    private int sourceYear; // The year this data pertains to, for batch operations

    @Column(length = 64)
    private String sourceVersion; // ETag, Last-Modified, or file hash for idempotency

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 64, nullable = false)
    private String createdBy; // The operator or process that imported this record

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
