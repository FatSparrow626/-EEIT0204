package com.project.HR.service;

import com.project.HR.model.NationalHoliday;
import com.project.HR.repository.NationalHolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HolidayService {

    private final NationalHolidayRepository holidayRepository;

    @Autowired
    public HolidayService(NationalHolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public List<NationalHoliday> findHolidaysBetween(LocalDate start, LocalDate end) {
        return holidayRepository.findByDateBetween(start, end);
    }

    /**
     * Calculates the precise leave hours between two datetimes, excluding weekends, holidays, and lunch breaks.
     * This is exposed publicly to be used by controllers for real-time calculations.
     * @param start The start datetime of the leave.
     * @param end The end datetime of the leave.
     * @return The calculated hours as a BigDecimal.
     */
    public BigDecimal calculateLeaveHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            return BigDecimal.ZERO;
        }

        List<NationalHoliday> holidays = findHolidaysBetween(start.toLocalDate(), end.toLocalDate());
        Set<LocalDate> holidayDates = holidays.stream()
            .filter(h -> !"MAKEUP_WORKDAY".equals(h.getType()))
            .map(NationalHoliday::getDate)
            .collect(Collectors.toSet());
        Set<LocalDate> makeupWorkdays = holidays.stream()
            .filter(h -> "MAKEUP_WORKDAY".equals(h.getType()))
            .map(NationalHoliday::getDate)
            .collect(Collectors.toSet());

        double totalMinutes = 0;
        LocalDate loopDate = start.toLocalDate();

        while (!loopDate.isAfter(end.toLocalDate())) {
            DayOfWeek dayOfWeek = loopDate.getDayOfWeek();
            boolean isWorkingDay = makeupWorkdays.contains(loopDate) || 
                                   (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY && !holidayDates.contains(loopDate));

            if (isWorkingDay) {
                LocalDateTime workDayStart = loopDate.atTime(9, 0);
                LocalDateTime workDayEnd = loopDate.atTime(18, 0);
                LocalDateTime lunchStart = loopDate.atTime(12, 0);
                LocalDateTime lunchEnd = loopDate.atTime(13, 0);

                LocalDateTime effectiveStart = loopDate.isEqual(start.toLocalDate()) ? start : workDayStart;
                LocalDateTime effectiveEnd = loopDate.isEqual(end.toLocalDate()) ? end : workDayEnd;
                
                if (effectiveStart.isBefore(workDayStart)) effectiveStart = workDayStart;
                if (effectiveEnd.isAfter(workDayEnd)) effectiveEnd = workDayEnd;

                if (effectiveStart.isBefore(effectiveEnd)) {
                    long minutesInDay = Duration.between(effectiveStart, effectiveEnd).toMinutes();

                    LocalDateTime lunchOverlapStart = effectiveStart.isAfter(lunchStart) ? effectiveStart : lunchStart;
                    LocalDateTime lunchOverlapEnd = effectiveEnd.isBefore(lunchEnd) ? effectiveEnd : lunchEnd;

                    if (lunchOverlapStart.isBefore(lunchOverlapEnd)) {
                        minutesInDay -= Duration.between(lunchOverlapStart, lunchOverlapEnd).toMinutes();
                    }
                    totalMinutes += minutesInDay;
                }
            }
            loopDate = loopDate.plusDays(1);
        }

        double totalHours = totalMinutes / 60.0;
        return BigDecimal.valueOf(totalHours).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void importHolidaysFromIcs(String icsUrl, int year, String operator) {
        holidayRepository.deleteBySourceAndYear("ICS", year);
        // Full implementation requires ical4j library and HTTP client.
        System.out.println("Placeholder: Importing holidays from " + icsUrl + " for year " + year);
    }
}
