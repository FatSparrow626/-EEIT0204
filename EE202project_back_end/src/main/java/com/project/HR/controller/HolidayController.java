package com.project.HR.controller;

import com.project.HR.model.NationalHoliday;
import com.project.HR.service.HolidayService;
import com.project.core.security.EmployeeUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "假日管理", description = "提供國定假日查詢與匯入功能")
public class HolidayController {

    private final HolidayService holidayService;

    @Autowired
    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @Operation(summary = "查詢指定範圍內的假日", description = "提供給前端日曆等元件查詢國定假日、補班日等資訊")
    @GetMapping("/api/holidays")
    public ResponseEntity<List<NationalHoliday>> getHolidays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        List<NationalHoliday> holidays = holidayService.findHolidaysBetween(start, end);
        return ResponseEntity.ok(holidays);
    }

    @Operation(summary = "從 ICS 匯入年度假日", description = "【需 HOLIDAY_ADMIN 權限】提供 ICS 的 URL 來匯入整年度的假日資料")
    @PostMapping("/admin/holidays/import/ics")
    @PreAuthorize("hasAuthority('HOLIDAY_ADMIN')")
    public ResponseEntity<String> importHolidays(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        
        String icsUrl = payload.get("url");
        String yearStr = payload.get("year");

        if (icsUrl == null || yearStr == null) {
            return ResponseEntity.badRequest().body("Request must include 'url' and 'year'.");
        }

        try {
            int year = Integer.parseInt(yearStr);
            String operator = userDetails.getUsername();
            holidayService.importHolidaysFromIcs(icsUrl, year, operator);
            return ResponseEntity.ok("Holiday import process started for year " + year);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid 'year' format.");
        } catch (Exception e) {
            // In a real application, log the exception
            return ResponseEntity.internalServerError().body("An error occurred during import: " + e.getMessage());
        }
    }
}
