package com.project.HR.repository;

import com.project.HR.model.NationalHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for {@link NationalHoliday} entities.
 * Provides standard CRUD operations and custom queries for accessing holiday data.
 */
@Repository
public interface NationalHolidayRepository extends JpaRepository<NationalHoliday, Integer> {

    /**
     * Finds all national holidays and makeup workdays within a given date range.
     * This is crucial for calculating actual workdays for leave requests.
     *
     * @param startDate The start of the date range (inclusive).
     * @param endDate   The end of the date range (inclusive).
     * @return A list of {@link NationalHoliday} entities falling within the specified range.
     */
    List<NationalHoliday> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Modifying
    @Query("DELETE FROM NationalHoliday n WHERE n.source = :source AND n.sourceYear = :year")
    void deleteBySourceAndYear(@Param("source") String source, @Param("year") int year);
}
