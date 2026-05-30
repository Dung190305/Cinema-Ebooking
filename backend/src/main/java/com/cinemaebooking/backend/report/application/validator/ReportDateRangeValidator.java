package com.cinemaebooking.backend.report.application.validator;


import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.report.application.dto.ReportDateRange;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ReportDateRangeValidator {

    private static final long MAX_REPORT_RANGE_DAYS = 366;

    public ReportDateRange validateAndBuild(LocalDate fromDate, LocalDate toDate) {
        LocalDate safeFromDate = fromDate == null
                ? LocalDate.now().withDayOfMonth(1)
                : fromDate;

        LocalDate safeToDate = toDate == null
                ? LocalDate.now()
                : toDate;

        if (safeFromDate.isAfter(safeToDate)) {
            throw CommonExceptions.invalidInput(
                    "fromDate: " + safeFromDate + ", toDate: " + safeToDate
                            + " - Ngày bắt đầu không được lớn hơn ngày kết thúc"
            );
        }

        long days = ChronoUnit.DAYS.between(safeFromDate, safeToDate);

        if (days > MAX_REPORT_RANGE_DAYS) {
            throw CommonExceptions.invalidInput(
                    "fromDate: " + safeFromDate + ", toDate: " + safeToDate
                            + " - Khoảng thời gian báo cáo không được vượt quá "
                            + MAX_REPORT_RANGE_DAYS + " ngày"
            );
        }

        LocalDateTime fromDateTime = safeFromDate.atStartOfDay();
        LocalDateTime toDateTime = safeToDate.plusDays(1).atStartOfDay();

        return new ReportDateRange(fromDateTime, toDateTime);
    }
}