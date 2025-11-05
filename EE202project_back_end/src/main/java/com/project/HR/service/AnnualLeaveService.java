package com.project.HR.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AnnualLeaveService {

    /**
     * Calculates the total number of annual leave days an employee is entitled to
     * based on their hire date.
     *
     * NOTE: The rules implemented here are common examples.
     * You MUST replace this logic with your company's specific policies or
     * the regulations required by local labor laws (e.g., 勞動基準法 in Taiwan).
     *
     * @param hireDate The employee's hire date.
     * @return The total number of entitled annual leave days for the current year.
     */
    public double calculateAnnualLeaveEntitlementDays(LocalDate hireDate) {
        if (hireDate == null || hireDate.isAfter(LocalDate.now())) {
            return 0;
        }

        Period period = Period.between(hireDate, LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();

        // Example logic based on Taiwan's Labor Standards Act (approximated)
        if (years >= 10) {
            // 15 days, plus 1 day for each additional year, up to 30
            return Math.min(30, 15 + (years - 10)); 
        } else if (years >= 5) {
            return 15;
        } else if (years >= 3) {
            return 14;
        } else if (years >= 2) {
            return 10;
        } else if (years >= 1) {
            return 7;
        } else if (months >= 6) {
            return 3;
        } else {
            return 0;
        }
    }

    /**
     * Converts entitled days to hours, assuming an 8-hour workday.
     * @param days The number of entitled days.
     * @return The equivalent number of hours.
     */
    public double convertDaysToHours(double days) {
        return days * 8.0;
    }
}
